package indigo.gameengine

final class GameTime(val running: Double, val delta: Double, val targetFPS: GameTime.FPS) {
  lazy val frameDuration: Double = 1000d / targetFPS.asDouble
  lazy val multiplier: Double    = delta / frameDuration

  def intByTime(value: Int): Int          = (value * multiplier).toInt
  def floatByTime(value: Float): Float    = (value * multiplier).toFloat
  def doubleByTime(value: Double): Double = value * multiplier

  def setTargetFPS(fps: Int): GameTime =
    new GameTime(running, delta, GameTime.FPS(fps))
}

object GameTime {

  def now: GameTime                                       = new GameTime(System.currentTimeMillis().toDouble, 0, FPS.Default)
  def zero: GameTime                                      = new GameTime(0, 0, FPS.Default)
  def is(running: Double): GameTime                       = new GameTime(running, 0, FPS.Default)
  def withDelta(running: Double, delta: Double): GameTime = new GameTime(running, delta, FPS.Default)

  def apply(running: Double, delta: Double, targetFPS: FPS): GameTime =
    new GameTime(running, delta, targetFPS)

  final class FPS(val value: Int) extends AnyVal {
    def asDouble: Double = value.toDouble
  }
  object FPS {

    val `30`: FPS = FPS(30)
    val `60`: FPS = FPS(60)
    val Default: FPS = `30`

    def apply(value: Int): FPS =
      new FPS(value)
  }
}
