package indigoextras.datatypes

import indigo.shared.time.Seconds
import indigo.shared.EqualTo
import indigo.shared.EqualTo._

final case class TimeVaryingValue[@specialized(Int, Long, Float, Double) T](value: T, startValue: T)(implicit vot: ValueOverTime[T]) {

  def increase(unitsPerSecond: T, timeDelta: Seconds): TimeVaryingValue[T] =
    TimeVaryingValue.increase(this, unitsPerSecond, timeDelta)

  def increaseTo(limit: T, unitsPerSecond: T, timeDelta: Seconds): TimeVaryingValue[T] =
    TimeVaryingValue.increaseTo(this, limit, unitsPerSecond, timeDelta)

  def increaseWrapAt(limit: T, unitsPerSecond: T, timeDelta: Seconds): TimeVaryingValue[T] =
    TimeVaryingValue.increaseWrapAt(this, limit, unitsPerSecond, timeDelta)

  def decrease(unitsPerSecond: T, timeDelta: Seconds): TimeVaryingValue[T] =
    TimeVaryingValue.decrease(this, unitsPerSecond, timeDelta)

  def decreaseTo(limit: T, unitsPerSecond: T, timeDelta: Seconds): TimeVaryingValue[T] =
    TimeVaryingValue.decreaseTo(this, limit, unitsPerSecond, timeDelta)

  def decreaseWrapAt(limit: T, unitsPerSecond: T, timeDelta: Seconds): TimeVaryingValue[T] =
    TimeVaryingValue.decreaseWrapAt(this, limit, unitsPerSecond, timeDelta)

}
object TimeVaryingValue {

  implicit def eqTimeVaryingValue[@specialized(Int, Long, Float, Double) T](implicit vot: ValueOverTime[T]): EqualTo[TimeVaryingValue[T]] =
    EqualTo.create[TimeVaryingValue[T]] { (a, b) =>
      vot.equal(a.value, b.value) && vot.equal(a.startValue, b.startValue)
    }

  import ValueOverTime._

  def apply[@specialized(Int, Long, Float, Double) T](value: T)(implicit vot: ValueOverTime[T]): TimeVaryingValue[T] =
    TimeVaryingValue(value, value)

  def withStartingValue[@specialized(Int, Long, Float, Double) T](value: T, startValue: T)(implicit vot: ValueOverTime[T]): TimeVaryingValue[T] =
    TimeVaryingValue(value, startValue)

  def modifyValue[@specialized(Int, Long, Float, Double) T](timeVaryingValue: TimeVaryingValue[T], newValue: T)(implicit vot: ValueOverTime[T]): TimeVaryingValue[T] =
    TimeVaryingValue(newValue, timeVaryingValue.startValue)

  def increase[@specialized(Int, Long, Float, Double) T](timeVaryingValue: TimeVaryingValue[T], unitsPerSecond: T, timeDelta: Seconds)(implicit
      vot: ValueOverTime[T]
  ): TimeVaryingValue[T] =
    modifyValue(
      timeVaryingValue,
      timeVaryingValue.startValue + vot.changeAmount(timeDelta, unitsPerSecond)
    )

  def increaseTo[@specialized(Int, Long, Float, Double) T](timeVaryingValue: TimeVaryingValue[T], limit: T, unitsPerSecond: T, timeDelta: Seconds)(implicit
      vot: ValueOverTime[T]
  ): TimeVaryingValue[T] =
    timeVaryingValue.startValue + vot.changeAmount(timeDelta, unitsPerSecond) match {
      case x if x === limit || x > limit =>
        modifyValue(timeVaryingValue, limit)

      case x =>
        modifyValue(timeVaryingValue, x)
    }

  def increaseWrapAt[@specialized(Int, Long, Float, Double) T](timeVaryingValue: TimeVaryingValue[T], limit: T, unitsPerSecond: T, timeDelta: Seconds)(implicit
      vot: ValueOverTime[T]
  ): TimeVaryingValue[T] =
    modifyValue(
      timeVaryingValue,
      (timeVaryingValue.startValue + vot.changeAmount(timeDelta, unitsPerSecond)) % (limit + vot.one)
    )

  def decrease[@specialized(Int, Long, Float, Double) T](timeVaryingValue: TimeVaryingValue[T], unitsPerSecond: T, timeDelta: Seconds)(implicit
      vot: ValueOverTime[T]
  ): TimeVaryingValue[T] =
    modifyValue(
      timeVaryingValue,
      timeVaryingValue.startValue - vot.changeAmount(timeDelta, unitsPerSecond)
    )

  def decreaseTo[@specialized(Int, Long, Float, Double) T](timeVaryingValue: TimeVaryingValue[T], limit: T, unitsPerSecond: T, timeDelta: Seconds)(implicit
      vot: ValueOverTime[T]
  ): TimeVaryingValue[T] =
    timeVaryingValue.startValue - vot.changeAmount(timeDelta, unitsPerSecond) match {
      case x if x === limit || x < limit =>
        modifyValue(timeVaryingValue, limit)

      case x =>
        modifyValue(timeVaryingValue, x)
    }

  def decreaseWrapAt[@specialized(Int, Long, Float, Double) T](timeVaryingValue: TimeVaryingValue[T], limit: T, unitsPerSecond: T, timeDelta: Seconds)(implicit
      vot: ValueOverTime[T]
  ): TimeVaryingValue[T] =
    modifyValue(
      timeVaryingValue,
      (timeVaryingValue.startValue - vot.changeAmount(timeDelta, unitsPerSecond)) % (limit + vot.one)
    )

}
