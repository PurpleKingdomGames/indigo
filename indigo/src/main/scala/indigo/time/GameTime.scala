package indigo.time

import indigo.shared.{EqualTo, AsString}

final class GameTime(val system: Millis, val delta: Millis, val targetFPS: GameTime.FPS, val launch: Millis) {

  val running: Millis = system - launch

  lazy val frameDuration: Millis = Millis((1000d / targetFPS.asDouble).toLong)
  lazy val multiplier: Double    = delta.toDouble / frameDuration.toDouble

  def intByTime(value: Int): Int          = (value * multiplier).toInt
  def floatByTime(value: Float): Float    = (value * multiplier).toFloat
  def doubleByTime(value: Double): Double = value * multiplier

  def setTargetFPS(fps: Int): GameTime =
    new GameTime(running, delta, GameTime.FPS(fps), launch)
}

object GameTime {

  implicit val equalTo: EqualTo[GameTime] =
    EqualTo.create { (a, b) =>
      implicitly[EqualTo[Millis]].equal(a.system, b.system) &&
      implicitly[EqualTo[Millis]].equal(a.launch, b.launch) &&
      implicitly[EqualTo[Millis]].equal(a.delta, b.delta) &&
      implicitly[EqualTo[FPS]].equal(a.targetFPS, b.targetFPS)
    }

  implicit val gameTimeAsString: AsString[GameTime] =
    AsString.create { gt =>
      s"GameTime(running = ${implicitly[AsString[Millis]].show(gt.running)}, delta = ${implicitly[AsString[Millis]].show(gt.delta)}, fps = ${implicitly[AsString[FPS]].show(gt.targetFPS)})"
    }

  def now: GameTime =
    GameTime(Millis(System.currentTimeMillis()), Millis(0), FPS.Default, Millis(0))

  def zero: GameTime =
    GameTime(Millis(0), Millis(0), FPS.Default, Millis(0))

  def is(running: Millis): GameTime =
    ({ (system: Millis) =>
      new GameTime(system, Millis(0), FPS.Default, system - running)
    })(Millis(System.currentTimeMillis()))

  def withDelta(running: Millis, delta: Millis): GameTime =
    ({ (system: Millis) =>
      new GameTime(system, delta, FPS.Default, system - running)
    })(Millis(System.currentTimeMillis()))

  def apply(system: Millis, delta: Millis, targetFPS: FPS, launch: Millis): GameTime =
    new GameTime(system, delta, targetFPS, launch)

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
