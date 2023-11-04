package indigo.physics

import indigo.*
import indigo.physics.Displacement
import indigo.physics.Friction
import indigo.physics.Mass
import indigo.physics.Restitution
import indigo.shared.geometry.ReflectionData

enum Collider[A]:
  def tag: A
  def static: Boolean
  def velocity: Vector2
  def mass: Mass
  def restitution: Restitution
  def canCollideWith: A => Boolean
  def onCollisionWith: Collider[A] => Batch[GlobalEvent]

  case Circle(
      tag: A,
      bounds: BoundingCircle,
      mass: Mass,
      velocity: Vector2,
      restitution: Restitution,
      friction: Friction,
      static: Boolean,
      canCollideWith: A => Boolean,
      onCollisionWith: Collider[A] => Batch[GlobalEvent]
  ) extends Collider[A]
  case Box(
      tag: A,
      bounds: BoundingBox,
      mass: Mass,
      velocity: Vector2,
      restitution: Restitution,
      friction: Friction,
      static: Boolean,
      canCollideWith: A => Boolean,
      onCollisionWith: Collider[A] => Batch[GlobalEvent]
  ) extends Collider[A]

object Collider:

  def apply[A](tag: A, bounds: BoundingCircle | BoundingBox): Collider[A] =
    bounds match
      case b: BoundingCircle => Collider.Circle(tag, b)
      case b: BoundingBox    => Collider.Box(tag, b)

  object Circle:
    def apply[A](tag: A, bounds: BoundingCircle): Collider.Circle[A] =
      Collider.Circle(
        tag,
        bounds,
        Mass.default,
        Vector2.zero,
        Restitution.default,
        Friction.zero,
        false,
        _ => true,
        _ => Batch.empty
      )

  object Box:
    def apply[A](tag: A, bounds: BoundingBox): Collider.Box[A] =
      Collider.Box(
        tag,
        bounds,
        Mass.default,
        Vector2.zero,
        Restitution.default,
        Friction.zero,
        false,
        _ => true,
        _ => Batch.empty
      )

  extension [A](c: Collider.Circle[A])
    def toBox: Collider.Box[A] =
      Collider.Box(
        c.tag,
        c.bounds.toIncircleBoundingBox,
        c.mass,
        c.velocity,
        c.restitution,
        c.friction,
        c.static,
        _ => true,
        _ => Batch.empty
      )

  extension [A](c: Collider[A])
    def isStatic: Boolean = c.static

    def position: Vertex =
      c match
        case cc: Circle[_] => cc.bounds.position
        case cc: Box[_]    => cc.bounds.position

    def center: Vertex =
      c match
        case cc: Circle[_] => cc.bounds.center
        case cc: Box[_]    => cc.bounds.center

    def makeStatic: Collider[A] = c match
      case cc: Circle[_] => cc.copy(static = true)
      case cc: Box[_]    => cc.copy(static = true)

    def makeDynamic: Collider[A] = c match
      case cc: Circle[_] => cc.copy(static = false)
      case cc: Box[_]    => cc.copy(static = false)

    def withFriction(value: Friction): Collider[A] =
      c match
        case cc: Circle[_] => cc.copy(friction = value)
        case cc: Box[_]    => cc.copy(friction = value)

    def withMass(value: Mass): Collider[A] =
      c match
        case cc: Circle[_] => cc.copy(mass = value)
        case cc: Box[_]    => cc.copy(mass = value)

    def withRestitution(value: Restitution): Collider[A] =
      c match
        case cc: Circle[_] => cc.copy(restitution = value)
        case cc: Box[_]    => cc.copy(restitution = value)

    def withVelocity(value: Vector2): Collider[A] =
      c match
        case cc: Circle[_] => cc.copy(velocity = value)
        case cc: Box[_]    => cc.copy(velocity = value)
    def withVelocity(x: Double, y: Double): Collider[A] =
      withVelocity(Vector2(x, y))

    def withPosition(value: Vertex): Collider[A] =
      c match
        case cc: Circle[_] => cc.copy(bounds = cc.bounds.moveTo(value))
        case cc: Box[_]    => cc.copy(bounds = cc.bounds.moveTo(value))
    def withPosition(x: Double, y: Double): Collider[A] =
      withPosition(Vertex(x, y))

    def collidesWith(tagTest: A => Boolean): Collider[A] =
      c match
        case cc: Circle[_] => cc.copy(canCollideWith = tagTest)
        case cc: Box[_]    => cc.copy(canCollideWith = tagTest)

    def onCollision(produceEvents: Collider[A] => Batch[GlobalEvent]): Collider[A] =
      c match
        case cc: Circle[_] => cc.copy(onCollisionWith = produceEvents)
        case cc: Box[_]    => cc.copy(onCollisionWith = produceEvents)

    def +(value: Vector2): Collider[A] =
      withVelocity(c.velocity + value)
    def -(value: Vector2): Collider[A] =
      withVelocity(c.velocity - value)
    def *(value: Vector2): Collider[A] =
      withVelocity(c.velocity * value)
    def /(value: Vector2): Collider[A] =
      withVelocity(c.velocity / value)

    def moveBy(amount: Vertex): Collider[A] =
      c match
        case cc: Circle[_] => cc.copy(bounds = cc.bounds.moveBy(amount))
        case cc: Box[_]    => cc.copy(bounds = cc.bounds.moveBy(amount))
    def moveBy(x: Double, y: Double): Collider[A] =
      moveBy(Vertex(x, y))
    def moveBy(amount: Vector2): Collider[A] =
      moveBy(Vertex.fromVector2(amount))

    def moveTo(newPosition: Vertex): Collider[A] =
      c match
        case cc: Circle[_] => cc.copy(bounds = cc.bounds.moveTo(newPosition))
        case cc: Box[_]    => cc.copy(bounds = cc.bounds.moveTo(newPosition))
    def moveTo(x: Double, y: Double): Collider[A] =
      moveTo(Vertex(x, y))
    def moveTo(newPosition: Vector2): Collider[A] =
      moveTo(Vertex.fromVector2(newPosition))

    def hitTest(other: Collider[A]): Boolean =
      (c, other) match
        case (a: Collider.Circle[_], b: Collider.Circle[_]) =>
          a.bounds.overlaps(b.bounds)

        case (a: Collider.Circle[_], b: Collider.Box[_]) =>
          a.bounds.overlaps(b.bounds)

        case (a: Collider.Box[_], b: Collider.Circle[_]) =>
          a.bounds.overlaps(b.bounds)

        case (a: Collider.Box[_], b: Collider.Box[_]) =>
          a.bounds.overlaps(b.bounds)

    def displacementWith(other: Collider[A]): Displacement =
      Displacement.calculate(c, other)

    def reflect(ray: LineSegment): Option[ReflectionData] =
      c match
        case Circle(_, bounds, _, _, _, _, _, _, _) => bounds.reflect(ray)
        case Box(_, bounds, _, _, _, _, _, _, _)    => bounds.reflect(ray)

    def ~==(other: Collider[A])(using CanEqual[A, A]): Boolean =
      (c, other) match
        case (Collider.Circle(aT, a, aM, aV, aR, aF, aS, _, _), Collider.Circle(bT, b, bM, bV, bR, bF, bS, _, _)) =>
          (aT == bT) && (a ~== b) && (aM ~== bM) && (aV ~== bV) && (aR ~== bR) && (aF ~== bF) && aS == bS

        case (Collider.Box(aT, a, aM, aV, aR, aF, aS, _, _), Collider.Box(bT, b, bM, bV, bR, bF, bS, _, _)) =>
          (aT == bT) && (a ~== b) && (aM ~== bM) && (aV ~== bV) && (aR ~== bR) && (aF ~== bF) && aS == bS

        case _ =>
          false
