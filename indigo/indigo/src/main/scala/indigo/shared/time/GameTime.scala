package indigo.shared.time

final case class GameTime(running: Seconds, delta: Seconds, targetFPS: GameTime.FPS) derives CanEqual:
  lazy val frameDuration: Millis = Millis((1000d / targetFPS.asDouble).toLong)
  lazy val multiplier: Double    = delta.toDouble / frameDuration.toDouble

  def intByTime(value: Int): Int          = (value * multiplier).toInt
  def floatByTime(value: Float): Float    = (value * multiplier).toFloat
  def doubleByTime(value: Double): Double = value * multiplier

  def setTargetFPS(fps: Int): GameTime =
    this.copy(targetFPS = GameTime.FPS(fps))

object GameTime:

  def zero: GameTime =
    GameTime(Seconds.zero, Seconds.zero, FPS.Default)

  def is(running: Seconds): GameTime =
    GameTime(running, Seconds.zero, FPS.Default)

  def withDelta(running: Seconds, delta: Seconds): GameTime =
    GameTime(running, delta, FPS.Default)

  opaque type FPS = Int

  object FPS:
    val `30`: FPS    = FPS(30)
    val `60`: FPS    = FPS(60)
    val Default: FPS = `30`

    def apply(fps: Int): FPS = fps

    extension (fps: FPS)
      def asLong: Long     = fps.toLong
      def asDouble: Double = fps.toDouble
  end FPS
