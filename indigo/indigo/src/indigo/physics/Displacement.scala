package indigo.physics

import indigo.*
import indigo.physics.Mass

final case class Displacement(amount: Double, normal: Vector2, contact: LineSegment, massA: Mass, massB: Option[Mass]):
  val displaceBy: Vector2 =
    massB match
      case None =>
        normal * amount.toDouble

      case Some(mb) =>
        val m = (Mass.one / (massA + mb)) * massA
        normal * (m * amount).toDouble

  val displaceAmount: Double =
    displaceBy.magnitude

  def ~==(other: Displacement): Boolean =
    Math.abs(amount - other.amount) < 0.0001 &&
      (normal ~== other.normal) &&
      (contact ~== other.contact) &&
      (massA ~== other.massA) &&
      (massB.isEmpty && other.massB.isEmpty || (massB.isDefined && other.massB.isDefined && (massB.get ~== other.massB.get)))

object Displacement:

  val boxBoxCornerThreshold: Double = 0.001

  def calculate[A](ca: Collider[A], cb: Collider[A]): Displacement =
    (ca, cb) match
      case (a: Collider.Circle[_], b: Collider.Circle[_]) =>
        calculateDisplacement(a, b)

      case (a: Collider.Circle[_], b: Collider.Box[_]) =>
        calculateDisplacement(a, b)

      case (a: Collider.Box[_], b: Collider.Circle[_]) =>
        calculateDisplacement(a, b)

      case (a: Collider.Box[_], b: Collider.Box[_]) =>
        calculateDisplacement(a, b)

  def calculateDisplacement(a: Collider.Circle[?], b: Vertex, bMass: Option[Mass]): Displacement =
    val amount  = a.bounds.position.distanceTo(b)
    val normal  = (a.bounds.center - b).toVector2.normalise
    val contact = LineSegment(a.bounds.center, b)

    Displacement(amount, normal, contact, a.mass, bMass)

  def calculateDisplacement(a: Collider.Circle[?], b: Collider.Circle[?]): Displacement =
    val amount  = a.bounds.radius + b.bounds.radius - a.bounds.position.distanceTo(b.bounds.position)
    val normal  = (a.bounds.center - b.bounds.center).toVector2.normalise
    val contact = LineSegment(a.bounds.center, b.bounds.center)

    Displacement(amount, normal, contact, a.mass, if b.isStatic then None else Option(b.mass))

  def calculateDisplacement(circle: Collider.Circle[?], box: Collider.Box[?]): Displacement =
    val shortestSide = Math.min(box.bounds.halfSize.x, box.bounds.halfSize.y)
    val innerBounds  = box.bounds.contract(shortestSide)

    val moveableCenter =
      Vertex(
        x = Math.min(innerBounds.right, Math.max(innerBounds.left, circle.bounds.x)),
        y = Math.min(innerBounds.bottom, Math.max(innerBounds.top, circle.bounds.y))
      )

    val boxCenterToCircle = circle.bounds.sdf(moveableCenter)
    val normal            = (circle.bounds.center - moveableCenter).toVector2.normalise
    val contact           = LineSegment(circle.bounds.center, moveableCenter)

    box.bounds.lineIntersectsAt(contact).map { impact =>
      Math.abs(moveableCenter.distanceTo(impact) - boxCenterToCircle)
    } match
      case None =>
        calculateDisplacement(circle.toBox, box)

      case Some(amount) =>
        Displacement(amount, normal, contact, circle.mass, if box.isStatic then None else Option(box.mass))

  def calculateDisplacement(box: Collider.Box[?], circle: Collider.Circle[?]): Displacement =
    val circleCenterToBox = box.bounds.sdf(circle.bounds.center)
    val normal            = (box.bounds.center - circle.bounds.center).toVector2.normalise
    val contact           = LineSegment(box.bounds.center, circle.bounds.center)

    circle.bounds
      .lineIntersectsAt(contact)
      .nearest
      .map(v => Math.abs(circle.bounds.center.distanceTo(v) - circleCenterToBox)) match
      case None =>
        calculateDisplacement(box, circle.toBox)

      case Some(amount) =>
        Displacement(amount, normal, contact, box.mass, if circle.isStatic then None else Option(circle.mass))

  def calculateDisplacement(a: Collider.Box[?], b: Collider.Box[?]): Displacement =
    val aMidX = a.bounds.center.x
    val aMidY = a.bounds.center.y
    val bMidX = b.bounds.center.x
    val bMidY = b.bounds.center.y

    val dx = (bMidX - aMidX) / b.bounds.halfSize.x
    val dy = (bMidY - aMidY) / b.bounds.halfSize.y

    val absDX = Math.abs(dx)
    val absDY = Math.abs(dy)

    if Math.abs(absDX - absDY) < boxBoxCornerThreshold then {
      val amount =
        Vector2(
          if dx < 0 then a.bounds.left - b.bounds.right else a.bounds.right - b.bounds.left,
          if dy < 0 then a.bounds.top - b.bounds.bottom else a.bounds.bottom - b.bounds.top
        ).magnitude

      val normal =
        Vector2(
          if dx < 0 then 1 else -1,
          if dy < 0 then 1 else -1
        )

      val corner =
        if dx < 0 && dy < 0 then b.bounds.corners(2) // bottom right
        else if dx < 0 then b.bounds.corners(1)      // top right
        else if dy < 0 then b.bounds.corners(3)      // bottom left
        else b.bounds.corners(0)                     // top left

      Displacement(
        amount = Math.abs(amount),
        normal = normal,
        contact = LineSegment(corner, corner + normal),
        massA = a.mass,
        massB = if b.isStatic then None else Option(b.mass)
      )

    } else if absDX > absDY then {

      if dx < 0 then
        val normal = Vector2(1, 0)
        val start  = Vertex(b.bounds.right, a.bounds.center.y)

        Displacement(
          amount = Math.abs(a.bounds.left - b.bounds.right),
          normal = normal,
          contact = LineSegment(
            start,
            start + normal
          ),
          massA = a.mass,
          massB = if b.isStatic then None else Option(b.mass)
        )
      else
        val normal = Vector2(-1, 0)
        val start  = Vertex(b.bounds.left, a.bounds.center.y)

        Displacement(
          amount = Math.abs(a.bounds.right - b.bounds.left),
          normal = normal,
          contact = LineSegment(
            start,
            start + normal
          ),
          massA = a.mass,
          massB = if b.isStatic then None else Option(b.mass)
        )

    } else {

      if dy < 0 then
        val normal = Vector2(0, 1)
        val start  = Vertex(a.bounds.center.x, b.bounds.bottom)

        Displacement(
          amount = Math.abs(a.bounds.top - b.bounds.bottom),
          normal = normal,
          contact = LineSegment(
            start,
            start + normal
          ),
          massA = a.mass,
          massB = if b.isStatic then None else Option(b.mass)
        )
      else
        val normal = Vector2(0, -1)
        val start  = Vertex(a.bounds.center.x, b.bounds.top)

        Displacement(
          amount = Math.abs(a.bounds.bottom - b.bounds.top),
          normal = normal,
          contact = LineSegment(
            start,
            start + normal
          ),
          massA = a.mass,
          massB = if b.isStatic then None else Option(b.mass)
        )
    }
