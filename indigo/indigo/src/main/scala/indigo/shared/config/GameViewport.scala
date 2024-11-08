package indigo.shared.config

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Size

opaque type GameViewport = Size
object GameViewport:
  def apply(size: Size): GameViewport              = size
  def apply(width: Int, height: Int): GameViewport = Size(width, height)

  val atWUXGA: GameViewport    = GameViewport(1920, 1200)
  val atWUXGABy2: GameViewport = GameViewport(960, 600)

  val at1080p: GameViewport    = GameViewport(1920, 1080)
  val at1080pBy2: GameViewport = GameViewport(960, 540)

  val at720p: GameViewport    = GameViewport(1280, 720)
  val at720pBy2: GameViewport = GameViewport(640, 360)

  extension (a: GameViewport)
    def width: Int  = a.width
    def height: Int = a.height

    def horizontalMiddle: Int = a.width / 2
    def verticalMiddle: Int   = a.height / 2
    def center: Point         = Point(horizontalMiddle, verticalMiddle)

    def toRectangle: Rectangle = Rectangle(Point.zero, a)
    def size: Size             = a
    def toSize: Size           = a
    def bounds: Rectangle      = toRectangle
    def giveDimensions(magnification: Int): Rectangle =
      Rectangle(0, 0, a.width / magnification, a.height / magnification)
