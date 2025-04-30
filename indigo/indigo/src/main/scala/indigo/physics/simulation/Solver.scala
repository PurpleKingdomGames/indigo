package indigo.physics.simulation

import indigo.*
import indigo.physics.*
import indigo.shared.geometry.LineSegment

object Solver:

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
