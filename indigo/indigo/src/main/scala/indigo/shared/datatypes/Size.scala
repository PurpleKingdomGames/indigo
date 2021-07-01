package indigo.shared.datatypes

final case class Size(width: Int, height: Int) derives CanEqual:
  def +(size: Size): Size = Size(width + size.width, height + size.height)
  def +(i: Int): Size     = Size(width + i, height + i)
  def -(size: Size): Size = Size(width - size.width, height - size.height)
  def -(i: Int): Size     = Size(width - i, height - i)
  def *(size: Size): Size = Size(width * size.width, height * size.height)
  def *(i: Int): Size     = Size(width * i, height * i)
  def /(size: Size): Size = Size(width / size.width, height / size.height)
  def /(i: Int): Size     = Size(width / i, height / i)

  def withWidth(newX: Int): Size  = this.copy(width = newX)
  def withHeight(newY: Int): Size = this.copy(height = newY)

  def abs: Size =
    Size(Math.abs(width), Math.abs(height))

  def min(other: Size): Size =
    Size(Math.min(other.width, width), Math.min(other.height, height))
  def min(value: Int): Size =
    Size(Math.min(value, width), Math.min(value, height))

  def max(other: Size): Size =
    Size(Math.max(other.width, width), Math.max(other.height, height))
  def max(value: Int): Size =
    Size(Math.max(value, width), Math.max(value, height))

  def clamp(min: Int, max: Int): Size =
    Size(Math.min(max, Math.max(min, width)), Math.min(max, Math.max(min, height)))

  def invert: Size =
    Size(-width, -height)

  def resizeBy(amount: Size): Size =
    this + amount
  def resizeBy(width: Int, height: Int): Size =
    resizeBy(Size(width, height))

  def toVector: Vector2 =
    Vector2(width.toDouble, height.toDouble)

  def toPoint: Point =
    Point(width, height)

object Size:

  given CanEqual[Option[Size], Option[Size]] = CanEqual.derived

  def apply(xy: Int): Size =
    Size(xy, xy)

  val zero: Size = Size(0, 0)

  def tuple2ToSize(t: (Int, Int)): Size = Size(t._1, t._2)
