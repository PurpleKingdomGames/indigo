package indigo.shared.time

import indigo.shared.{EqualTo, AsString}

final class GameTime(val running: Millis, val delta: Seconds, val targetFPS: GameTime.FPS) {

  lazy val frameDuration: Millis = Millis((1000d / targetFPS.asDouble).toLong)
  lazy val multiplier: Double    = delta.toDouble / frameDuration.toDouble

  def intByTime(value: Int): Int          = (value * multiplier).toInt
  def floatByTime(value: Float): Float    = (value * multiplier).toFloat
  def doubleByTime(value: Double): Double = value * multiplier

  def setTargetFPS(fps: Int): GameTime =
    new GameTime(running, delta, GameTime.FPS(fps))
}

object GameTime {

  implicit val equalTo: EqualTo[GameTime] =
    EqualTo.create { (a, b) =>
      implicitly[EqualTo[Millis]].equal(a.running, b.running) &&
      implicitly[EqualTo[Seconds]].equal(a.delta, b.delta) &&
      implicitly[EqualTo[FPS]].equal(a.targetFPS, b.targetFPS)
    }

  implicit val gameTimeAsString: AsString[GameTime] =
    AsString.create { gt =>
      s"GameTime(running = ${implicitly[AsString[Millis]].show(gt.running)}, delta = ${implicitly[AsString[Seconds]].show(gt.delta)}, fps = ${implicitly[AsString[FPS]].show(gt.targetFPS)})"
    }

  def zero: GameTime =
    GameTime(Millis(0), Seconds(0), FPS.Default)

  def is(running: Millis): GameTime =
      new GameTime(running, Seconds(0), FPS.Default)

  def withDelta(running: Millis, delta: Seconds): GameTime =
      new GameTime(running, delta, FPS.Default)

  def apply(running: Millis, delta: Seconds, targetFPS: FPS): GameTime =
    new GameTime(running, delta, targetFPS)

  final class FPS(val value: Int) extends AnyVal {
    def asLong: Long     = value.toLong
    def asDouble: Double = value.toDouble
  }
  object FPS {

    implicit val fpsEqualTo: EqualTo[FPS] =
      EqualTo.create { (a, b) =>
        implicitly[EqualTo[Int]].equal(a.value, b.value)
      }

    implicit val fpsAsString: AsString[FPS] =
      AsString.create(fps => s"FPS(${implicitly[AsString[Int]].show(fps.value)})")

    val `30`: FPS    = FPS(30)
    val `60`: FPS    = FPS(60)
    val Default: FPS = `30`

    def apply(value: Int): FPS =
      new FPS(value)

  }

}
