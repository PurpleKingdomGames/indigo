package indigoextras.datatypes

import indigo.shared.time.Seconds
import indigo.shared.EqualTo
import indigo.shared.EqualTo._

/**
  * Represents one of the type of values that changes over time.
  */
sealed trait TimeVaryingValue {

  /**
    * The current value
    *
    * @return Double
    */
  val value: Double

  /**
    * The rate of change
    *
    * @return Double
    */
  val unitsPerSecond: Double

  /**
    * Value as an Int
    *
    * @return Int
    */
  def toInt: Int = value.toInt

  /**
    * Value as an Long
    *
    * @return Long
    */
  def toLong: Long = value.toLong

  /**
    * Value as an Float
    *
    * @return Float
    */
  def toFloat: Float = value.toFloat

  /**
    * Value as an Double
    *
    * @return Double
    */
  def toDouble: Double = value

  /**
    * Update the time varying value based on a time delta
    *
    * @param timeDelta the time delta typically supplied from GameTime(..).delta
    * @return TimeVaryingValue
    */
  def update(timeDelta: Seconds): TimeVaryingValue
  
}

/**
  * A value that increases over time.
  *
  * @param value The current value
  * @param unitsPerSecond The rate of change
  */
final case class Increasing(value: Double, unitsPerSecond: Double) extends TimeVaryingValue {
  def update(timeDelta: Seconds): Increasing =
    this.copy(
      value = value + unitsPerSecond * timeDelta.value
    )
}

/**
  * A value that increases over time until it hits a limit.
  *
  * @param value The current value
  * @param unitsPerSecond The rate of change
  * @param limit The upper limit
  */
final case class IncreaseTo(value: Double, unitsPerSecond: Double, limit: Double) extends TimeVaryingValue {
  def update(timeDelta: Seconds): IncreaseTo =
    this.copy(
      value = value + unitsPerSecond * timeDelta.value match {
        case x if x === limit || x > limit =>
          limit

        case x =>
          x
      }
    )
}

/**
  * A value that increases over time and wraps back to zero when it hits the limit
  *
  * @param value The current/starting value
  * @param unitsPerSecond The rate of change
  * @param limit The upper limit
  */
final case class IncreaseWrapAt(value: Double, unitsPerSecond: Double, limit: Double) extends TimeVaryingValue {
  def update(timeDelta: Seconds): IncreaseWrapAt =
    this.copy(
      value = (value + unitsPerSecond * timeDelta.value) % (limit + 1.0d)
    )
}
object IncreaseWrapAt {

  /**
    * Constructor for a value that increases over time and wraps back to zero when it hits the limit.
    * This constructor assumes the start value is zero.
    *
    * @param unitsPerSecond The rate of change
    * @param limit The upper limit
    * @return IncreaseWrapAt
    */
  def apply(unitsPerSecond: Double, limit: Double): IncreaseWrapAt =
    IncreaseWrapAt(0, unitsPerSecond, limit)

}

/**
  * A value that decreases over time.
  *
  * @param value The current value
  * @param unitsPerSecond The rate of change
  */
final case class Decreasing(value: Double, unitsPerSecond: Double) extends TimeVaryingValue {
  def update(timeDelta: Seconds): Decreasing =
    this.copy(
      value = value - unitsPerSecond * timeDelta.value
    )
}

/**
  * A value that decreases over time until it hits a limit.
  *
  * @param value The current value
  * @param unitsPerSecond The rate of change
  * @param limit The lower limit
  */
final case class DecreaseTo(value: Double, unitsPerSecond: Double, limit: Double) extends TimeVaryingValue {
  def update(timeDelta: Seconds): DecreaseTo =
    this.copy(
      value = value - unitsPerSecond * timeDelta.value match {
        case x if x === limit || x < limit =>
          limit

        case x =>
          x
      }
    )
}

/**
  * A value that decreases over time and wraps back to zero when it hits the limit
  *
  * @param value The current/starting value
  * @param unitsPerSecond The rate of change
  * @param limit The lower limit
  */
final case class DecreaseWrapAt(value: Double, unitsPerSecond: Double, limit: Double) extends TimeVaryingValue {
  def update(timeDelta: Seconds): DecreaseWrapAt =
    this.copy(
      value = (value - unitsPerSecond * timeDelta.value) % (limit + 1.0d)
    )
}
object DecreaseWrapAt {

  /**
    * Constructor for a value that decreases over time and wraps back to zero when it hits the limit.
    * This constructor assumes the start value is zero.
    *
    * @param value The current value
    * @param unitsPerSecond The rate of change
    * @param limit The lower limit
    * @return DecreaseWrapAt
    */
  def apply(unitsPerSecond: Double, limit: Double): DecreaseWrapAt =
    DecreaseWrapAt(0, unitsPerSecond, limit)

}

object TimeVaryingValue {

  implicit def eqTimeVaryingValue: EqualTo[TimeVaryingValue] =
    EqualTo.create[TimeVaryingValue] { (a, b) =>
      a.value === b.value
    }

}
