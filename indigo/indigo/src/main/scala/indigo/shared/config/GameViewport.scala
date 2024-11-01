package indigo.shared.config

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Size

/** Respresents the initial size of the game's viewport.
  *
  * @param width
  *   Width in pixels
  * @param height
  *   Height in pixels
  */
final case class GameViewport(size: Size) derives CanEqual:
  val width: Int            = size.width
  val height: Int           = size.height
  val horizontalMiddle: Int = width / 2
  val verticalMiddle: Int   = height / 2
  val center: Point         = Point(horizontalMiddle, verticalMiddle)

  @deprecated("use 'toRectangle' instead")
  def asRectangle: Rectangle =
    toRectangle
  def toRectangle: Rectangle =
    Rectangle(Point.zero, size)

  def toPoint: Point =
    size.toPoint

  def toSize: Size =
    size

  def bounds: Rectangle =
    toRectangle

  def giveDimensions(magnification: Int): Rectangle =
    Rectangle(0, 0, width / magnification, height / magnification)

object GameViewport:

  def apply(width: Int, height: Int): GameViewport =
    GameViewport(Size(width, height))

  val atWUXGA: GameViewport =
    GameViewport(1920, 1200)
  val atWUXGABy2: GameViewport =
    GameViewport(960, 600)

  val at1080p: GameViewport =
    GameViewport(1920, 1080)
  val at1080pBy2: GameViewport =
    GameViewport(960, 540)

  val at720p: GameViewport =
    GameViewport(1280, 720)
  val at720pBy2: GameViewport =
    GameViewport(640, 360)
