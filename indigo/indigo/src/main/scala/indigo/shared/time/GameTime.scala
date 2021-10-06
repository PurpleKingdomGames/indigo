package indigo.shared.time

/** An instance of `GameTime` is present on every frame, and the values it holds do not change during that frame. This
  * allows for "synchronous" programming, where it is assumed that everything happens at the exact same time during the
  * current frame. The most commonly used fields (e.g. for animation) are the running time of the game and the time delta since the
  * last frame.
  */
final case class GameTime(running: Seconds, delta: Seconds, targetFPS: GameTime.FPS) derives CanEqual:
  lazy val frameDuration: Millis = Millis((1000d / targetFPS.asDouble).toLong)
  lazy val multiplier: Double    = delta.toDouble / frameDuration.toDouble

  def intByTime(value: Int): Int          = (value * multiplier).toInt
  def floatByTime(value: Float): Float    = (value * multiplier).toFloat
  def doubleByTime(value: Double): Double = value * multiplier

  def setTargetFPS(fps: Int): GameTime =
    this.copy(targetFPS = GameTime.FPS(fps))

object GameTime:

  val zero: GameTime =
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

    inline def apply(fps: Int): FPS = fps

    extension (fps: FPS)
      def asLong: Long     = fps.toLong
      def asDouble: Double = fps.toDouble
  end FPS
