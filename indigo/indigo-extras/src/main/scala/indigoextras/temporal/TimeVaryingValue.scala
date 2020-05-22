package indigoextras.temporal

import indigo.shared.time.Seconds
import indigo.shared.EqualTo
import indigo.shared.EqualTo._
import indigo.shared.AsString

final class TimeVaryingValue[@specialized(Int, Long, Float, Double) T](val value: T, val startValue: T, val createdAt: Seconds)(implicit vot: ValueOverTime[T], millisAsString: AsString[Seconds]) {

  def increase(unitsPerSecond: T, runningTime: Seconds): TimeVaryingValue[T] =
    TimeVaryingValue.increase(this, unitsPerSecond, runningTime)

  def increaseTo(limit: T, unitsPerSecond: T, runningTime: Seconds): TimeVaryingValue[T] =
    TimeVaryingValue.increaseTo(this, limit, unitsPerSecond, runningTime)

  def increaseWrapAt(limit: T, unitsPerSecond: T, runningTime: Seconds): TimeVaryingValue[T] =
    TimeVaryingValue.increaseWrapAt(this, limit, unitsPerSecond, runningTime)

  def decrease(unitsPerSecond: T, runningTime: Seconds): TimeVaryingValue[T] =
    TimeVaryingValue.decrease(this, unitsPerSecond, runningTime)

  def decreaseTo(limit: T, unitsPerSecond: T, runningTime: Seconds): TimeVaryingValue[T] =
    TimeVaryingValue.decreaseTo(this, limit, unitsPerSecond, runningTime)

  def decreaseWrapAt(limit: T, unitsPerSecond: T, runningTime: Seconds): TimeVaryingValue[T] =
    TimeVaryingValue.decreaseWrapAt(this, limit, unitsPerSecond, runningTime)

  override def toString(): String =
    s"TimeVaryingValue(${vot.asString(value)}, ${vot.asString(startValue)}, ${millisAsString.show(createdAt)})"

}
object TimeVaryingValue {

  implicit def eqTimeVaryingValue[@specialized(Int, Long, Float, Double) T](implicit vot: ValueOverTime[T]): EqualTo[TimeVaryingValue[T]] =
    EqualTo.create[TimeVaryingValue[T]] { (a, b) =>
      vot.equal(a.value, b.value) && vot.equal(a.startValue, b.startValue) && a.createdAt === b.createdAt
    }

  import ValueOverTime._

  def apply[@specialized(Int, Long, Float, Double) T](value: T, createdAt: Seconds)(implicit vot: ValueOverTime[T]): TimeVaryingValue[T] =
    new TimeVaryingValue(value, value, createdAt)

  def withStartingValue[@specialized(Int, Long, Float, Double) T](value: T, startValue: T, createdAt: Seconds)(implicit vot: ValueOverTime[T]): TimeVaryingValue[T] =
    new TimeVaryingValue(value, startValue, createdAt)

  def modifyValue[@specialized(Int, Long, Float, Double) T](timeVaryingValue: TimeVaryingValue[T], newValue: T)(implicit vot: ValueOverTime[T]): TimeVaryingValue[T] =
    new TimeVaryingValue(newValue, timeVaryingValue.startValue, timeVaryingValue.createdAt)

  def increase[@specialized(Int, Long, Float, Double) T](timeVaryingValue: TimeVaryingValue[T], unitsPerSecond: T, runningTime: Seconds)(
      implicit vot: ValueOverTime[T]
  ): TimeVaryingValue[T] =
    modifyValue(
      timeVaryingValue,
      timeVaryingValue.startValue + vot.changeAmount(runningTime, unitsPerSecond, timeVaryingValue.createdAt)
    )

  def increaseTo[@specialized(Int, Long, Float, Double) T](timeVaryingValue: TimeVaryingValue[T], limit: T, unitsPerSecond: T, runningTime: Seconds)(
      implicit vot: ValueOverTime[T]
  ): TimeVaryingValue[T] =
    timeVaryingValue.startValue + vot.changeAmount(runningTime, unitsPerSecond, timeVaryingValue.createdAt) match {
      case x if x === limit || x > limit =>
        modifyValue(timeVaryingValue, limit)

      case x =>
        modifyValue(timeVaryingValue, x)
    }

  def increaseWrapAt[@specialized(Int, Long, Float, Double) T](timeVaryingValue: TimeVaryingValue[T], limit: T, unitsPerSecond: T, runningTime: Seconds)(
      implicit vot: ValueOverTime[T]
  ): TimeVaryingValue[T] =
    modifyValue(
      timeVaryingValue,
      (timeVaryingValue.startValue + vot.changeAmount(runningTime, unitsPerSecond, timeVaryingValue.createdAt)) % (limit + vot.one)
    )

  def decrease[@specialized(Int, Long, Float, Double) T](timeVaryingValue: TimeVaryingValue[T], unitsPerSecond: T, runningTime: Seconds)(
      implicit vot: ValueOverTime[T]
  ): TimeVaryingValue[T] =
    modifyValue(
      timeVaryingValue,
      timeVaryingValue.startValue - vot.changeAmount(runningTime, unitsPerSecond, timeVaryingValue.createdAt)
    )

  def decreaseTo[@specialized(Int, Long, Float, Double) T](timeVaryingValue: TimeVaryingValue[T], limit: T, unitsPerSecond: T, runningTime: Seconds)(
      implicit vot: ValueOverTime[T]
  ): TimeVaryingValue[T] =
    timeVaryingValue.startValue - vot.changeAmount(runningTime, unitsPerSecond, timeVaryingValue.createdAt) match {
      case x if x === limit || x < limit =>
        modifyValue(timeVaryingValue, limit)

      case x =>
        modifyValue(timeVaryingValue, x)
    }

  def decreaseWrapAt[@specialized(Int, Long, Float, Double) T](timeVaryingValue: TimeVaryingValue[T], limit: T, unitsPerSecond: T, runningTime: Seconds)(
      implicit vot: ValueOverTime[T]
  ): TimeVaryingValue[T] =
    modifyValue(
      timeVaryingValue,
      (timeVaryingValue.startValue - vot.changeAmount(runningTime, unitsPerSecond, timeVaryingValue.createdAt)) % (limit + vot.one)
    )

}
