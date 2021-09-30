package indigo.shared.datatypes

final case class Vector2(x: Double, y: Double) derives CanEqual:

  def withX(newX: Double): Vector2 =
    this.copy(x = newX)

  def withY(newY: Double): Vector2 =
    this.copy(y = newY)

  def abs: Vector2 =
    Vector2(Math.abs(x), Math.abs(y))

  def min(other: Vector2): Vector2 =
    Vector2(Math.min(other.x, x), Math.min(other.y, y))
  def min(value: Double): Vector2 =
    Vector2(Math.min(value, x), Math.min(value, y))

  def max(other: Vector2): Vector2 =
    Vector2(Math.max(other.x, x), Math.max(other.y, y))
  def max(value: Double): Vector2 =
    Vector2(Math.max(value, x), Math.max(value, y))

  def clamp(min: Double, max: Double): Vector2 =
    Vector2(Math.min(max, Math.max(min, x)), Math.min(max, Math.max(min, y)))

  def length: Double =
    distanceTo(Vector2.zero)

  def invert: Vector2 =
    Vector2(-x, -y)

  def translate(vec: Vector2): Vector2 =
    Vector2.add(this, vec)

  def moveTo(newPosition: Vector2): Vector2 =
    newPosition
  def moveTo(x: Double, y: Double): Vector2 =
    moveTo(Vector2(x, y))

  def moveBy(amount: Vector2): Vector2 =
    Vector2.add(this, amount)
  def moveBy(x: Double, y: Double): Vector2 =
    moveBy(Vector2(x, y))

  def scaleBy(vec: Vector2): Vector2 =
    Vector2.multiply(this, vec)
  def scaleBy(amount: Double): Vector2 =
    scaleBy(Vector2(amount))

  def rotateBy(angle: Radians): Vector2 = {
    val a = angle.wrap.toDouble
    val s = Math.sin(a)
    val c = Math.cos(a)

    Vector2(
      this.x * c - this.y * s,
      this.x * s + this.y * c
    )
  }
  def rotateBy(angle: Radians, origin: Vector2): Vector2 = {
    Vector2.add(
      Vector2.subtract(this, origin).rotateBy(angle),
      origin
    )
  }

  def round: Vector2 =
    Vector2(Math.round(x).toDouble, Math.round(y).toDouble)

  def toList: List[Double] =
    List(x, y)

  def +(other: Vector2): Vector2 = Vector2.add(this, other)
  def -(other: Vector2): Vector2 = Vector2.subtract(this, other)
  def *(other: Vector2): Vector2 = Vector2.multiply(this, other)
  def /(other: Vector2): Vector2 = Vector2.divide(this, other)

  def +(value: Double): Vector2 = Vector2.add(this, Vector2(value, value))
  def -(value: Double): Vector2 = Vector2.subtract(this, Vector2(value, value))
  def *(value: Double): Vector2 = Vector2.multiply(this, Vector2(value, value))
  def /(value: Double): Vector2 = Vector2.divide(this, Vector2(value, value))

  def dot(other: Vector2): Double =
    Vector2.dotProduct(this, other)

  def normalise: Vector2 = {
    val magnitude = length

    if (magnitude == 0) Vector2.zero
    else
      Vector2(
        x / magnitude,
        y / magnitude
      )
  }

  def distanceTo(other: Vector2): Double =
    Vector2.distance(this, other)

  def toPoint: Point =
    Point(x.toInt, y.toInt)

  def transform(matrix3: Matrix3): Vector2 =
    matrix3.transform(this)

  def transform(matrix4: Matrix4): Vector2 =
    matrix4.transform(toVector3).toVector2

  def toVector3: Vector3 =
    Vector3(x, y, 1)

  def toVector4: Vector4 =
    Vector4(x, y, 1, 1)

  def ~==(other: Vector2): Boolean =
    Math.abs(x - other.x) < 0.0001 &&
      Math.abs(y - other.y) < 0.0001

object Vector2:

  def apply(d: Double): Vector2 =
    Vector2(d, d)

  val zero: Vector2     = Vector2(0d, 0d)
  val one: Vector2      = Vector2(1d, 1d)
  val minusOne: Vector2 = Vector2(-1d, -1d)

  def fromPoints(start: Point, end: Point): Vector2 =
    Vector2((end.x - start.x).toDouble, (end.y - start.y).toDouble)

  def fromPoint(point: Point): Vector2 =
    Vector2(point.x.toDouble, point.y.toDouble)

  @inline def add(vec1: Vector2, vec2: Vector2): Vector2 =
    Vector2(vec1.x + vec2.x, vec1.y + vec2.y)

  @inline def subtract(vec1: Vector2, vec2: Vector2): Vector2 =
    Vector2(vec1.x - vec2.x, vec1.y - vec2.y)

  @inline def multiply(vec1: Vector2, vec2: Vector2): Vector2 =
    Vector2(vec1.x * vec2.x, vec1.y * vec2.y)

  @inline def divide(vec1: Vector2, vec2: Vector2): Vector2 =
    Vector2(vec1.x / vec2.x, vec1.y / vec2.y)

  def dotProduct(vec1: Vector2, vec2: Vector2): Double =
    (vec1.x * vec2.x) + (vec1.y * vec2.y)

  def distance(v1: Vector2, v2: Vector2): Double =
    Math.sqrt(Math.abs(Math.pow(v2.x - v1.x, 2) + Math.pow(v2.y - v1.y, 2)))
