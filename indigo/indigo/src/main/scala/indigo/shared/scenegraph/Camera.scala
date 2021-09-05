package indigo.shared.scenegraph

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians

sealed trait Camera:
  def position: Point
  def zoom: Zoom
  def rotation: Radians
  def isLookAt: Boolean
  def withZoom(newZoom: Zoom): Camera
  def rotateTo(angle: Radians): Camera
  def rotateBy(angle: Radians): Camera
  def withRotation(newRotation: Radians): Camera

object Camera:

  final case class Fixed(position: Point, zoom: Zoom, rotation: Radians) extends Camera:
    val isLookAt: Boolean = false

    def withX(newX: Int): Fixed =
      this.copy(position = position.withX(newX))
    def withY(newY: Int): Fixed =
      this.copy(position = position.withY(newY))

    def moveTo(newPosition: Point): Fixed =
      this.copy(position = newPosition)
    def moveTo(x: Int, y: Int): Fixed =
      moveTo(Point(x, y))

    def moveBy(amount: Point): Fixed =
      this.copy(position = position + amount)
    def moveBy(x: Int, y: Int): Fixed =
      moveBy(Point(x, y))

    def withZoom(newZoom: Zoom): Fixed =
      this.copy(zoom = newZoom)

    def rotateTo(angle: Radians): Fixed =
      this.copy(rotation = angle)
    def rotateBy(angle: Radians): Fixed =
      rotateTo(rotation + angle)
    def withRotation(newRotation: Radians): Fixed =
      rotateTo(newRotation)

    def toLookAt: LookAt =
      LookAt(position, zoom, rotation)

  final case class LookAt(target: Point, zoom: Zoom, rotation: Radians) extends Camera:
    val isLookAt: Boolean = true
    val position: Point   = target

    def withTarget(newTarget: Point): LookAt =
      this.copy(target = newTarget)
    def lookAt(newTarget: Point): LookAt =
      withTarget(newTarget)

    def withZoom(newZoom: Zoom): LookAt =
      this.copy(zoom = newZoom)

    def rotateTo(angle: Radians): LookAt =
      this.copy(rotation = angle)
    def rotateBy(angle: Radians): LookAt =
      rotateTo(rotation + angle)
    def withRotation(newRotation: Radians): LookAt =
      rotateTo(newRotation)

    def toFixed: Fixed =
      Fixed(position, zoom, rotation)

  def default: Camera =
    Fixed(Point.zero, Zoom.x1, Radians.zero)

  given CanEqual[Fixed, Fixed]                   = CanEqual.derived
  given CanEqual[LookAt, LookAt]                 = CanEqual.derived
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
