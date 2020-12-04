package indigoextras.geometry

import indigo.shared.EqualTo
import indigo.shared.datatypes.Vector2

final case class BoundingCircle(position: Vertex, radius: Double) {
  lazy val x: Double        = position.x
  lazy val y: Double        = position.y
  lazy val diameter: Double = radius * 2

  lazy val left: Double   = x - radius
  lazy val right: Double  = x + radius
  lazy val top: Double    = y - radius
  lazy val bottom: Double = y + radius

  def toBoundingBox: BoundingBox =
    BoundingBox(Vertex(left, top), Vertex(diameter, diameter))

  def contains(vertex: Vertex): Boolean =
    vertex.distanceTo(position) <= radius

  def contains(x: Double, y: Double): Boolean =
    contains(Vertex(x, y))

  def +(d: Double): BoundingCircle = resize(radius + d)
  def -(d: Double): BoundingCircle = resize(radius - d)
  def *(d: Double): BoundingCircle = resize(radius * d)
  def /(d: Double): BoundingCircle = resize(radius / d)

  def sdf(vertex: Vertex): Double =
    BoundingCircle.signedDistanceFunction(vertex - position, radius)
  def distanceToBoundary(vertex: Vertex): Double =
    sdf(vertex)

  def expandToInclude(other: BoundingCircle): BoundingCircle =
    BoundingCircle.expandToInclude(this, other)

  def encompasses(other: BoundingCircle): Boolean =
    BoundingCircle.encompassing(this, other)

  def overlaps(other: BoundingCircle): Boolean =
    BoundingCircle.overlapping(this, other)

  def moveBy(amount: Vertex): BoundingCircle =
    this.copy(position = position + amount)
  def moveBy(x: Double, y: Double): BoundingCircle =
    moveBy(Vertex(x, y))

  def moveTo(newPosition: Vertex): BoundingCircle =
    this.copy(position = newPosition)
  def moveTo(x: Double, y: Double): BoundingCircle =
    moveTo(Vertex(x, y))

  def resize(newRadius: Double): BoundingCircle =
    this.copy(radius = newRadius)

  def lineIntersects(line: LineSegment): Boolean =
    BoundingCircle.lineIntersects(this, line)

  def lineIntersectsAt(line: LineSegment): Option[Vertex] =
    BoundingCircle.lineIntersectsAt(this, line)

  def ===(other: BoundingCircle): Boolean =
    implicitly[EqualTo[BoundingCircle]].equal(this, other)

  @SuppressWarnings(Array("org.wartremover.warts.IsInstanceOf", "org.wartremover.warts.AsInstanceOf"))
  override def equals(obj: Any): Boolean =
    if (obj.isInstanceOf[BoundingCircle])
      this === obj.asInstanceOf[BoundingCircle]
    else false
}

object BoundingCircle {

  val zero: BoundingCircle =
    BoundingCircle(Vertex.zero, 0)

  def apply(x: Double, y: Double, radius: Double): BoundingCircle =
    BoundingCircle(Vertex(x, y), radius)

  // def fromTwoVertices(pt1: Vertex, pt2: Vertex): BoundingBox = {
  //   val x = Math.min(pt1.x, pt2.x)
  //   val y = Math.min(pt1.y, pt2.y)
  //   val w = Math.max(pt1.x, pt2.x) - x
  //   val h = Math.max(pt1.y, pt2.y) - y

  //   BoundingBox(x, y, w, h)
  // }

  // def fromVertices(vertices: List[Vertex]): BoundingBox = {
  //   @tailrec
  //   def rec(remaining: List[Vertex], left: Double, top: Double, right: Double, bottom: Double): BoundingBox =
  //     remaining match {
  //       case Nil =>
  //         BoundingBox(left, top, right - left, bottom - top)

  //       case p :: ps =>
  //         rec(
  //           ps,
  //           Math.min(left, p.x),
  //           Math.min(top, p.y),
  //           Math.max(right, p.x),
  //           Math.max(bottom, p.y)
  //         )
  //     }

  //   rec(vertices, Double.MaxValue, Double.MaxValue, Double.MinValue, Double.MinValue)
  // }

  // def fromVertexCloud(vertices: List[Vertex]): BoundingBox =
  //   fromVertices(vertices)

  implicit val bcEqualTo: EqualTo[BoundingCircle] = {
    val eq = implicitly[EqualTo[Vertex]]

    EqualTo.create { (a, b) =>
      eq.equal(a.position, b.position) && EqualTo.eqDouble.equal(a.radius, b.radius)
    }
  }

  def expandToInclude(a: BoundingCircle, b: BoundingCircle): BoundingCircle =
    a.resize(a.position.distanceTo(b.position) + Math.abs(b.radius))

  def encompassing(a: BoundingCircle, b: BoundingCircle): Boolean =
    a.position.distanceTo(b.position) < Math.abs(a.radius) - Math.abs(b.radius)

  def overlapping(a: BoundingCircle, b: BoundingCircle): Boolean =
    a.position.distanceTo(b.position) < Math.abs(a.radius) + Math.abs(b.radius)

  def lineIntersects(boundingCircle: BoundingCircle, line: LineSegment): Boolean =
    Math.abs(line.sdf(boundingCircle.position)) <= boundingCircle.radius

  def lineIntersectsAt(boundingCircle: BoundingCircle, line: LineSegment): Option[Vertex] = {
    val a: Vector2 = (boundingCircle.position - line.start).toVector2
    val b: Vector2 = (line.end - line.start).toVector2
    val c = a.dot(b) / b.length

    val d = (a * c) + line.start.toVector2 //

    if(d.distanceTo(boundingCircle.position.toVector2) <= boundingCircle.radius) {
      //hit
      ???
    } else None
  }

  // Centered at the origin
  def signedDistanceFunction(point: Vertex, radius: Double): Double =
    point.length - radius

}
