package indigo.physics

import indigo.*
import indigo.physics.Friction
import indigo.physics.Mass
import indigo.physics.Resistance
import indigo.shared.geometry.LineSegment

import scala.annotation.tailrec

object Physics:

  def update[Tag](
      timeDelta: Seconds,
      world: World[Tag],
      transient: Batch[Collider[Tag]],
      settings: SimulationSettings
  ): Outcome[World[Tag]] =
    val moved: Batch[Internal.IndexedCollider[Tag]] =
      Internal.moveColliders(timeDelta, world.colliders, world.combinedForce, world.resistance)

    val staticTransient = transient.map(_.makeStatic)

    @tailrec
    def rec(
        remaining: Int,
        workingColliders: Batch[Internal.IndexedCollider[Tag]],
        accEvents: Batch[GlobalEvent]
    ): (Batch[Internal.IndexedCollider[Tag]], Batch[GlobalEvent]) =
      if remaining == 0 then (workingColliders, accEvents)
      else
        val collisions: Batch[(Internal.IndexedCollider[Tag], Batch[Collider[Tag]])] =
          Internal.findCollisionGroups(workingColliders, staticTransient, settings)

        if collisions.exists(_._2.nonEmpty) then
          val collisionEvents: Batch[GlobalEvent] =
            collisions.flatMap { case (c, cs) =>
              cs.flatMap(target => c.proposed.onCollisionWith(target))
            }

          val resolved: Batch[Internal.IndexedCollider[Tag]] =
            Internal.solveAllCollisions(collisions)

          rec(remaining - 1, resolved, collisionEvents)
        else (workingColliders, accEvents)

    val (resolved, collisionEvents) =
      rec(settings.maxIterations, moved, Batch.empty)

    Outcome(world.copy(colliders = resolved.map(_.proposed)))
      .addGlobalEvents(collisionEvents)

  object Internal:

    given [Tag]: CanEqual[IndexedCollider[Tag], IndexedCollider[Tag]] = CanEqual.derived

    def moveColliders[Tag](
        timeDelta: Seconds,
        colliders: Batch[Collider[Tag]],
        combinedForce: Vector2,
        resistance: Resistance
    ): Batch[IndexedCollider[Tag]] =
      colliders.zipWithIndex.map(moveCollider(timeDelta, combinedForce, resistance))

    def calculateNewMovement(
        timeDelta: Seconds,
        worldForces: Vector2,
        worldResistance: Resistance,
        position: Vertex,
        velocity: Vector2,
        terminalVelocity: Vector2,
        mass: Mass
    ): (Vertex, Vector2) =
      val t = timeDelta.toDouble

      val acceleration = worldForces
      val force        = mass * acceleration

      // v(n + 1) = a * t + v(n)
      val newVelocity =
        ((force * Vector2(t)) + velocity) * (1.0 - worldResistance.toDouble)

      val clampedVelocity =
        clampVelocity(newVelocity, terminalVelocity)

      // p(n + 1) = v * t + p(n)
      val newPosition =
        (clampedVelocity * Vector2(t)).toVertex + position

      (newPosition, clampedVelocity)

    def clampVelocity(velocity: Vector2, terminalVelocity: Vector2): Vector2 =
      val abs = terminalVelocity.abs
      velocity.clamp(abs.invert, abs)

    def moveCollider[Tag](timeDelta: Seconds, worldForces: Vector2, worldResistance: Resistance)(
        colliderWithIndex: (Collider[Tag], Int)
    ): IndexedCollider[Tag] =
      val (collider, index) = colliderWithIndex

      collider match
        case c: Collider.Circle[Tag] if c.static =>
          IndexedCollider(index, c, c)

        case c: Collider.Circle[Tag] =>
          val (p, v) = calculateNewMovement(
            timeDelta,
            worldForces,
            worldResistance,
            c.bounds.position,
            c.velocity,
            c.terminalVelocity,
            c.mass
          )

          IndexedCollider(
            index,
            c,
            c.copy(bounds = c.bounds.moveTo(p), velocity = v)
          )

        case c: Collider.Box[Tag] if c.static =>
          IndexedCollider(index, c, c)

        case c: Collider.Box[Tag] =>
          val (p, v) = calculateNewMovement(
            timeDelta,
            worldForces,
            worldResistance,
            c.bounds.position,
            c.velocity,
            c.terminalVelocity,
            c.mass
          )

          IndexedCollider(
            index,
            c,
            c.copy(bounds = c.bounds.moveTo(p), velocity = v)
          )

    def combineAndCull[Tag](
        indexedColliders: Batch[IndexedCollider[Tag]],
        transient: Batch[Collider[Tag]],
        simulationBounds: BoundingBox
    ): Batch[IndexedCollider[Tag]] =
      (
        indexedColliders ++
          transient.zipWithIndex.map { case (t, i) => Internal.IndexedCollider(-i - 1, t, t) }
      ).filter(p => p.proposed.boundingBox.overlaps(simulationBounds))

    def findCollisionGroups[Tag](
        indexedColliders: Batch[IndexedCollider[Tag]],
        transient: Batch[Collider[Tag]],
        settings: SimulationSettings
    ): Batch[(IndexedCollider[Tag], Batch[Collider[Tag]])] =
      val bounds =
        settings.bounds
          .getOrElse(
            indexedColliders
              .map(_.movementBounds)
              .foldLeft(BoundingBox.zero)(_.expandToInclude(_))
          )

      val lookup: QuadTree[BoundingBox, Internal.IndexedCollider[Tag]] =
        QuadTree
          .empty(bounds)
          .insert(
            combineAndCull(indexedColliders, transient, bounds).map(m => m.movementBounds -> m),
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

    def solveAllCollisions[Tag](
        collisions: Batch[(IndexedCollider[Tag], Batch[Collider[Tag]])]
    ): Batch[IndexedCollider[Tag]] =
      collisions.map { case (c, cs) =>
        if c.proposed.isStatic then c else solveCollisions(c, cs)
      }

    def solveCollisions[Tag](indexed: IndexedCollider[Tag], collidees: Batch[Collider[Tag]]): IndexedCollider[Tag] =
      val collider = indexed.proposed

      if collidees.isEmpty then indexed
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
              case cc: Collider.Circle[Tag] =>
                c match
                  case c: Collider.Circle[_] =>
                    solveCollisionWithCircle(
                      ray = displacement.contact,
                      position = cc.bounds.position,
                      target = c,
                      velocity = cc.velocity,
                      friction = c.friction + cc.friction,
                      displaceBy = displaceBy,
                      continueDistance = continueDistance,
                      remainingEnergy = remainingEnergy
                    )

                  case c: Collider.Box[_] =>
                    solveCollisionWithBox(
                      displacement = displacement,
                      position = cc.bounds.position,
                      center = cc.bounds.center,
                      target = c,
                      velocity = cc.velocity,
                      friction = c.friction + cc.friction,
                      displaceBy = displaceBy,
                      continueDistance = continueDistance,
                      remainingEnergy = remainingEnergy
                    )

              case cc: Collider.Box[Tag] =>
                c match
                  case c: Collider.Circle[_] =>
                    solveCollisionWithCircle(
                      ray = displacement.contact,
                      position = cc.bounds.position,
                      target = c,
                      velocity = cc.velocity,
                      friction = c.friction + cc.friction,
                      displaceBy = displaceBy,
                      continueDistance = continueDistance,
                      remainingEnergy = remainingEnergy
                    )

                  case c: Collider.Box[_] =>
                    solveCollisionWithBox(
                      displacement = displacement,
                      position = cc.bounds.position,
                      center = cc.bounds.center,
                      target = c,
                      velocity = cc.velocity,
                      friction = c.friction + cc.friction,
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

        indexed.copy(
          proposed = collider.withVelocity(meanVelocity).withPosition(meanPosition)
        )

    def solveCollisionWithCircle[Tag](
        ray: LineSegment,
        position: Vertex,
        target: Collider.Circle[Tag],
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

    def solveCollisionWithBox[Tag](
        displacement: Displacement,
        position: Vertex,
        center: Vertex,
        target: Collider.Box[Tag],
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

    final case class IndexedCollider[Tag](index: Int, previous: Collider[Tag], proposed: Collider[Tag]):
      val safeMove: Boolean = previous.boundingBox.overlaps(proposed.boundingBox)
      val movementBounds: BoundingBox =
        if safeMove then proposed.boundingBox
        else BoundingBox.expandToInclude(previous.boundingBox, proposed.boundingBox)

    final case class Solved(nextPosition: Vertex, nextVelocity: Vector2)
