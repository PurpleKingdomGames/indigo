package indigo.shared.scenegraph

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Size

/** Parent type of camera instances. Cameras are used to look around your games graphics / levels / scenes.
  */
sealed trait Camera:
  def position: Point
  def topLeft(viewport: Size): Point
  def bounds(viewport: Size): Rectangle
  def frustum(viewport: Size): Rectangle = bounds(viewport)
  def zoom: Zoom
  def rotation: Radians
  def isLookAt: Boolean
  def withZoom(newZoom: Zoom): Camera
  def rotateTo(angle: Radians): Camera
  def rotateBy(angle: Radians): Camera
  def withRotation(newRotation: Radians): Camera

object Camera:

  /** Indigo's default camera is fixed. It starts at position 0,0 and shows you everything down and right from there
    * until it runs out of screen. Fixed cameras are useful for replicating the behaviour of Indigos normal windowing
    * while controlling the position, zoom and rotation.
    */
  final case class Fixed(position: Point, zoom: Zoom, rotation: Radians) extends Camera:
    def topLeft(viewport: Size): Point    = position
    def bounds(viewport: Size): Rectangle = Rectangle(position, viewport)
    val isLookAt: Boolean                 = false

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

  object Fixed:
    def apply(position: Point): Fixed =
      Fixed(position, Zoom.x1, Radians.zero)

    def apply(position: Point, zoom: Zoom): Fixed =
      Fixed(position, zoom, Radians.zero)

  /** LookAt cameras center the screen on whatever position they are looking at. Useful for following a players
    * character, for example.
    */
  final case class LookAt(target: Point, zoom: Zoom, rotation: Radians) extends Camera:
    val isLookAt: Boolean = true
    val position: Point   = target
    def topLeft(viewport: Size): Point =
      target - (viewport.toPoint / 2) / zoom.toDouble.toInt
    def bounds(viewport: Size): Rectangle =
      Rectangle(topLeft(viewport), viewport)

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

  object LookAt:
    def apply(target: Point): LookAt =
      LookAt(target, Zoom.x1, Radians.zero)

    def apply(target: Point, zoom: Zoom): LookAt =
      LookAt(target, zoom, Radians.zero)

  def default: Fixed =
    Fixed(Point.zero, Zoom.x1, Radians.zero)

  given CanEqual[Fixed, Fixed]                   = CanEqual.derived
  given CanEqual[LookAt, LookAt]                 = CanEqual.derived
  given CanEqual[Option[Camera], Option[Camera]] = CanEqual.derived

/** Zoom your camera in and out! Behaves like physical camera's zoom, so x2 means "make everything twice as big". Unlike
  * a real camera, you can zoom in or our infinitely!
  */
opaque type Zoom = Double
object Zoom:
  inline def apply(amount: Double): Zoom = amount

  val x025: Zoom = Zoom(0.25)
  val x05: Zoom  = Zoom(0.5)
  val x1: Zoom   = Zoom(1.0)
  val x2: Zoom   = Zoom(2.0)
  val x3: Zoom   = Zoom(3.0)
  val x4: Zoom   = Zoom(4.0)

  extension (z: Zoom) def toDouble: Double = z
