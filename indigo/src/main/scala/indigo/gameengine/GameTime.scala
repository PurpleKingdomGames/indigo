package indigo.gameengine

import indigo.{EqualTo, AsString}

final class GameTime(val running: GameTime.Millis, val delta: GameTime.Millis, val targetFPS: GameTime.FPS) {
  lazy val frameDuration: GameTime.Millis = GameTime.Millis(1000d / targetFPS.asDouble)
  lazy val multiplier: Double             = delta.value / frameDuration.value

  def intByTime(value: Int): Int          = (value * multiplier).toInt
  def floatByTime(value: Float): Float    = (value * multiplier).toFloat
  def doubleByTime(value: Double): Double = value * multiplier

  def setTargetFPS(fps: Int): GameTime =
    new GameTime(running, delta, GameTime.FPS(fps))
}

object GameTime {

  def now: GameTime                                       = new GameTime(Millis(System.currentTimeMillis().toDouble), Millis(0), FPS.Default)
  def zero: GameTime                                      = new GameTime(Millis(0), Millis(0), FPS.Default)
  def is(running: Millis): GameTime                       = new GameTime(running, Millis(0), FPS.Default)
  def withDelta(running: Millis, delta: Millis): GameTime = new GameTime(running, delta, FPS.Default)

  def apply(running: Millis, delta: Millis, targetFPS: FPS): GameTime =
    new GameTime(running, delta, targetFPS)

  final class FPS(val value: Int) extends AnyVal {
    def asDouble: Double = value.toDouble
  }
  object FPS {

    val `30`: FPS    = FPS(30)
    val `60`: FPS    = FPS(60)
    val Default: FPS = `30`

    def apply(value: Int): FPS =
      new FPS(value)
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

    implicit def equalToMillis(implicit double: EqualTo[Double]): EqualTo[Millis] =
      EqualTo.create { (a, b) =>
        double.equal(a.value, b.value)
      }

    implicit def asStringMillis(implicit double: AsString[Double]): AsString[Millis] =
      AsString.create(d => s"Millis(${double.show(d.value)})")

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
