package indigo.shared.geometry

import indigo.shared.collections.Batch
import indigo.shared.datatypes.Circle
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Size
import indigo.shared.datatypes.Vector2

import scala.annotation.tailrec

final case class BoundingBox(position: Vertex, size: Vertex) derives CanEqual:
  lazy val x: Double      = position.x
  lazy val y: Double      = position.y
  lazy val width: Double  = size.x
  lazy val height: Double = size.y

  lazy val left: Double   = if width >= 0 then x else x + width
  lazy val right: Double  = if width >= 0 then x + width else x
  lazy val top: Double    = if height >= 0 then y else y + height
  lazy val bottom: Double = if height >= 0 then y + height else y

  lazy val horizontalCenter: Double = x + (width / 2)
  lazy val verticalCenter: Double   = y + (height / 2)

  lazy val topLeft: Vertex     = Vertex(left, top)
  lazy val topRight: Vertex    = Vertex(right, top)
  lazy val bottomRight: Vertex = Vertex(right, bottom)
  lazy val bottomLeft: Vertex  = Vertex(left, bottom)
  lazy val center: Vertex      = Vertex(horizontalCenter, verticalCenter)
  lazy val halfSize: Vertex    = (size / 2).abs

  lazy val corners: Batch[Vertex] =
    Batch(topLeft, topRight, bottomRight, bottomLeft)

  def contains(vertex: Vertex): Boolean =
    vertex.x >= left && vertex.x < right && vertex.y >= top && vertex.y < bottom
  def contains(vector: Vector2): Boolean =
    contains(Vertex.fromVector2(vector))
  def contains(x: Double, y: Double): Boolean =
    contains(Vertex(x, y))

  def +(rect: BoundingBox): BoundingBox = BoundingBox(x + rect.x, y + rect.y, width + rect.width, height + rect.height)
  def +(d: Double): BoundingBox         = BoundingBox(x + d, y + d, width + d, height + d)
  def -(rect: BoundingBox): BoundingBox = BoundingBox(x - rect.x, y - rect.y, width - rect.width, height - rect.height)
  def -(d: Double): BoundingBox         = BoundingBox(x - d, y - d, width - d, height - d)
  def *(rect: BoundingBox): BoundingBox = BoundingBox(x * rect.x, y * rect.y, width * rect.width, height * rect.height)
  def *(d: Double): BoundingBox         = BoundingBox(x * d, y * d, width * d, height * d)
  def /(rect: BoundingBox): BoundingBox = BoundingBox(x / rect.x, y / rect.y, width / rect.width, height / rect.height)
  def /(d: Double): BoundingBox         = BoundingBox(x / d, y / d, width / d, height / d)

  def sdf(vertex: Vertex): Double =
    val p: Vertex = vertex - center
    val d: Vertex = p.abs - halfSize
    d.max(0.0).length + Math.min(Math.max(d.x, d.y), 0.0)
  def sdf(vector: Vector2): Double =
    sdf(Vertex.fromVector2(vector))

  def distanceToBoundary(vertex: Vertex): Double =
    sdf(vertex)
  def distanceToBoundary(vector: Vector2): Double =
    sdf(Vertex.fromVector2(vector))

  def expand(amount: Double): BoundingBox =
    BoundingBox.expand(this, amount)
  def expand(amount: Vector2): BoundingBox =
    BoundingBox.expand(this, amount)

  def expandToInclude(other: BoundingBox): BoundingBox =
    BoundingBox.expandToInclude(this, other)

  def contract(amount: Double): BoundingBox =
    BoundingBox.contract(this, amount)
  def contract(amount: Vector2): BoundingBox =
    BoundingBox.contract(this, amount)

  def encompasses(other: BoundingBox): Boolean =
    BoundingBox.encompassing(this, other)
  def encompasses(other: BoundingCircle): Boolean =
    BoundingBox.encompassing(this, other)

  def overlaps(other: BoundingBox): Boolean =
    BoundingBox.overlapping(this, other)
  def overlaps(other: BoundingCircle): Boolean =
    contains(other.position) || Math.abs(sdf(other.position)) <= Math.abs(other.radius)
  def overlaps(other: LineSegment): Boolean =
    contains(other.start) || contains(other.end) || lineIntersects(other)

  def moveBy(amount: Vertex): BoundingBox =
    this.copy(position = position + amount)
  def moveBy(x: Double, y: Double): BoundingBox =
    moveBy(Vertex(x, y))
  def moveBy(amount: Vector2): BoundingBox =
    moveBy(Vertex.fromVector2(amount))

  def moveTo(newPosition: Vertex): BoundingBox =
    this.copy(position = newPosition)
  def moveTo(x: Double, y: Double): BoundingBox =
    moveTo(Vertex(x, y))
  def moveTo(newPosition: Vector2): BoundingBox =
    moveTo(Vertex.fromVector2(newPosition))

  def resize(newSize: Vertex): BoundingBox =
    this.copy(size = newSize)
  def resize(newSize: Vector2): BoundingBox =
    resize(Vertex.fromVector2(newSize))
  def resize(x: Double, y: Double): BoundingBox =
    resize(Vertex(x, y))
  def resize(value: Double): BoundingBox =
    resize(Vertex(value))

  def resizeBy(amount: Vertex): BoundingBox =
    this.copy(size = size + amount)
  def resizeBy(amount: Vector2): BoundingBox =
    resizeBy(Vertex.fromVector2(amount))
  def resizeBy(x: Double, y: Double): BoundingBox =
    resizeBy(Vertex(x, y))
  def resizeBy(amount: Double): BoundingBox =
    resizeBy(Vertex(amount))

  def toIncircle: Circle =
    Circle.incircle(this.toRectangle)
  def toCircumcircle: Circle =
    Circle.circumcircle(this.toRectangle)

  def toSquare: BoundingBox =
    this.copy(size = Vertex(Math.max(size.x, size.y)))

  def toRectangle: Rectangle =
    Rectangle(position.toPoint, Size(size.x.toInt, size.y.toInt))

  def toBoundingIncircle: BoundingCircle =
    BoundingCircle.incircle(this)
  def toBoundingCircumcircle: BoundingCircle =
    BoundingCircle.circumcircle(this)

  def toLineSegments: Batch[LineSegment] =
    BoundingBox.toLineSegments(this)

  def lineIntersects(line: LineSegment): Boolean =
    BoundingBox.lineIntersects(this, line)

  def lineIntersectsAt(line: LineSegment): Option[Vertex] =
    BoundingBox.lineIntersectsAt(this, line)

  /** Reflects the incoming 'ray' off of the BoundingBox
    */
  def reflect(ray: LineSegment): Option[ReflectionData] =
    BoundingBox.lineIntersectsWithEdge(this, ray).flatMap { case (_, line) =>
      line.reflect(ray)
    }

  def ~==(other: BoundingBox): Boolean =
    (position ~== other.position) && (size ~== other.size)

object BoundingBox:

  val zero: BoundingBox =
    BoundingBox(0, 0, 0, 0)

  def apply(x: Double, y: Double, width: Double, height: Double): BoundingBox =
    BoundingBox(Vertex(x, y), Vertex(width, height))

  def apply(width: Double, height: Double): BoundingBox =
    BoundingBox(Vertex.zero, Vertex(width, height))

  def apply(size: Vertex): BoundingBox =
    BoundingBox(Vertex.zero, size)

  def fromTwoVertices(pt1: Vertex, pt2: Vertex): BoundingBox = {
    val x = Math.min(pt1.x, pt2.x)
    val y = Math.min(pt1.y, pt2.y)
    val w = Math.max(pt1.x, pt2.x) - x
    val h = Math.max(pt1.y, pt2.y) - y

    BoundingBox(x, y, w, h)
  }

  def fromVertices(vertices: Batch[Vertex]): BoundingBox = {
    val margin: Double = 0.001
    @tailrec
    def rec(remaining: List[Vertex], left: Double, top: Double, right: Double, bottom: Double): BoundingBox =
      remaining match {
        case Nil =>
          BoundingBox(left, top, right - left + margin, bottom - top + margin)

        case p :: ps =>
          rec(
            ps,
            Math.min(left, p.x),
            Math.min(top, p.y),
            Math.max(right, p.x),
            Math.max(bottom, p.y)
          )
      }

    rec(vertices.toList, Double.MaxValue, Double.MaxValue, Double.MinValue, Double.MinValue)
  }

  /** Produces a bounding box that could include all of the vertices. Since the `contains` methods right and bottom
    * checks are < not <= (to allow bounds to sit next to each other with no overlap), a small fixed margin of 0.001 is
    * add to the size values.
    */
  def fromVertexCloud(vertices: Batch[Vertex]): BoundingBox =
    fromVertices(vertices)

  def fromRectangle(rectangle: Rectangle): BoundingBox =
    BoundingBox(
      Vertex.fromPoint(rectangle.position),
      Vertex(rectangle.size.width.toDouble, rectangle.size.height.toDouble)
    )

  def fromIncircle(boundingCircle: BoundingCircle): BoundingBox =
    BoundingBox(Vertex(boundingCircle.left, boundingCircle.top), Vertex(boundingCircle.diameter))
  def fromIncircle(circle: Circle): BoundingBox =
    fromIncircle(circle.toBoundingCircle)

  def fromCircumcircle(boundingCircle: BoundingCircle): BoundingBox =
    val sideLength = (boundingCircle.diameter * Math.sqrt(2)) / 2
    BoundingBox(boundingCircle.center - (sideLength / 2), Vertex(sideLength))
  def fromCircumcircle(circle: Circle): BoundingBox =
    fromCircumcircle(circle.toBoundingCircle)

  def toLineSegments(boundingBox: BoundingBox): Batch[LineSegment] =
    Batch(
      LineSegment(boundingBox.topLeft, boundingBox.bottomLeft),
      LineSegment(boundingBox.bottomLeft, boundingBox.bottomRight),
      LineSegment(boundingBox.bottomRight, boundingBox.topRight),
      LineSegment(boundingBox.topRight, boundingBox.topLeft)
    )

  def expand(boundingBox: BoundingBox, amount: Double): BoundingBox =
    BoundingBox(
      x = if boundingBox.width >= 0 then boundingBox.x - amount else boundingBox.x + amount,
      y = if boundingBox.height >= 0 then boundingBox.y - amount else boundingBox.y + amount,
      width = if boundingBox.width >= 0 then boundingBox.width + (amount * 2) else boundingBox.width - (amount * 2),
      height = if boundingBox.height >= 0 then boundingBox.height + (amount * 2) else boundingBox.height - (amount * 2)
    )

  def expand(boundingBox: BoundingBox, amount: Vector2): BoundingBox =
    BoundingBox(
      x = if boundingBox.width >= 0 then boundingBox.x - amount.x else boundingBox.x + amount.x,
      y = if boundingBox.height >= 0 then boundingBox.y - amount.y else boundingBox.y + amount.y,
      width = if boundingBox.width >= 0 then boundingBox.width + (amount.x * 2) else boundingBox.width - (amount.x * 2),
      height =
        if boundingBox.height >= 0 then boundingBox.height + (amount.y * 2) else boundingBox.height - (amount.y * 2)
    )

  def expandToInclude(a: BoundingBox, b: BoundingBox): BoundingBox =
    val newX: Double = if (a.left < b.left) a.left else b.left
    val newY: Double = if (a.top < b.top) a.top else b.top

    BoundingBox(
      x = newX,
      y = newY,
      width = (if (a.right > b.right) a.right else b.right) - newX,
      height = (if (a.bottom > b.bottom) a.bottom else b.bottom) - newY
    )

  def contract(boundingBox: BoundingBox, amount: Double): BoundingBox =
    BoundingBox(
      x = if boundingBox.width >= 0 then boundingBox.x + amount else boundingBox.x - amount,
      y = if boundingBox.height >= 0 then boundingBox.y + amount else boundingBox.y - amount,
      width = if boundingBox.width >= 0 then boundingBox.width - (amount * 2) else boundingBox.width + (amount * 2),
      height = if boundingBox.height >= 0 then boundingBox.height - (amount * 2) else boundingBox.height + (amount * 2)
    )

  def contract(boundingBox: BoundingBox, amount: Vector2): BoundingBox =
    BoundingBox(
      x = if boundingBox.width >= 0 then boundingBox.x + amount.x else boundingBox.x - amount.x,
      y = if boundingBox.height >= 0 then boundingBox.y + amount.y else boundingBox.y - amount.y,
      width = if boundingBox.width >= 0 then boundingBox.width - (amount.x * 2) else boundingBox.width + (amount.x * 2),
      height =
        if boundingBox.height >= 0 then boundingBox.height - (amount.y * 2) else boundingBox.height + (amount.y * 2)
    )

  def encompassing(a: BoundingBox, b: BoundingBox): Boolean =
    b.x >= a.x && b.y >= a.y && (b.width + (b.x - a.x)) <= a.width && (b.height + (b.y - a.y)) <= a.height
  def encompassing(a: BoundingBox, b: BoundingCircle): Boolean =
    encompassing(a, b.toIncircleBoundingBox)

  def overlapping(a: BoundingBox, b: BoundingBox): Boolean =
    Math.abs(a.center.x - b.center.x) < a.halfSize.x + b.halfSize.x &&
      Math.abs(a.center.y - b.center.y) < a.halfSize.y + b.halfSize.y

  def lineIntersects(boundingBox: BoundingBox, line: LineSegment): Boolean =
    @tailrec
    def rec(remaining: List[LineSegment]): Boolean =
      remaining match
        case Nil =>
          false

        case x :: _ if x.intersectsWith(line) =>
          true

        case _ :: xs =>
          rec(xs)

    val containsStart = boundingBox.contains(line.start)
    val containsEnd   = boundingBox.contains(line.end)

    if containsStart && containsEnd then false
    else if !line.toBoundingBox.overlaps(boundingBox) then false
    else rec(boundingBox.toLineSegments.toList)

  def lineIntersectsAt(boundingBox: BoundingBox, line: LineSegment): Option[Vertex] =
    lineIntersectsWithEdge(boundingBox, line).map(_._1)

  def lineIntersectsWithEdge(boundingBox: BoundingBox, line: LineSegment): Option[(Vertex, LineSegment)] =
    val containsStart = boundingBox.contains(line.start)
    val containsEnd   = boundingBox.contains(line.end)

    if containsStart && containsEnd then None
    else if !line.toBoundingBox.overlaps(boundingBox) then None
    else
      val outside =
        if !containsStart && !containsEnd then line.start
        else if containsStart then line.end
        else line.start

      boundingBox.toLineSegments
        .flatMap { ln =>
          if ln.isFacingVertex(outside) then
            val at = ln.intersectsAt(line)
            if at.isDefined then Batch(at.get -> ln) else Batch.empty
          else Batch.empty
        }
        .sortWith((a, b) => a._1.distanceTo(outside) < b._1.distanceTo(outside))
        .headOption
