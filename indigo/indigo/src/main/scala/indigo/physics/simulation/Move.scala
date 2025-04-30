package indigo.physics.simulation

import indigo.*
import indigo.physics.*

object Move:

  def colliders[Tag](
      timeDelta: Seconds,
      colliders: Batch[Collider[Tag]],
      combinedForce: Vector2,
      resistance: Resistance
  ): Batch[IndexedCollider[Tag]] =
    colliders.zipWithIndex.map(moveCollider(timeDelta, combinedForce, resistance))

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
