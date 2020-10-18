package indigoextras.datatypes

import indigo.shared.time.Seconds
import indigo.shared.EqualTo

/**
  * A [ValueOverTime[T]] manages the time based
  * rate of change logic for numeric values.
  */
sealed trait ValueOverTime[@specialized(Int, Long, Float, Double) T] {
  def zero: T
  def one: T
  def changeAmount(timeDelta: Seconds, unitsPerSecond: T): T
  def equal(a: T, b: T): Boolean
  def plus(a: T, b: T): T
  def minus(a: T, b: T): T
  def gt(a: T, b: T): Boolean
  def lt(a: T, b: T): Boolean
  def modulo(a: T, b: T): T
  def asString(t: T): String
}
object ValueOverTime {

  implicit val intValueOverTime: ValueOverTime[Int] =
    new ValueOverTime[Int] {
      val zero: Int = 0

      val one: Int = 1

      def changeAmount(timeDelta: Seconds, unitsPerSecond: Int): Int =
        (unitsPerSecond.toDouble * timeDelta.value).toInt

      def equal(a: Int, b: Int): Boolean =
        implicitly[EqualTo[Int]].equal(a, b)

      def plus(a: Int, b: Int): Int =
        a + b

      def minus(a: Int, b: Int): Int =
        a - b

      def gt(a: Int, b: Int): Boolean =
        a > b

      def lt(a: Int, b: Int): Boolean =
        a < b

      def modulo(a: Int, b: Int): Int =
        a % b

      def asString(t: Int): String =
        t.toString
    }

  implicit val longValueOverTime: ValueOverTime[Long] =
    new ValueOverTime[Long] {
      val zero: Long = 0

      val one: Long = 1

      def changeAmount(timeDelta: Seconds, unitsPerSecond: Long): Long =
        (unitsPerSecond.toDouble * timeDelta.value).toLong

      def equal(a: Long, b: Long): Boolean =
        implicitly[EqualTo[Long]].equal(a, b)

      def plus(a: Long, b: Long): Long =
        a + b

      def minus(a: Long, b: Long): Long =
        a - b

      def gt(a: Long, b: Long): Boolean =
        a > b

      def lt(a: Long, b: Long): Boolean =
        a < b

      def modulo(a: Long, b: Long): Long =
        a % b

      def asString(t: Long): String =
        t.toString
    }

  implicit val floatValueOverTime: ValueOverTime[Float] =
    new ValueOverTime[Float] {
      val zero: Float = 0

      val one: Float = 1

      def changeAmount(timeDelta: Seconds, unitsPerSecond: Float): Float =
        (unitsPerSecond.toDouble * timeDelta.value).toFloat

      def equal(a: Float, b: Float): Boolean =
        implicitly[EqualTo[Float]].equal(a, b)

      def plus(a: Float, b: Float): Float =
        a + b

      def minus(a: Float, b: Float): Float =
        a - b

      def gt(a: Float, b: Float): Boolean =
        a > b

      def lt(a: Float, b: Float): Boolean =
        a < b

      def modulo(a: Float, b: Float): Float =
        a % b

      def asString(t: Float): String =
        t.toString
    }

  implicit val doubleValueOverTime: ValueOverTime[Double] =
    new ValueOverTime[Double] {
      val zero: Double = 0

      val one: Double = 1

      def changeAmount(timeDelta: Seconds, unitsPerSecond: Double): Double =
        (unitsPerSecond * timeDelta.value).toDouble

      def equal(a: Double, b: Double): Boolean =
        implicitly[EqualTo[Double]].equal(a, b)

      def plus(a: Double, b: Double): Double =
        a + b

      def minus(a: Double, b: Double): Double =
        a - b

      def gt(a: Double, b: Double): Boolean =
        a > b

      def lt(a: Double, b: Double): Boolean =
        a < b

      def modulo(a: Double, b: Double): Double =
        a % b

      def asString(t: Double): String =
        t.toString
    }

  implicit class VoT[T](t: T)(implicit vot: ValueOverTime[T]) {

    val zero: T =
      vot.zero

    val one: T =
      vot.one

    def ===(b: T): Boolean =
      vot.equal(t, b)

    def +(b: T): T =
      vot.plus(t, b)

    def -(b: T): T =
      vot.minus(t, b)

    def >(b: T): Boolean =
      vot.gt(t, b)

    def <(b: T): Boolean =
      vot.lt(t, b)

    def %(b: T): T =
      vot.modulo(t, b)
  }

}
