package indigojs.delegates.temporal

import scala.scalajs.js.annotation._
import indigo.shared.temporal.TimeVaryingValue
import indigo.shared.time.Seconds

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("TimeVaryingValue")
final class TimeVaryingValueDelegate(_value: Double, _startValue: Double, _createdAt: Double) {

  @JSExport
  val value = _value
  @JSExport
  val startValue = _startValue
  @JSExport
  val createdAt = _createdAt

  private def convert(tvv: TimeVaryingValue[Double]): TimeVaryingValueDelegate =
    new TimeVaryingValueDelegate(tvv.value, tvv.startValue, tvv.createdAt.value.toDouble)

  @JSExport
  def increase(unitsPerSecond: Double, runningTime: Double): TimeVaryingValueDelegate =
    convert(TimeVaryingValue.increase(this.toInternal, unitsPerSecond, Seconds(runningTime)))

  @JSExport
  def increaseTo(limit: Double, unitsPerSecond: Double, runningTime: Double): TimeVaryingValueDelegate =
    convert(TimeVaryingValue.increaseTo(this.toInternal, limit, unitsPerSecond, Seconds(runningTime)))

  @JSExport
  def increaseWrapAt(limit: Double, unitsPerSecond: Double, runningTime: Double): TimeVaryingValueDelegate =
    convert(TimeVaryingValue.increaseWrapAt(this.toInternal, limit, unitsPerSecond, Seconds(runningTime)))

  @JSExport
  def decrease(unitsPerSecond: Double, runningTime: Double): TimeVaryingValueDelegate =
    convert(TimeVaryingValue.decrease(this.toInternal, unitsPerSecond, Seconds(runningTime)))

  @JSExport
  def decreaseTo(limit: Double, unitsPerSecond: Double, runningTime: Double): TimeVaryingValueDelegate =
    convert(TimeVaryingValue.decreaseTo(this.toInternal, limit, unitsPerSecond, Seconds(runningTime)))

  @JSExport
  def decreaseWrapAt(limit: Double, unitsPerSecond: Double, runningTime: Double): TimeVaryingValueDelegate =
    convert(TimeVaryingValue.decreaseWrapAt(this.toInternal, limit, unitsPerSecond, Seconds(runningTime)))

  @JSExport
  def toInternal: TimeVaryingValue[Double] =
    new TimeVaryingValue[Double](value, startValue, Seconds(createdAt))
}
