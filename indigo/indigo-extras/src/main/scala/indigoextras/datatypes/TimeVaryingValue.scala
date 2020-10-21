package indigoextras.datatypes

import indigo.shared.time.Seconds
import indigo.shared.EqualTo
import indigo.shared.EqualTo._

sealed trait TimeVaryingValue {
  val value: Double
  val unitsPerSecond: Double
  val toInt: Int       = value.toInt
  val toLong: Long     = value.toLong
  val toFloat: Float   = value.toFloat
  val toDouble: Double = value
}
final case class Increasing(value: Double, unitsPerSecond: Double) extends TimeVaryingValue {
  def update(timeDelta: Seconds): Increasing =
    this.copy(
      value = value + unitsPerSecond * timeDelta.value
    )
}
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
final case class IncreaseWrapAt(value: Double, unitsPerSecond: Double, limit: Double) extends TimeVaryingValue {
  def update(timeDelta: Seconds): IncreaseWrapAt =
    this.copy(
      value = (value + unitsPerSecond * timeDelta.value) % (limit + 1.0d)
    )
}
final case class Decreasing(value: Double, unitsPerSecond: Double) extends TimeVaryingValue {
  def update(timeDelta: Seconds): Decreasing =
    this.copy(
      value = value - unitsPerSecond * timeDelta.value
    )
}
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
final case class DecreaseWrapAt(value: Double, unitsPerSecond: Double, limit: Double) extends TimeVaryingValue {
  def update(timeDelta: Seconds): DecreaseWrapAt =
    this.copy(
      value = (value - unitsPerSecond * timeDelta.value) % (limit + 1.0d)
    )
}

object TimeVaryingValue {

  implicit def eqTimeVaryingValue: EqualTo[TimeVaryingValue] =
    EqualTo.create[TimeVaryingValue] { (a, b) =>
      a.value === b.value
    }

}
