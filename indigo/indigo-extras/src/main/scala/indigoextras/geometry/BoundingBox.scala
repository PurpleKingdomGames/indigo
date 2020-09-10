package indigoextras.geometry

import indigo.shared.EqualTo

import scala.annotation.tailrec
import indigo.shared.datatypes.Rectangle
import indigoextras.geometry.IntersectionResult.IntersectionVertex

final case class BoundingBox(position: Vertex, size: Vertex) {
  val x: Double         = position.x
  val y: Double         = position.y
  val width: Double     = size.x
  val height: Double    = size.y
  lazy val hash: String = s"${x.toString()}${y.toString()}${width.toString()}${height.toString()}"

  lazy val left: Double   = x
  lazy val right: Double  = x + width
  lazy val top: Double    = y
  lazy val bottom: Double = y + height

  lazy val horizontalCenter: Double = x + (width / 2)
  lazy val verticalCenter: Double   = y + (height / 2)

  lazy val topLeft: Vertex     = Vertex(left, top)
  lazy val topRight: Vertex    = Vertex(right, top)
  lazy val bottomRight: Vertex = Vertex(right, bottom)
  lazy val bottomLeft: Vertex  = Vertex(left, bottom)
  lazy val center: Vertex      = Vertex(horizontalCenter, verticalCenter)
  lazy val halfSize: Vertex    = size / 2

  lazy val corners: List[Vertex] =
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

  def encompasses(other: BoundingBox): Boolean =
    BoundingBox.encompassing(this, other)

  def overlaps(other: BoundingBox): Boolean =
    BoundingBox.overlapping(this, other)

  def moveBy(amount: Vertex): BoundingBox =
    this.copy(position = position + amount)

  def moveTo(newPosition: Vertex): BoundingBox =
    this.copy(position = newPosition)

  def resize(newSize: Vertex): BoundingBox =
    this.copy(size = newSize)

  def toRectangle: Rectangle =
    Rectangle(position.toPoint, size.toPoint)

  def toLineSegments: List[LineSegment] =
    BoundingBox.toLineSegments(this)

  def lineIntersectsAt(line: LineSegment): Option[Vertex] =
    BoundingBox.lineIntersectsAt(this, line)

  def ===(other: BoundingBox): Boolean =
    implicitly[EqualTo[BoundingBox]].equal(this, other)

  @SuppressWarnings(Array("org.wartremover.warts.IsInstanceOf", "org.wartremover.warts.AsInstanceOf"))
  override def equals(obj: Any): Boolean =
    if (obj.isInstanceOf[BoundingBox])
      this === obj.asInstanceOf[BoundingBox]
    else false
}

object BoundingBox {

  val zero: BoundingBox = BoundingBox(0, 0, 0, 0)

  def apply(x: Double, y: Double, width: Double, height: Double): BoundingBox =
    BoundingBox(Vertex(x, y), Vertex(width, height))

  def fromTwoVertices(pt1: Vertex, pt2: Vertex): BoundingBox = {
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

  def fromVertexCloud(vertices: List[Vertex]): BoundingBox =
    fromVertices(vertices)

  def toLineSegments(boundingBox: BoundingBox): List[LineSegment] =
    List(
      LineSegment(boundingBox.topLeft, boundingBox.bottomLeft),
      LineSegment(boundingBox.bottomLeft, boundingBox.bottomRight),
      LineSegment(boundingBox.bottomRight, boundingBox.topRight),
      LineSegment(boundingBox.topRight, boundingBox.topLeft)
    )

  implicit val bbEqualTo: EqualTo[BoundingBox] = {
    val eq = implicitly[EqualTo[Vertex]]

    EqualTo.create { (a, b) =>
      eq.equal(a.position, b.position) && eq.equal(a.size, b.size)
    }
  }

  def expand(boundingBox: BoundingBox, amount: Double): BoundingBox =
    BoundingBox(
      x = boundingBox.x - amount,
      y = boundingBox.y - amount,
      width = boundingBox.width + (amount * 2),
      height = boundingBox.height + (amount * 2)
    )

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

  def encompassing(a: BoundingBox, b: BoundingBox): Boolean =
    b.x >= a.x && b.y >= a.y && (b.width + (b.x - a.x)) <= a.width && (b.height + (b.y - a.y)) <= a.height

  def overlapping(a: BoundingBox, b: BoundingBox): Boolean =
    Math.abs(a.center.x - b.center.x) < a.halfSize.x + b.halfSize.x && Math.abs(a.center.y - b.center.y) < a.halfSize.y + b.halfSize.y

  def lineIntersectsAt(boundingBox: BoundingBox, line: LineSegment): Option[Vertex] = {
    val verts =
      boundingBox.toLineSegments
        .flatMap { bbLine =>
          bbLine.intersectWith(line) match {
            case r @ IntersectionVertex(_, _) =>
              val v = r.toVertex

              if (line.containsVertex(v) && bbLine.containsVertex(v))
                Some(v)
              else
                None
            case _ =>
              None
          }
        }

    verts
      .foldLeft((Option.empty[Vertex], Double.MaxValue)) { (acc, v) =>
        val dist = v.distanceTo(line.start)
        if (dist < acc._2) (Some(v), dist)
        else acc
      }
      ._1
  }

}
