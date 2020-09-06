package indigo.shared.time

import indigo.shared.EqualTo

final class GameTime(val running: Seconds, val delta: Seconds, val targetFPS: GameTime.FPS) {

  lazy val frameDuration: Millis = Millis((1000d / targetFPS.asDouble).toLong)
  lazy val multiplier: Double    = delta.toDouble / frameDuration.toDouble

  def intByTime(value: Int): Int          = (value * multiplier).toInt
  def floatByTime(value: Float): Float    = (value * multiplier).toFloat
  def doubleByTime(value: Double): Double = value * multiplier

  def setTargetFPS(fps: Int): GameTime =
    new GameTime(running, delta, GameTime.FPS(fps))

  override def toString(): String =
    s"GameTime(running = ${running.toString()}, delta = ${delta.toString()}, fps = ${targetFPS.toString()})"
}

object GameTime {

  implicit val equalTo: EqualTo[GameTime] =
    EqualTo.create { (a, b) =>
      implicitly[EqualTo[Seconds]].equal(a.running, b.running) &&
      implicitly[EqualTo[Seconds]].equal(a.delta, b.delta) &&
      implicitly[EqualTo[FPS]].equal(a.targetFPS, b.targetFPS)
    }

  def zero: GameTime =
    GameTime(Seconds.zero, Seconds.zero, FPS.Default)

  def is(running: Seconds): GameTime =
    new GameTime(running, Seconds.zero, FPS.Default)

  def withDelta(running: Seconds, delta: Seconds): GameTime =
    new GameTime(running, delta, FPS.Default)

  def apply(running: Seconds, delta: Seconds, targetFPS: FPS): GameTime =
    new GameTime(running, delta, targetFPS)

  final class FPS(val value: Int) extends AnyVal {
    def asLong: Long     = value.toLong
    def asDouble: Double = value.toDouble

    override def toString(): String =
      s"FPS(${value.toString()})"
  }
  object FPS {

    implicit val fpsEqualTo: EqualTo[FPS] =
      EqualTo.create { (a, b) =>
        implicitly[EqualTo[Int]].equal(a.value, b.value)
      }

    val `30`: FPS    = FPS(30)
    val `60`: FPS    = FPS(60)
    val Default: FPS = `30`

    def apply(value: Int): FPS =
      new FPS(value)

  }

}
