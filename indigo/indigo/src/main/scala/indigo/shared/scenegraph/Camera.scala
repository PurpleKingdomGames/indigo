package indigo.shared.scenegraph

import indigo.shared.datatypes.Point

final case class Camera(position: Point, zoom: Zoom):

  def withX(newX: Int): Camera =
    this.copy(position = position.withX(newX))
  def withY(newY: Int): Camera =
    this.copy(position = position.withY(newY))

  def moveTo(newPosition: Point): Camera =
    this.copy(position = newPosition)
  def moveTo(x: Int, y: Int): Camera =
    moveTo(Point(x, y))

  def moveBy(amount: Point): Camera =
    this.copy(position = position + amount)
  def moveBy(x: Int, y: Int): Camera =
    moveBy(Point(x, y))

  def withZoom(newZoom: Zoom): Camera =
    this.copy(zoom = newZoom)

object Camera:
  def default: Camera =
    Camera(Point.zero, Zoom.x1)

  given CanEqual[Camera, Camera]                 = CanEqual.derived
  given CanEqual[Option[Camera], Option[Camera]] = CanEqual.derived

opaque type Zoom = Double
object Zoom:
  def apply(amount: Double): Zoom = amount

  val x025: Zoom = Zoom(0.25)
  val x05: Zoom  = Zoom(0.5)
  val x1: Zoom   = Zoom(1.0)
  val x2: Zoom   = Zoom(2.0)
  val x3: Zoom   = Zoom(3.0)
  val x4: Zoom   = Zoom(4.0)

  extension (z: Zoom) def toDouble: Double = z
