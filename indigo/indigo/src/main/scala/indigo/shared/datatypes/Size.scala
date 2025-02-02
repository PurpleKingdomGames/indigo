package indigo.shared.datatypes

import indigo.shared.dice.Dice
import indigo.shared.geometry.Vertex

final case class Size(width: Int, height: Int) derives CanEqual:
  def +(size: Size): Size = Size(width + size.width, height + size.height)
  def +(i: Int): Size     = Size(width + i, height + i)
  def +(d: Double): Size  = Size((width.toDouble + d).toInt, (height.toDouble + d).toInt)
  def -(size: Size): Size = Size(width - size.width, height - size.height)
  def -(i: Int): Size     = Size(width - i, height - i)
  def -(d: Double): Size  = Size((width.toDouble - d).toInt, (height.toDouble - d).toInt)
  def *(size: Size): Size = Size(width * size.width, height * size.height)
  def *(i: Int): Size     = Size(width * i, height * i)
  def *(d: Double): Size  = Size((width.toDouble * d).toInt, (height.toDouble * d).toInt)
  def /(size: Size): Size = Size(width / size.width, height / size.height)
  def /(i: Int): Size     = Size(width / i, height / i)
  def /(d: Double): Size  = Size((width.toDouble / d).toInt, (height.toDouble / d).toInt)
  def %(pt: Size): Size   = Size.mod(this, pt)
  def %(i: Int): Size     = Size.mod(this, Size(i))
  def %(d: Double): Size  = Size.mod(this, Size(d.toInt))

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

  def toVertex: Vertex =
    Vertex(width.toDouble, height.toDouble)

object Size:

  given CanEqual[Option[Size], Option[Size]] = CanEqual.derived

  def apply(xy: Int): Size =
    Size(xy, xy)

  val zero: Size = Size(0, 0)
  val one: Size  = Size(1, 1)

  def tuple2ToSize(t: (Int, Int)): Size = Size(t._1, t._2)

  def fromPoint(point: Point): Size =
    Size(point.x, point.y)

  def fromVector2(vector2: Vector2): Size =
    Size(vector2.x.toInt, vector2.y.toInt)

  def fromVertex(vertex: Vertex): Size =
    Size(vertex.x.toInt, vertex.y.toInt)

  def mod(dividend: Size, divisor: Size): Size =
    Size(
      width = (dividend.width   % divisor.width + divisor.width)   % divisor.width,
      height = (dividend.height % divisor.height + divisor.height) % divisor.height
    )

  def random(dice: Dice, max: Int): Size =
    Size(
      if max <= 0 then 0 else dice.rollFromZero(max),
      if max <= 0 then 0 else dice.rollFromZero(max)
    )

  def random(dice: Dice, max: Size): Size =
    Size(
      if max.width <= 0 then 0 else dice.rollFromZero(max.width),
      if max.height <= 0 then 0 else dice.rollFromZero(max.height)
    )

  def random(dice: Dice, min: Int, max: Int): Size =
    Size(dice.rollFromZero(max - min) + min, dice.rollFromZero(max - min) + min)

  def random(dice: Dice, min: Size, max: Size): Size =
    Size(dice.rollFromZero(max.width - min.width) + min.width, dice.rollFromZero(max.height - min.height) + min.height)
