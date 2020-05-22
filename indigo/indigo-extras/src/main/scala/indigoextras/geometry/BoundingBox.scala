package indigoextras.geometry

import indigo.shared.{AsString, EqualTo}
import indigo.shared.AsString._

import scala.annotation.tailrec
import indigo.shared.datatypes.Rectangle

final class BoundingBox(val position: Vertex, val size: Vertex) {
  val x: Double      = position.x
  val y: Double      = position.y
  val width: Double  = size.x
  val height: Double = size.y
  val hash: String   = s"${x.show}${y.show}${width.show}${height.show}"

  val left: Double   = x
  val right: Double  = x + width
  val top: Double    = y
  val bottom: Double = y + height

  def topLeft: Vertex     = Vertex(left, top)
  def topRight: Vertex    = Vertex(right, top)
  def bottomRight: Vertex = Vertex(right, bottom)
  def bottomLeft: Vertex  = Vertex(left, bottom)

  def corners: List[Vertex] =
    List(topLeft, topRight, bottomRight, bottomLeft)

  def isVertexWithin(pt: Vertex): Boolean =
    pt.x >= left && pt.x < right && pt.y >= top && pt.y < bottom

  def isVertexWithin(x: Double, y: Double): Boolean = isVertexWithin(Vertex(x, y))

  def +(rect: BoundingBox): BoundingBox = BoundingBox(x + rect.x, y + rect.y, width + rect.width, height + rect.height)
  def +(i: Double): BoundingBox         = BoundingBox(x + i, y + i, width + i, height + i)
  def -(rect: BoundingBox): BoundingBox = BoundingBox(x - rect.x, y - rect.y, width - rect.width, height - rect.height)
  def -(i: Double): BoundingBox         = BoundingBox(x - i, y - i, width - i, height - i)
  def *(rect: BoundingBox): BoundingBox = BoundingBox(x * rect.x, y * rect.y, width * rect.width, height * rect.height)
  def *(i: Double): BoundingBox         = BoundingBox(x * i, y * i, width * i, height * i)
  def /(rect: BoundingBox): BoundingBox = BoundingBox(x / rect.x, y / rect.y, width / rect.width, height / rect.height)
  def /(i: Double): BoundingBox         = BoundingBox(x / i, y / i, width / i, height / i)

  def expandToInclude(other: BoundingBox): BoundingBox =
    BoundingBox.expandToInclude(this, other)

  def intersects(other: BoundingBox): Boolean =
    BoundingBox.intersecting(this, other)

  def encompasses(other: BoundingBox): Boolean =
    BoundingBox.encompassing(this, other)

  def overlaps(other: BoundingBox): Boolean =
    BoundingBox.overlapping(this, other)

  def moveBy(point: Vertex): BoundingBox =
    BoundingBox(x + point.x, y + point.y, width, height)

  def moveTo(point: Vertex): BoundingBox =
    BoundingBox(point, size)

  def resize(point: Vertex): BoundingBox =
    BoundingBox(position, point)

  def toRectangle: Rectangle =
    Rectangle(position.toPoint, size.toPoint)

  def asString: String =
    implicitly[AsString[BoundingBox]].show(this)

  def ===(other: BoundingBox): Boolean =
    implicitly[EqualTo[BoundingBox]].equal(this, other)
}

object BoundingBox {

  val zero: BoundingBox = BoundingBox(0, 0, 0, 0)

  def apply(position: Vertex, size: Vertex): BoundingBox =
    new BoundingBox(position, size)

  def apply(x: Double, y: Double, width: Double, height: Double): BoundingBox =
    BoundingBox(Vertex(x, y), Vertex(width, height))

  def unapply(rectangle: BoundingBox): Option[(Vertex, Vertex)] =
    Option((rectangle.position, rectangle.size))

  def fromTwoVertexs(pt1: Vertex, pt2: Vertex): BoundingBox = {
    val x = Math.min(pt1.x, pt2.x)
    val y = Math.min(pt1.y, pt2.y)
    val w = Math.max(pt1.x, pt2.x) - x
    val h = Math.max(pt1.y, pt2.y) - y

    BoundingBox(x, y, w, h)
  }

  def fromVertices(vertices: List[Vertex]): BoundingBox = {
    @tailrec
    def rec(remaining: List[Vertex], left: Double, top: Double, right: Double, bottom: Double): BoundingBox =
      remaining match {
        case Nil =>
          BoundingBox(left, top, right - left, bottom - top)

        case p :: ps =>
          rec(
            ps,
            Math.min(left, p.x),
            Math.min(top, p.y),
            Math.max(right, p.x),
            Math.max(bottom, p.y)
          )
      }

    rec(vertices, Double.MaxValue, Double.MaxValue, Double.MinValue, Double.MinValue)
  }

  implicit val rectangleShow: AsString[BoundingBox] =
    AsString.create(p => s"""BoundingBox(Position(${p.x.show}, ${p.y.show}), Size(${p.width.show}, ${p.height.show}))""")

  implicit val rectangleEqualTo: EqualTo[BoundingBox] = {
    val eq = implicitly[EqualTo[Vertex]]

    EqualTo.create { (a, b) =>
      eq.equal(a.position, b.position) && eq.equal(a.size, b.size)
    }
  }

  def expandToInclude(a: BoundingBox, b: BoundingBox): BoundingBox = {
    val newX: Double = if (a.left < b.left) a.left else b.left
    val newY: Double = if (a.top < b.top) a.top else b.top

    BoundingBox(
      x = newX,
      y = newY,
      width = (if (a.right > b.right) a.right else b.right) - newX,
      height = (if (a.bottom > b.bottom) a.bottom else b.bottom) - newY
    )
  }

  def intersecting(a: BoundingBox, b: BoundingBox): Boolean =
    b.corners.exists(p => a.isVertexWithin(p))

  def encompassing(a: BoundingBox, b: BoundingBox): Boolean =
    b.corners.forall(p => a.isVertexWithin(p))

  def overlapping(a: BoundingBox, b: BoundingBox): Boolean =
    intersecting(a, b) || intersecting(b, a) || encompassing(a, b) || encompassing(b, a)

}
