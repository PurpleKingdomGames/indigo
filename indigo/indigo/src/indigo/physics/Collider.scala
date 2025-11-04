package indigo.physics

import indigo.*
import indigo.physics.Friction
import indigo.physics.Mass
import indigo.physics.Restitution
import indigo.shared.geometry.ReflectionData

enum Collider[Tag]:
  def tag: Tag
  def static: Boolean
  def velocity: Vector2
  def terminalVelocity: Vector2
  def mass: Mass
  def restitution: Restitution
  def canCollideWith: Tag => Boolean
  def onCollisionWith: Collider[Tag] => Batch[GlobalEvent]

  def boundingBox: BoundingBox =
    this match
      case c: Collider.Circle[_] => c.bounds.toIncircleBoundingBox
      case c: Collider.Box[_]    => c.bounds

  /** The angle of the velocity vector in radians, normalized to the range (-π, π].
    *
    * Important note: The angle is calculated using the arctangent of the y and x components of the velocity vector,
    * meaning that 0 radians (0 degrees) points directly to the right (positive x direction), and angles increase
    * counter-clockwise.
    *
    * For example:
    *   - A velocity of (x = 1, y = 0) yields an angle of 0 radians (0 degrees).
    *   - A velocity of (x = 1, y = -1) yields an angle of -π/4 radians (-45 degrees).
    *   - A velocity of (x = 0, y = 1) yields an angle of π/2 radians (90 degrees).
    *   - A velocity of (x = -1, y = 0) yields an angle of π radians (180 degrees).
    */
  def velocityDirectionAngle: Radians =
    Radians(Math.atan2(velocity.y, velocity.x))

  case Circle(
      tag: Tag,
      bounds: BoundingCircle,
      mass: Mass,
      velocity: Vector2,
      terminalVelocity: Vector2,
      restitution: Restitution,
      friction: Friction,
      static: Boolean,
      canCollideWith: Tag => Boolean,
      onCollisionWith: Collider[Tag] => Batch[GlobalEvent]
  ) extends Collider[Tag]
  case Box(
      tag: Tag,
      bounds: BoundingBox,
      mass: Mass,
      velocity: Vector2,
      terminalVelocity: Vector2,
      restitution: Restitution,
      friction: Friction,
      static: Boolean,
      canCollideWith: Tag => Boolean,
      onCollisionWith: Collider[Tag] => Batch[GlobalEvent]
  ) extends Collider[Tag]

object Collider:

  def apply[Tag](tag: Tag, bounds: BoundingCircle | BoundingBox): Collider[Tag] =
    bounds match
      case b: BoundingCircle => Collider.Circle(tag, b)
      case b: BoundingBox    => Collider.Box(tag, b)

  object Circle:
    def apply[Tag](tag: Tag, bounds: BoundingCircle): Collider.Circle[Tag] =
      Collider.Circle(
        tag,
        bounds,
        Mass.default,
        Vector2.zero,
        Vector2.max,
        Restitution.default,
        Friction.zero,
        false,
        _ => true,
        _ => Batch.empty
      )

    def unapply[Tag](c: Collider.Circle[Tag]): Option[Tag] =
      Some(c.tag)

  object Box:
    def apply[Tag](tag: Tag, bounds: BoundingBox): Collider.Box[Tag] =
      Collider.Box(
        tag,
        bounds,
        Mass.default,
        Vector2.zero,
        Vector2.max,
        Restitution.default,
        Friction.zero,
        false,
        _ => true,
        _ => Batch.empty
      )

    def unapply[Tag](c: Collider.Box[Tag]): Option[Tag] =
      Some(c.tag)

  extension [Tag](c: Collider.Circle[Tag])
    def toBox: Collider.Box[Tag] =
      Collider.Box(
        c.tag,
        c.bounds.toIncircleBoundingBox,
        c.mass,
        c.velocity,
        c.terminalVelocity,
        c.restitution,
        c.friction,
        c.static,
        _ => true,
        _ => Batch.empty
      )

  extension [Tag](c: Collider[Tag])
    def isStatic: Boolean = c.static

    def position: Vertex =
      c match
        case cc: Circle[_] => cc.bounds.position
        case cc: Box[_]    => cc.bounds.position

    def center: Vertex =
      c match
        case cc: Circle[_] => cc.bounds.center
        case cc: Box[_]    => cc.bounds.center

    def makeStatic: Collider[Tag] = c match
      case cc: Circle[_] => cc.copy(static = true)
      case cc: Box[_]    => cc.copy(static = true)

    def makeDynamic: Collider[Tag] = c match
      case cc: Circle[_] => cc.copy(static = false)
      case cc: Box[_]    => cc.copy(static = false)

    def withFriction(value: Friction): Collider[Tag] =
      c match
        case cc: Circle[_] => cc.copy(friction = value)
        case cc: Box[_]    => cc.copy(friction = value)

    def withMass(value: Mass): Collider[Tag] =
      c match
        case cc: Circle[_] => cc.copy(mass = value)
        case cc: Box[_]    => cc.copy(mass = value)

    def withRestitution(value: Restitution): Collider[Tag] =
      c match
        case cc: Circle[_] => cc.copy(restitution = value)
        case cc: Box[_]    => cc.copy(restitution = value)

    def withVelocity(value: Vector2): Collider[Tag] =
      c match
        case cc: Circle[_] => cc.copy(velocity = value)
        case cc: Box[_]    => cc.copy(velocity = value)
    def withVelocity(x: Double, y: Double): Collider[Tag] =
      withVelocity(Vector2(x, y))

    def withTerminalVelocity(value: Vector2): Collider[Tag] =
      c match
        case cc: Circle[_] => cc.copy(terminalVelocity = value)
        case cc: Box[_]    => cc.copy(terminalVelocity = value)
    def withTerminalVelocity(x: Double, y: Double): Collider[Tag] =
      withTerminalVelocity(Vector2(x, y))

    def withPosition(value: Vertex): Collider[Tag] =
      c match
        case cc: Circle[_] => cc.copy(bounds = cc.bounds.moveTo(value))
        case cc: Box[_]    => cc.copy(bounds = cc.bounds.moveTo(value))
    def withPosition(x: Double, y: Double): Collider[Tag] =
      withPosition(Vertex(x, y))

    def collidesWith(tagTest: Tag => Boolean): Collider[Tag] =
      c match
        case cc: Circle[_] => cc.copy(canCollideWith = tagTest)
        case cc: Box[_]    => cc.copy(canCollideWith = tagTest)

    def onCollision(produceEvents: Collider[Tag] => Batch[GlobalEvent]): Collider[Tag] =
      c match
        case cc: Circle[_] => cc.copy(onCollisionWith = produceEvents)
        case cc: Box[_]    => cc.copy(onCollisionWith = produceEvents)

    def +(value: Vector2): Collider[Tag] =
      withVelocity(c.velocity + value)
    def -(value: Vector2): Collider[Tag] =
      withVelocity(c.velocity - value)
    def *(value: Vector2): Collider[Tag] =
      withVelocity(c.velocity * value)
    def /(value: Vector2): Collider[Tag] =
      withVelocity(c.velocity / value)

    def moveBy(amount: Vertex): Collider[Tag] =
      c match
        case cc: Circle[_] => cc.copy(bounds = cc.bounds.moveBy(amount))
        case cc: Box[_]    => cc.copy(bounds = cc.bounds.moveBy(amount))
    def moveBy(x: Double, y: Double): Collider[Tag] =
      moveBy(Vertex(x, y))
    def moveBy(amount: Vector2): Collider[Tag] =
      moveBy(Vertex.fromVector2(amount))

    def moveTo(newPosition: Vertex): Collider[Tag] =
      c match
        case cc: Circle[_] => cc.copy(bounds = cc.bounds.moveTo(newPosition))
        case cc: Box[_]    => cc.copy(bounds = cc.bounds.moveTo(newPosition))
    def moveTo(x: Double, y: Double): Collider[Tag] =
      moveTo(Vertex(x, y))
    def moveTo(newPosition: Vector2): Collider[Tag] =
      moveTo(Vertex.fromVector2(newPosition))

    def hitTest(other: Collider[Tag]): Boolean =
      (c, other) match
        case (a: Collider.Circle[_], b: Collider.Circle[_]) =>
          a.bounds.overlaps(b.bounds)

        case (a: Collider.Circle[_], b: Collider.Box[_]) =>
          a.bounds.overlaps(b.bounds)

        case (a: Collider.Box[_], b: Collider.Circle[_]) =>
          a.bounds.overlaps(b.bounds)

        case (a: Collider.Box[_], b: Collider.Box[_]) =>
          a.bounds.overlaps(b.bounds)

    def displacementWith(other: Collider[Tag]): Displacement =
      Displacement.calculate(c, other)

    def reflect(ray: LineSegment): Option[ReflectionData] =
      c match
        case c: Circle[?] => c.bounds.reflect(ray)
        case c: Box[?]    => c.bounds.reflect(ray)

    def ~==(other: Collider[Tag])(using CanEqual[Tag, Tag]): Boolean =
      (c, other) match
        case (a: Collider.Circle[Tag], b: Collider.Circle[Tag]) =>
          (a.tag == b.tag) &&
          (a.bounds ~== b.bounds) &&
          (a.mass ~== b.mass) &&
          (a.velocity ~== b.velocity) &&
          (a.terminalVelocity ~== b.terminalVelocity) &&
          (a.restitution ~== b.restitution) &&
          (a.friction ~== b.friction) &&
          a.static == b.static

        case (a: Collider.Box[Tag], b: Collider.Box[Tag]) =>
          (a.tag == b.tag) &&
          (a.bounds ~== b.bounds) &&
          (a.mass ~== b.mass) &&
          (a.velocity ~== b.velocity) &&
          (a.terminalVelocity ~== b.terminalVelocity) &&
          (a.restitution ~== b.restitution) &&
          (a.friction ~== b.friction) &&
          a.static == b.static

        case _ =>
          false
