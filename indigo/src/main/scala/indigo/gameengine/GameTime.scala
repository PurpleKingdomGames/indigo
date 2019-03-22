package indigo.gameengine

import indigo.{EqualTo, AsString}
import indigo.abstractions.Monoid

final class GameTime(val running: GameTime.Millis, val delta: GameTime.Millis, val targetFPS: GameTime.FPS) {
  lazy val frameDuration: GameTime.Millis = GameTime.Millis(1000d / targetFPS.asDouble)
  lazy val multiplier: Double             = delta.value / frameDuration.value

  def intByTime(value: Int): Int          = (value * multiplier).toInt
  def floatByTime(value: Float): Float    = (value * multiplier).toFloat
  def doubleByTime(value: Double): Double = value * multiplier

  def setTargetFPS(fps: Int): GameTime =
    new GameTime(running, delta, GameTime.FPS(fps))

  def forwardInTimeBy(amount: GameTime.Millis): GameTime =
    GameTime.combine(this, GameTime(amount, GameTime.Millis(0), GameTime.FPS(0)))

  def backInTimeBy(amount: GameTime.Millis): GameTime =
    GameTime.combine(this, GameTime(GameTime.Millis(-amount.value), GameTime.Millis(0), GameTime.FPS(0)))
}

object GameTime extends Monoid[GameTime] {

  implicit val equalTo: EqualTo[GameTime] =
    EqualTo.create { (a, b) =>
      implicitly[EqualTo[Millis]].equal(a.running, b.running) &&
      implicitly[EqualTo[Millis]].equal(a.delta, b.delta) &&
      implicitly[EqualTo[FPS]].equal(a.targetFPS, b.targetFPS)
    }

  implicit val gameTimeAsString: AsString[GameTime] =
    AsString.create { gt =>
      s"GameTime(${implicitly[AsString[Millis]].show(gt.running)}, ${implicitly[AsString[Millis]].show(gt.delta)}, ${implicitly[AsString[FPS]].show(gt.targetFPS)})"
    }

  def now: GameTime                                       = new GameTime(Millis(System.currentTimeMillis().toDouble), Millis(0), FPS.Default)
  def zero: GameTime                                      = new GameTime(Millis(0), Millis(0), FPS.Default)
  def is(running: Millis): GameTime                       = new GameTime(running, Millis(0), FPS.Default)
  def withDelta(running: Millis, delta: Millis): GameTime = new GameTime(running, delta, FPS.Default)

  def apply(running: Millis, delta: Millis, targetFPS: FPS): GameTime =
    new GameTime(running, delta, targetFPS)

  def identity: GameTime =
    GameTime(GameTime.Millis(0), GameTime.Millis(0), GameTime.FPS(0))

  def combine(a: GameTime, b: GameTime): GameTime =
    GameTime(
      a.running + b.running,
      a.delta + b.delta,
      a.targetFPS + b.targetFPS
    )

  final class FPS(val value: Int) extends AnyVal {
    def asDouble: Double = value.toDouble

    def +(other: FPS): FPS =
      FPS.combine(this, other)
  }
  object FPS extends Monoid[FPS] {

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

    def identity: FPS =
      FPS(0)

    def combine(a: FPS, b: FPS): FPS =
      FPS(a.value + b.value)
  }

  final class Millis(val value: Double) extends AnyVal {

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
      Millis.lessThan(this, other) || implicitly[EqualTo[Double]].equal(this.value, other.value)

    def >=(other: Millis): Boolean =
      Millis.greaterThan(this, other) || implicitly[EqualTo[Double]].equal(this.value, other.value)

    def toInt: Int =
      value.toInt

    def toFloat: Float =
      value.toFloat

    def toDouble: Double =
      value

  }
  object Millis {

    val zero: Millis =
      Millis(0)

    implicit val equalToMillis: EqualTo[Millis] =
      EqualTo.create { (a, b) =>
        implicitly[EqualTo[Double]].equal(a.value, b.value)
      }

    implicit val asStringMillis: AsString[Millis] =
      AsString.create(d => s"Millis(${implicitly[AsString[Double]].show(d.value)})")

    def apply(value: Double): Millis =
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
