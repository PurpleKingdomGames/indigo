package indigo.physics

import indigo.*
import indigo.physics.Collider
import indigo.physics.Displacement
import indigo.physics.Friction
import indigo.physics.Mass
import indigo.physics.Resistance
import indigo.physics.World
import indigo.shared.geometry.LineSegment

import scala.annotation.tailrec

object Physics:

  def update[A](
      timeDelta: Seconds,
      world: World[A],
      transient: Batch[Collider[A]],
      settings: SimulationSettings
  ): Outcome[World[A]] =
    val moved: Batch[Internal.IndexedCollider[A]] =
      Internal.moveColliders(timeDelta, world)

    val collisions: Batch[(Internal.IndexedCollider[A], Batch[Collider[A]])] =
      Internal.findCollisionGroups(moved, transient.map(_.makeStatic), settings)

    val collisionEvents: Batch[GlobalEvent] =
      collisions.flatMap { case (c, cs) =>
        cs.flatMap(target => c.proposed.onCollisionWith(target))
      }

    val resolved: Batch[Collider[A]] =
      Internal.solveAllCollisions(collisions)

    Outcome(world.copy(colliders = resolved))
      .addGlobalEvents(collisionEvents)

  object Internal:

    given [A]: CanEqual[IndexedCollider[A], IndexedCollider[A]] = CanEqual.derived

    def moveColliders[A](timeDelta: Seconds, world: World[A]): Batch[IndexedCollider[A]] =
      world.colliders.zipWithIndex.map(moveCollider(timeDelta, world.combinedForce, world.resistance))

    def calculateNewMovement(
        timeDelta: Seconds,
        worldForces: Vector2,
        worldResistance: Resistance,
        position: Vertex,
        velocity: Vector2,
        mass: Mass
    ): (Vertex, Vector2) =
      val t = timeDelta.toDouble

      val acceleration = worldForces
      val force        = mass * acceleration

      // v(n + 1) = a * t + v(n)
      val newVelocity =
        ((force * Vector2(t)) + velocity) * (1.0 - worldResistance.toDouble)

      // p(n + 1) = v * t + p(n)
      val newPosition =
        (newVelocity * Vector2(t)).toVertex + position

      (newPosition, newVelocity)

    def moveCollider[A](timeDelta: Seconds, worldForces: Vector2, worldResistance: Resistance)(
        colliderWithIndex: (Collider[A], Int)
    ): IndexedCollider[A] =
      val t                 = timeDelta.toDouble
      val (collider, index) = colliderWithIndex

      collider match
        case c @ Collider.Circle(_, _, _, _, _, _, static, _, _) if static =>
          IndexedCollider(index, c, c)

        case c @ Collider.Circle(_, bounds, mass, velocity, _, _, _, _, _) =>
          val (p, v) = calculateNewMovement(timeDelta, worldForces, worldResistance, c.bounds.position, velocity, mass)

          IndexedCollider(
            index,
            c,
            c.copy(bounds = c.bounds.moveTo(p), velocity = v)
          )

        case c @ Collider.Box(_, _, _, _, _, _, static, _, _) if static =>
          IndexedCollider(index, c, c)

        case c @ Collider.Box(_, bounds, mass, velocity, _, _, _, _, _) =>
          val (p, v) = calculateNewMovement(timeDelta, worldForces, worldResistance, c.bounds.position, velocity, mass)

          IndexedCollider(
            index,
            c,
            c.copy(bounds = c.bounds.moveTo(p), velocity = v)
          )

    def combineAndCull[A](
        indexedColliders: Batch[IndexedCollider[A]],
        transient: Batch[Collider[A]],
        simulationBounds: BoundingBox
    ): Batch[IndexedCollider[A]] =
      (
        indexedColliders ++
          transient.zipWithIndex.map { case (t, i) => Internal.IndexedCollider(-i - 1, t, t) }
      ).filter(p => p.proposed.boundingBox.overlaps(simulationBounds))

    def findCollisionGroups[A](
        indexedColliders: Batch[IndexedCollider[A]],
        transient: Batch[Collider[A]],
        settings: SimulationSettings
    ): Batch[(IndexedCollider[A], Batch[Collider[A]])] =
      val lookup: QuadTree[BoundingBox, Internal.IndexedCollider[A]] =
        QuadTree
          .empty(settings.bounds)
          .insert(
            combineAndCull(indexedColliders, transient, settings.bounds).map(m => m.movementBounds -> m),
            settings.idealCount,
            settings.minSize,
            settings.maxDepth
          )

      indexedColliders.map { c =>
        if c.proposed.isStatic then c -> Batch()
        else
          val collisions =
            lookup
              .searchByBoundingBox(c.movementBounds)
              .map(_.value)
              .distinctBy(_.index)
              .filter { i =>
                i.index != c.index &&
                c.proposed.canCollideWith(i.proposed.tag) &&
                c.proposed.hitTest(i.proposed)
              }
              .map(_.proposed)

          c -> collisions
      }

    def solveAllCollisions[A](
        collisions: Batch[(IndexedCollider[A], Batch[Collider[A]])]
    ): Batch[Collider[A]] =
      collisions.map { case (c, cs) =>
        if c.proposed.isStatic then c.proposed else solveCollisions(c, cs)
      }

    def solveCollisions[A](indexed: IndexedCollider[A], collidees: Batch[Collider[A]]): Collider[A] =
      val collider = indexed.proposed

      if collidees.isEmpty then collider
      else
        val results =
          collidees.map { c =>
            val displacement = collider.displacementWith(c)
            val displaceBy   = displacement.displaceBy

            val maxEnergy = Math.max(c.velocity.magnitude, collider.velocity.magnitude)
            val remainingEnergy =
              Math.min(maxEnergy, (c.velocity + collider.velocity).magnitude) * collider.restitution.toDouble
            val continueDistance = displacement.displaceAmount * collider.restitution.toDouble

            collider match
              case Collider.Circle(_, bounds, _, velocity, _, friction, _, _, _) =>
                c match
                  case c: Collider.Circle[_] =>
                    solveCollisionWithCircle(
                      ray = displacement.contact,
                      position = bounds.position,
                      target = c,
                      velocity = velocity,
                      friction = c.friction + friction,
                      displaceBy = displaceBy,
                      continueDistance = continueDistance,
                      remainingEnergy = remainingEnergy
                    )

                  case c: Collider.Box[_] =>
                    solveCollisionWithBox(
                      displacement = displacement,
                      position = bounds.position,
                      center = bounds.center,
                      target = c,
                      velocity = velocity,
                      friction = c.friction + friction,
                      displaceBy = displaceBy,
                      continueDistance = continueDistance,
                      remainingEnergy = remainingEnergy
                    )

              case Collider.Box(_, bounds, _, velocity, _, friction, _, _, _) =>
                c match
                  case c: Collider.Circle[_] =>
                    solveCollisionWithCircle(
                      ray = displacement.contact,
                      position = bounds.position,
                      target = c,
                      velocity = velocity,
                      friction = c.friction + friction,
                      displaceBy = displaceBy,
                      continueDistance = continueDistance,
                      remainingEnergy = remainingEnergy
                    )

                  case c: Collider.Box[_] =>
                    solveCollisionWithBox(
                      displacement = displacement,
                      position = bounds.position,
                      center = bounds.center,
                      target = c,
                      velocity = velocity,
                      friction = c.friction + friction,
                      displaceBy = displaceBy,
                      continueDistance = continueDistance,
                      remainingEnergy = remainingEnergy
                    )
          }

        val meanVelocity =
          val vels = results.map(_.nextVelocity)
          vels.foldLeft(Vector2.zero)(_ + _) / vels.length.toDouble

        val meanPosition =
          val vrts = results.map(_.nextPosition)
          vrts.foldLeft(Vertex.zero)(_ + _) / vrts.length.toDouble

        collider.withVelocity(meanVelocity).withPosition(meanPosition)

    def solveCollisionWithCircle[A](
        ray: LineSegment,
        position: Vertex,
        target: Collider.Circle[A],
        velocity: Vector2,
        friction: Friction,
        displaceBy: Vector2,
        continueDistance: Double,
        remainingEnergy: Double
    ): Solved =
      val reflection     = target.reflect(ray)
      val nextPosition   = position + displaceBy
      val frictionAmount = Math.min(1.0, Math.max(0.0, 1.0 - friction.toDouble))

      reflection.map(r => r.reflected) match
        case None =>
          Solved(nextPosition, velocity * frictionAmount)

        case Some(reflected) if reflected ~== Vector2.zero =>
          Solved(nextPosition, velocity * frictionAmount)

        case Some(reflected) =>
          Solved(nextPosition + (reflected * continueDistance), reflected * remainingEnergy * frictionAmount)

    def solveCollisionWithBox[A](
        displacement: Displacement,
        position: Vertex,
        center: Vertex,
        target: Collider.Box[A],
        velocity: Vector2,
        friction: Friction,
        displaceBy: Vector2,
        continueDistance: Double,
        remainingEnergy: Double
    ): Solved =
      val strikePoint        = target.reflect(displacement.contact).map(_.at).getOrElse(displacement.contact.start)
      val normalisedVelocity = velocity.normalise
      val ray                = LineSegment(strikePoint - normalisedVelocity, strikePoint + normalisedVelocity)
      val reflection         = target.reflect(ray)
      val nextPosition       = position + displaceBy
      val frictionAmount     = Math.min(1.0, Math.max(0.0, 1.0 - friction.toDouble))

      reflection.map(r => r.reflected) match
        case None =>
          Solved(nextPosition, velocity * frictionAmount)

        case Some(reflected) if reflected ~== Vector2.zero =>
          Solved(nextPosition, velocity * frictionAmount)

        case Some(reflected) =>
          Solved(nextPosition + (reflected * continueDistance), reflected * remainingEnergy * frictionAmount)

    final case class IndexedCollider[A](index: Int, previous: Collider[A], proposed: Collider[A]):
      val safeMove: Boolean = previous.boundingBox.overlaps(proposed.boundingBox)
      val movementBounds: BoundingBox =
        if safeMove then proposed.boundingBox
        else BoundingBox.expandToInclude(previous.boundingBox, proposed.boundingBox)

    final case class Solved(nextPosition: Vertex, nextVelocity: Vector2)
