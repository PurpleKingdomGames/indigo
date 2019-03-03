package indigo.gameengine

final class GameTime(val running: Double, val delta: Double, val targetFPS: Int) {
  lazy val frameDuration: Double = 1000d / targetFPS.toDouble
  lazy val multiplier: Double    = delta / frameDuration

  def intByTime(value: Int): Int          = (value * multiplier).toInt
  def floatByTime(value: Float): Float    = (value * multiplier).toFloat
  def doubleByTime(value: Double): Double = value * multiplier

  def setTargetFPS(fps: Int): GameTime =
    new GameTime(running, delta, fps)
}

object GameTime {

  val DefaultFPS: Int = 30

  def now: GameTime                                       = new GameTime(System.currentTimeMillis().toDouble, 0, DefaultFPS)
  def zero: GameTime                                      = new GameTime(0, 0, DefaultFPS)
  def is(running: Double): GameTime                       = new GameTime(running, 0, DefaultFPS)
  def withDelta(running: Double, delta: Double): GameTime = new GameTime(running, delta, DefaultFPS)
}
