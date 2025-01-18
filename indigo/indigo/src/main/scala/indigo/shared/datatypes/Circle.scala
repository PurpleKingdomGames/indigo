package indigo.shared.datatypes

import indigo.shared.collections.Batch
import indigo.shared.geometry.BoundingBox
import indigo.shared.geometry.BoundingCircle

final case class Circle(position: Point, radius: Int) derives CanEqual:
  lazy val x: Int        = position.x
  lazy val y: Int        = position.y
  lazy val diameter: Int = radius * 2

  lazy val left: Int   = x - radius
  lazy val right: Int  = x + radius
  lazy val top: Int    = y - radius
  lazy val bottom: Int = y + radius

  lazy val center: Point = position

  def contains(vertex: Point): Boolean =
    vertex.distanceTo(position) <= radius
  def contains(x: Int, y: Int): Boolean =
    contains(Point(x, y))
  def contains(vector: Vector2): Boolean =
    contains(vector.toPoint)

  def +(d: Int): Circle = resize(radius + d)
  def -(d: Int): Circle = resize(radius - d)
  def *(d: Int): Circle = resize(radius * d)
  def /(d: Int): Circle = resize(radius / d)

  def sdf(vertex: Point): Int =
    Circle.signedDistanceFunction(vertex - position, radius)
  def sdf(vector: Vector2): Int =
    sdf(vector.toPoint)

  def distanceToBoundary(vertex: Point): Int =
    sdf(vertex)
  def distanceToBoundary(vector: Vector2): Int =
    sdf(vector.toPoint)

  def expandToInclude(other: Circle): Circle =
    Circle.expandToInclude(this, other)

  def encompasses(other: Circle): Boolean =
    Circle.encompassing(this, other)
  def encompasses(other: Rectangle): Boolean =
    Circle.encompassing(this, other)

  def overlaps(other: Circle): Boolean =
    Circle.overlapping(this, other)
  def overlaps(other: Rectangle): Boolean =
    Circle.overlapping(this, other)

  def moveBy(amount: Point): Circle =
    this.copy(position = position + amount)
  def moveBy(x: Int, y: Int): Circle =
    moveBy(Point(x, y))
  def moveBy(amount: Vector2): Circle =
    moveBy(amount.toPoint)

  def moveTo(newPosition: Point): Circle =
    this.copy(position = newPosition)
  def moveTo(x: Int, y: Int): Circle =
    moveTo(Point(x, y))
  def moveTo(newPosition: Vector2): Circle =
    moveTo(newPosition.toPoint)

  def resize(newRadius: Int): Circle =
    this.copy(radius = newRadius)
  def resizeTo(newRadius: Int): Circle =
    resize(newRadius)
  def resizeBy(amount: Int): Circle =
    expand(amount)
  def withRadius(newRadius: Int): Circle =
    resize(newRadius)
  def expand(by: Int): Circle =
    resize(radius + by)
  def contract(by: Int): Circle =
    resize(radius - by)

  @deprecated("Please use `toIncircleRectangle`, or alternatively `toCircumcircleRectangle`.")
  def toRectangle: Rectangle =
    Rectangle.fromIncircle(this)
  def toIncircleRectangle: Rectangle =
    Rectangle.fromIncircle(this)
  def toCircumcircleRectangle: Rectangle =
    Rectangle.fromCircumcircle(this)

  def toBoundingCircle: BoundingCircle =
    BoundingCircle.fromCircle(this)

  @deprecated("Please use `toIncircleBoundingBox`, or alternatively `toCircumcircleBoundingBox`.")
  def toBoundingBox: BoundingBox =
    BoundingBox.fromCircle(this)
  def toIncircleBoundingBox: BoundingBox =
    BoundingBox.fromIncircle(this)
  def toCircumcircleBoundingBox: BoundingBox =
    BoundingBox.fromCircumcircle(this)

object Circle:

  val zero: Circle =
    Circle(Point.zero, 0)

  def apply(x: Int, y: Int, radius: Int): Circle =
    Circle(Point(x, y), radius)

  def fromTwoPoints(center: Point, boundary: Point): Circle =
    Circle(center, center.distanceTo(boundary).toInt)

  def fromPoint(points: Batch[Point]): Circle =
    val bb = Rectangle.fromPointCloud(points)
    Circle(bb.center, bb.center.distanceTo(bb.topLeft).toInt)

  def fromPointCloud(points: Batch[Point]): Circle =
    fromPoint(points)

  @deprecated("Please use `Circle.incircle`, or alternatively `Circle.circumcircle`.")
  def fromRectangle(rectangle: Rectangle): Circle =
    incircle(rectangle)

  /** Creates a `Circle` from a square (rectangles are squared off by the max width/height) where the circle fits inside
    * the square.
    */
  def incircle(rectangle: Rectangle): Circle =
    Circle(rectangle.center, Math.max(rectangle.halfSize.width, rectangle.halfSize.height))

  /** Creates a `Circle` from a square (rectangles are squared off by the max width/height) such that all of the corners
    * lie on the circle's circumference.
    */
  def circumcircle(rectangle: Rectangle): Circle =
    val r = rectangle.toSquare
    Circle(r.center, r.center.distanceTo(r.topLeft).toInt)

  /** Creates a `Circle` from three points such that all of the points lie on the circle's circumference.
    */
  def circumcircle(a: Point, b: Point, c: Point): Option[Circle] =
    BoundingCircle.circumcircle(a.toVertex, b.toVertex, c.toVertex).map(_.toCircle)

  def expandToInclude(a: Circle, b: Circle): Circle =
    a.resize((a.position.distanceTo(b.position) + Math.abs(b.radius)).toInt)

  def encompassing(a: Circle, b: Circle): Boolean =
    a.position.distanceTo(b.position) <= Math.abs(a.radius) - Math.abs(b.radius)
  def encompassing(a: Circle, b: Rectangle): Boolean =
    b.corners.forall(a.contains)

  def overlapping(a: Circle, b: Circle): Boolean =
    a.toBoundingCircle.overlaps(b.toIncircleBoundingBox)
  def overlapping(a: Circle, b: Rectangle): Boolean =
    a.toBoundingCircle.overlaps(b.toBoundingBox)

  // Centered at the origin
  def signedDistanceFunction(point: Point, radius: Int): Int =
    (point.toVector.length - radius).toInt
