package indigo.gameengine

import indigo.{EqualTo, AsString}

final class GameTime(val system: GameTime.Millis, val delta: GameTime.Millis, val targetFPS: GameTime.FPS, val launch: GameTime.Millis) {

  val running: GameTime.Millis = system - launch

  lazy val frameDuration: GameTime.Millis = GameTime.Millis((1000d / targetFPS.asDouble).toLong)
  lazy val multiplier: Double             = delta.toDouble / frameDuration.toDouble

  def intByTime(value: Int): Int          = (value * multiplier).toInt
  def floatByTime(value: Float): Float    = (value * multiplier).toFloat
  def doubleByTime(value: Double): Double = value * multiplier

  def setTargetFPS(fps: Int): GameTime =
    new GameTime(running, delta, GameTime.FPS(fps), launch)

  // def forwardInTimeBy(amount: GameTime.Millis): GameTime =
  //   GameTime.combine(this, GameTime(amount, GameTime.Millis(0), GameTime.FPS(0)))

  // def backInTimeBy(amount: GameTime.Millis): GameTime =
  //   GameTime.combine(this, GameTime(GameTime.Millis(-amount.value), GameTime.Millis(0), GameTime.FPS(0)))
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
      s"GameTime(${implicitly[AsString[Millis]].show(gt.running)}, ${implicitly[AsString[Millis]].show(gt.delta)}, ${implicitly[AsString[FPS]].show(gt.targetFPS)})"
    }

  def now: GameTime =
    new GameTime(Millis(System.currentTimeMillis()), Millis(0), FPS.Default, Millis(0))

  def zero: GameTime =
    new GameTime(Millis(0), Millis(0), FPS.Default, Millis(0))

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
    def asLong: Long = value.toLong
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

  final class Millis(val value: Long) extends AnyVal {

    def +(other: Millis): Millis =
      Millis.plus(this, other)

    def -(other: Millis): Millis =
      Millis.minus(this, other)

    def *(other: Millis): Millis =
      Millis.multiply(this, other)

    def /(other: Millis): Millis =
      Millis.divide(this, other)

    def <(other: Millis): Boolean =
      Millis.lessThan(this, other)

    def >(other: Millis): Boolean =
      Millis.greaterThan(this, other)

    def <=(other: Millis): Boolean =
      Millis.lessThan(this, other) || implicitly[EqualTo[Long]].equal(this.value, other.value)

    def >=(other: Millis): Boolean =
      Millis.greaterThan(this, other) || implicitly[EqualTo[Long]].equal(this.value, other.value)

    def toInt: Int =
      value.toInt

    def toLong: Long =
      value

    def toFloat: Float =
      value.toFloat

    def toDouble: Double =
      value.toDouble

  }
  object Millis {

    val zero: Millis =
      Millis(0)

    implicit val equalToMillis: EqualTo[Millis] =
      EqualTo.create { (a, b) =>
        implicitly[EqualTo[Long]].equal(a.value, b.value)
      }

    implicit val asStringMillis: AsString[Millis] =
      AsString.create(d => s"Millis(${implicitly[AsString[Long]].show(d.value)})")

    def apply(value: Long): Millis =
      new Millis(value)

    def plus(a: Millis, b: Millis): Millis =
      Millis(a.value + b.value)

    def minus(a: Millis, b: Millis): Millis =
      Millis(a.value - b.value)

    def multiply(a: Millis, b: Millis): Millis =
      Millis(a.value * b.value)

    def divide(a: Millis, b: Millis): Millis =
      Millis(a.value / b.value)

    def greaterThan(a: Millis, b: Millis): Boolean =
      a.value > b.value

    def lessThan(a: Millis, b: Millis): Boolean =
      a.value < b.value

  }
}
