package indigo.shared.config

final case class GameViewport(width: Int, height: Int) {
  val horizontalMiddle: Int = width / 2
  val verticalMiddle: Int   = height / 2
  val center: (Int, Int)    = (horizontalMiddle, verticalMiddle)
}
object GameViewport {
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
}