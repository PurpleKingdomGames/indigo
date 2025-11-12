package indigoextras.datatypes

import indigo.shared.time.Seconds

/** Represents one of the type of values that changes over time.
  */
sealed trait TimeVaryingValue derives CanEqual {

  /** The current value
    *
    * @return
    *   Double
    */
  val value: Double

  /** The rate of change
    *
    * @return
    *   Double
    */
  val unitsPerSecond: Double

  /** Value as an Int
    *
    * @return
    *   Int
    */
  def toInt: Int = value.toInt

  /** Value as an Long
    *
    * @return
    *   Long
    */
  def toLong: Long = value.toLong

  /** Value as an Float
    *
    * @return
    *   Float
    */
  def toFloat: Float = value.toFloat

  /** Value as an Double
    *
    * @return
    *   Double
    */
  def toDouble: Double = value

  /** Update the time varying value based on a time delta
    *
    * @param timeDelta
    *   the time delta typically supplied from GameTime(..).delta
    * @return
    *   TimeVaryingValue
    */
  def update(timeDelta: Seconds): TimeVaryingValue

}

/** A value that increases over time.
  *
  * @param value
  *   The current value
  * @param unitsPerSecond
  *   The rate of change
  */
final case class Increasing(value: Double, unitsPerSecond: Double) extends TimeVaryingValue {
  def update(timeDelta: Seconds): Increasing =
    this.copy(
      value = value + unitsPerSecond * timeDelta.toDouble
    )
}

/** A value that increases over time until it hits a limit.
  *
  * @param value
  *   The current value
  * @param unitsPerSecond
  *   The rate of change
  * @param limit
  *   The upper limit
  */
final case class IncreaseTo(value: Double, unitsPerSecond: Double, limit: Double) extends TimeVaryingValue {
  def update(timeDelta: Seconds): IncreaseTo =
    this.copy(
      value = value + unitsPerSecond * timeDelta.toDouble match {
        case x if x == limit || x > limit =>
          limit

        case x =>
          x
      }
    )
}

/** A TimeVaryingValue instance that progresses from 0.0 to 1.0 over time.
  */
final case class Lerp private (_tracker: IncreaseTo) extends TimeVaryingValue {
  def update(timeDelta: Seconds): Lerp =
    this.copy(
      _tracker = _tracker.update(timeDelta)
    )

  def progress: Double =
    val p = _tracker.value
    if p < 0.0 then 0.0 else p

  def progressPercent: Int =
    (100 * progress).toInt

  val isComplete: Boolean = _tracker.value >= 1.0
  val inProgress: Boolean = !isComplete

  val unitsPerSecond: Double = _tracker.unitsPerSecond
  val value: Double          = _tracker.value
}
object Lerp:
  def apply(over: Seconds): Lerp =
    Lerp(
      IncreaseTo(0.0, 1.0 / over.toDouble, 1.0)
    )

/** A value that increases over time and wraps back to zero when it hits the limit
  *
  * @param value
  *   The current/starting value
  * @param unitsPerSecond
  *   The rate of change
  * @param limit
  *   The upper limit
  */
final case class IncreaseWrapAt(value: Double, unitsPerSecond: Double, limit: Double) extends TimeVaryingValue {
  def update(timeDelta: Seconds): IncreaseWrapAt =
    this.copy(
      value = (value + unitsPerSecond * timeDelta.toDouble) % (limit + 1.0d)
    )
}
object IncreaseWrapAt {

  /** Constructor for a value that increases over time and wraps back to zero when it hits the limit. This constructor
    * assumes the start value is zero.
    *
    * @param unitsPerSecond
    *   The rate of change
    * @param limit
    *   The upper limit
    * @return
    *   IncreaseWrapAt
    */
  def apply(unitsPerSecond: Double, limit: Double): IncreaseWrapAt =
    IncreaseWrapAt(0, unitsPerSecond, limit)

}

/** A value that decreases over time.
  *
  * @param value
  *   The current value
  * @param unitsPerSecond
  *   The rate of change
  */
final case class Decreasing(value: Double, unitsPerSecond: Double) extends TimeVaryingValue {
  def update(timeDelta: Seconds): Decreasing =
    this.copy(
      value = value - unitsPerSecond * timeDelta.toDouble
    )
}

/** A value that decreases over time until it hits a limit.
  *
  * @param value
  *   The current value
  * @param unitsPerSecond
  *   The rate of change
  * @param limit
  *   The lower limit
  */
final case class DecreaseTo(value: Double, unitsPerSecond: Double, limit: Double) extends TimeVaryingValue {
  def update(timeDelta: Seconds): DecreaseTo =
    this.copy(
      value = value - unitsPerSecond * timeDelta.toDouble match {
        case x if x == limit || x < limit =>
          limit

        case x =>
          x
      }
    )
}

/** A value that decreases over time and wraps back to zero when it hits the limit
  *
  * @param value
  *   The current/starting value
  * @param unitsPerSecond
  *   The rate of change
  * @param limit
  *   The lower limit
  */
final case class DecreaseWrapAt(value: Double, unitsPerSecond: Double, limit: Double) extends TimeVaryingValue {
  def update(timeDelta: Seconds): DecreaseWrapAt =
    this.copy(
      value = (value - unitsPerSecond * timeDelta.toDouble) % (limit + 1.0d)
    )
}
object DecreaseWrapAt {

  /** Constructor for a value that decreases over time and wraps back to zero when it hits the limit. This constructor
    * assumes the start value is zero.
    *
    * @param value
    *   The current value
    * @param unitsPerSecond
    *   The rate of change
    * @param limit
    *   The lower limit
    * @return
    *   DecreaseWrapAt
    */
  def apply(unitsPerSecond: Double, limit: Double): DecreaseWrapAt =
    DecreaseWrapAt(0, unitsPerSecond, limit)

}
