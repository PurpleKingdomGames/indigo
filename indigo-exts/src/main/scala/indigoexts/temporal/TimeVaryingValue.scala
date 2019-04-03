package indigoexts.temporal

import indigo.time.Millis
import indigo.shared.EqualTo
import indigo.shared.EqualTo._
import indigo.shared.AsString

class TimeVaryingValue[T](val value: T, val startValue: T, val createdAt: Millis)(implicit vot: ValueOverTime[T], millisAsString: AsString[Millis]) {

  def ===(other: TimeVaryingValue[T])(implicit eq: EqualTo[TimeVaryingValue[T]]): Boolean =
    eq.equal(this, other)

  def increase(unitsPerSecond: T, runningTime: Millis): TimeVaryingValue[T] =
    TimeVaryingValue.increase(this, unitsPerSecond, runningTime)

  def increaseTo(limit: T, unitsPerSecond: T, runningTime: Millis): TimeVaryingValue[T] =
    TimeVaryingValue.increaseTo(this, limit, unitsPerSecond, runningTime)

  def increaseWrapAt(limit: T, unitsPerSecond: T, runningTime: Millis): TimeVaryingValue[T] =
    TimeVaryingValue.increaseWrapAt(this, limit, unitsPerSecond, runningTime)

  def decrease(unitsPerSecond: T, runningTime: Millis): TimeVaryingValue[T] =
    TimeVaryingValue.decrease(this, unitsPerSecond, runningTime)

  def decreaseTo(limit: T, unitsPerSecond: T, runningTime: Millis): TimeVaryingValue[T] =
    TimeVaryingValue.decreaseTo(this, limit, unitsPerSecond, runningTime)

  def decreaseWrapAt(limit: T, unitsPerSecond: T, runningTime: Millis): TimeVaryingValue[T] =
    TimeVaryingValue.decreaseWrapAt(this, limit, unitsPerSecond, runningTime)

  override def toString(): String =
    s"TimeVaryingValue(${vot.asString(value)}, ${vot.asString(startValue)}, ${millisAsString.show(createdAt)})"

}
object TimeVaryingValue {

  implicit def eqTimeVaryingValue[T](implicit vot: ValueOverTime[T]): EqualTo[TimeVaryingValue[T]] =
    EqualTo.create[TimeVaryingValue[T]] { (a, b) =>
      vot.equal(a.value, b.value) && vot.equal(a.startValue, b.startValue) && a.createdAt === b.createdAt
    }

  import ValueOverTime._

  def apply[T](value: T, createdAt: Millis)(implicit vot: ValueOverTime[T]): TimeVaryingValue[T] =
    new TimeVaryingValue(value, value, createdAt)

  def withStartingValue[T](value: T, startValue: T, createdAt: Millis)(implicit vot: ValueOverTime[T]): TimeVaryingValue[T] =
    new TimeVaryingValue(value, startValue, createdAt)

  def modifyValue[T](timeVaryingValue: TimeVaryingValue[T], newValue: T)(implicit vot: ValueOverTime[T]): TimeVaryingValue[T] =
    new TimeVaryingValue(newValue, timeVaryingValue.startValue, timeVaryingValue.createdAt)

  def increase[T](timeVaryingValue: TimeVaryingValue[T], unitsPerSecond: T, runningTime: Millis)(
      implicit vot: ValueOverTime[T]
  ): TimeVaryingValue[T] =
    modifyValue(
      timeVaryingValue,
      timeVaryingValue.startValue + vot.changeAmount(runningTime, unitsPerSecond, timeVaryingValue.createdAt)
    )

  def increaseTo[T](timeVaryingValue: TimeVaryingValue[T], limit: T, unitsPerSecond: T, runningTime: Millis)(
      implicit vot: ValueOverTime[T]
  ): TimeVaryingValue[T] =
    timeVaryingValue.startValue + vot.changeAmount(runningTime, unitsPerSecond, timeVaryingValue.createdAt) match {
      case x if x === limit || x > limit =>
        modifyValue(timeVaryingValue, limit)

      case x =>
        modifyValue(timeVaryingValue, x)
    }

  def increaseWrapAt[T](timeVaryingValue: TimeVaryingValue[T], limit: T, unitsPerSecond: T, runningTime: Millis)(
      implicit vot: ValueOverTime[T]
  ): TimeVaryingValue[T] =
    modifyValue(
      timeVaryingValue,
      (timeVaryingValue.startValue + vot.changeAmount(runningTime, unitsPerSecond, timeVaryingValue.createdAt)) % (limit + vot.one)
    )

  def decrease[T](timeVaryingValue: TimeVaryingValue[T], unitsPerSecond: T, runningTime: Millis)(
      implicit vot: ValueOverTime[T]
  ): TimeVaryingValue[T] =
    modifyValue(
      timeVaryingValue,
      timeVaryingValue.startValue - vot.changeAmount(runningTime, unitsPerSecond, timeVaryingValue.createdAt)
    )

  def decreaseTo[T](timeVaryingValue: TimeVaryingValue[T], limit: T, unitsPerSecond: T, runningTime: Millis)(
      implicit vot: ValueOverTime[T]
  ): TimeVaryingValue[T] =
    timeVaryingValue.startValue - vot.changeAmount(runningTime, unitsPerSecond, timeVaryingValue.createdAt) match {
      case x if x === limit || x < limit =>
        modifyValue(timeVaryingValue, limit)

      case x =>
        modifyValue(timeVaryingValue, x)
    }

  def decreaseWrapAt[T](timeVaryingValue: TimeVaryingValue[T], limit: T, unitsPerSecond: T, runningTime: Millis)(
      implicit vot: ValueOverTime[T]
  ): TimeVaryingValue[T] =
    modifyValue(
      timeVaryingValue,
      (timeVaryingValue.startValue - vot.changeAmount(runningTime, unitsPerSecond, timeVaryingValue.createdAt)) % (limit + vot.one)
    )

}
