package indigojs.delegates.temporal

import scala.scalajs.js.annotation._
import indigo.shared.temporal.TimeVaryingValue
import indigo.shared.time.Millis

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("TimeVaryingValue")
final class TimeVaryingValueDelegate(val value: Double, val startValue: Double, val createdAt: Double) {

  private def convert(tvv: TimeVaryingValue[Double]): TimeVaryingValueDelegate =
    new TimeVaryingValueDelegate(tvv.value, tvv.startValue, tvv.createdAt.value.toDouble)

  @JSExport
  def increase(unitsPerSecond: Double, runningTime: Double): TimeVaryingValueDelegate =
    convert(TimeVaryingValue.increase(this.toInternal, unitsPerSecond, Millis(runningTime.toLong)))

  @JSExport
  def increaseTo(limit: Double, unitsPerSecond: Double, runningTime: Double): TimeVaryingValueDelegate =
    convert(TimeVaryingValue.increaseTo(this.toInternal, limit, unitsPerSecond, Millis(runningTime.toLong)))

  @JSExport
  def increaseWrapAt(limit: Double, unitsPerSecond: Double, runningTime: Double): TimeVaryingValueDelegate =
    convert(TimeVaryingValue.increaseWrapAt(this.toInternal, limit, unitsPerSecond, Millis(runningTime.toLong)))

  @JSExport
  def decrease(unitsPerSecond: Double, runningTime: Double): TimeVaryingValueDelegate =
    convert(TimeVaryingValue.decrease(this.toInternal, unitsPerSecond, Millis(runningTime.toLong)))

  @JSExport
  def decreaseTo(limit: Double, unitsPerSecond: Double, runningTime: Double): TimeVaryingValueDelegate =
    convert(TimeVaryingValue.decreaseTo(this.toInternal, limit, unitsPerSecond, Millis(runningTime.toLong)))

  @JSExport
  def decreaseWrapAt(limit: Double, unitsPerSecond: Double, runningTime: Double): TimeVaryingValueDelegate =
    convert(TimeVaryingValue.decreaseWrapAt(this.toInternal, limit, unitsPerSecond, Millis(runningTime.toLong)))

  @JSExport
  def toInternal: TimeVaryingValue[Double] =
    new TimeVaryingValue[Double](value, startValue, Millis(createdAt.toLong))
}
