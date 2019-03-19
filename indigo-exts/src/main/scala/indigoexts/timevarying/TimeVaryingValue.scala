package indigoexts.timevarying

import indigo.GameTime
import indigo.Eq
import indigo.Eq._

class TimeVaryingValue[T](val value: T, val startValue: T, val createdAt: Double)(implicit vot: ValueOverTime[T]) {

  def ===(other: TimeVaryingValue[T])(implicit eq: Eq[TimeVaryingValue[T]]): Boolean =
    eq.equal(this, other)

  def increase(unitsPerSecond: T, gameTime: GameTime): TimeVaryingValue[T] =
    TimeVaryingValue.increase(this, unitsPerSecond, gameTime)

  def increaseTo(limit: T, unitsPerSecond: T, gameTime: GameTime): TimeVaryingValue[T] =
    TimeVaryingValue.increaseTo(this, limit, unitsPerSecond, gameTime)

  def increaseWrapAt(limit: T, unitsPerSecond: T, gameTime: GameTime): TimeVaryingValue[T] =
    TimeVaryingValue.increaseWrapAt(this, limit, unitsPerSecond, gameTime)

  def decrease(unitsPerSecond: T, gameTime: GameTime): TimeVaryingValue[T] =
    TimeVaryingValue.decrease(this, unitsPerSecond, gameTime)

  def decreaseTo(limit: T, unitsPerSecond: T, gameTime: GameTime): TimeVaryingValue[T] =
    TimeVaryingValue.decreaseTo(this, limit, unitsPerSecond, gameTime)

  def decreaseWrapAt(limit: T, unitsPerSecond: T, gameTime: GameTime): TimeVaryingValue[T] =
    TimeVaryingValue.decreaseWrapAt(this, limit, unitsPerSecond, gameTime)

  override def toString(): String =
    s"TimeVaryingValue(${vot.asString(value)}, ${vot.asString(startValue)}, ${createdAt.toString})"

}
object TimeVaryingValue {

  implicit def eqTimeVaryingValue[T](implicit vot: ValueOverTime[T]): Eq[TimeVaryingValue[T]] =
    Eq.create[TimeVaryingValue[T]] { (a, b) =>
      vot.equal(a.value, b.value) && vot.equal(a.startValue, b.startValue) && a.createdAt === b.createdAt
    }

  import ValueOverTime._

  def apply[T](value: T, gameTime: GameTime)(implicit vot: ValueOverTime[T]): TimeVaryingValue[T] =
    new TimeVaryingValue(value, value, gameTime.running)

  def withStartingValue[T](value: T, startValue: T, gameTime: GameTime)(implicit vot: ValueOverTime[T]): TimeVaryingValue[T] =
    new TimeVaryingValue(value, startValue, gameTime.running)

  def modifyValue[T](timeVaryingValue: TimeVaryingValue[T], newValue: T)(implicit vot: ValueOverTime[T]): TimeVaryingValue[T] =
    new TimeVaryingValue(newValue, timeVaryingValue.startValue, timeVaryingValue.createdAt)

  def increase[T](timeVaryingValue: TimeVaryingValue[T], unitsPerSecond: T, gameTime: GameTime)(
      implicit vot: ValueOverTime[T]
  ): TimeVaryingValue[T] =
    modifyValue(
      timeVaryingValue,
      timeVaryingValue.startValue + vot.changeAmount(gameTime, unitsPerSecond, timeVaryingValue.createdAt)
    )

  def increaseTo[T](timeVaryingValue: TimeVaryingValue[T], limit: T, unitsPerSecond: T, gameTime: GameTime)(
      implicit vot: ValueOverTime[T]
  ): TimeVaryingValue[T] =
    timeVaryingValue.startValue + vot.changeAmount(gameTime, unitsPerSecond, timeVaryingValue.createdAt) match {
      case x if x === limit || x > limit =>
        modifyValue(timeVaryingValue, limit)

      case x =>
        modifyValue(timeVaryingValue, x)
    }

  def increaseWrapAt[T](timeVaryingValue: TimeVaryingValue[T], limit: T, unitsPerSecond: T, gameTime: GameTime)(
      implicit vot: ValueOverTime[T]
  ): TimeVaryingValue[T] =
    modifyValue(
      timeVaryingValue,
      (timeVaryingValue.startValue + vot.changeAmount(gameTime, unitsPerSecond, timeVaryingValue.createdAt)) % (limit + vot.one)
    )

  def decrease[T](timeVaryingValue: TimeVaryingValue[T], unitsPerSecond: T, gameTime: GameTime)(
      implicit vot: ValueOverTime[T]
  ): TimeVaryingValue[T] =
    modifyValue(
      timeVaryingValue,
      timeVaryingValue.startValue - vot.changeAmount(gameTime, unitsPerSecond, timeVaryingValue.createdAt)
    )

  def decreaseTo[T](timeVaryingValue: TimeVaryingValue[T], limit: T, unitsPerSecond: T, gameTime: GameTime)(
      implicit vot: ValueOverTime[T]
  ): TimeVaryingValue[T] =
    timeVaryingValue.startValue - vot.changeAmount(gameTime, unitsPerSecond, timeVaryingValue.createdAt) match {
      case x if x === limit || x < limit =>
        modifyValue(timeVaryingValue, limit)

      case x =>
        modifyValue(timeVaryingValue, x)
    }

  def decreaseWrapAt[T](timeVaryingValue: TimeVaryingValue[T], limit: T, unitsPerSecond: T, gameTime: GameTime)(
      implicit vot: ValueOverTime[T]
  ): TimeVaryingValue[T] =
    modifyValue(
      timeVaryingValue,
      (timeVaryingValue.startValue - vot.changeAmount(gameTime, unitsPerSecond, timeVaryingValue.createdAt)) % (limit + vot.one)
    )

}
