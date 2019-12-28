package indigojs.delegates

import scala.scalajs.js.annotation._
import indigo.shared.datatypes.Tint

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Tint")
final class TintDelegate(_r: Double, _g: Double, _b: Double, _a: Double) {

  @JSExport
  val r = _r

  @JSExport
  val g = _g

  @JSExport
  val b = _b

  @JSExport
  val a = _a

  @JSExport
  def withRed(newRed: Double): TintDelegate =
    new TintDelegate(newRed, g, b, a)

  @JSExport
  def withGreen(newGreen: Double): TintDelegate =
    new TintDelegate(r, newGreen, b, a)

  @JSExport
  def withBlue(newBlue: Double): TintDelegate =
    new TintDelegate(r, g, newBlue, a)

  @JSExport
  def withAmount(amount: Double): TintDelegate =
    new TintDelegate(r, g, b, amount)

  @JSExport
  def toClearColor: ClearColorDelegate =
    new ClearColorDelegate(r, g, b, a)

  def toInternal: Tint =
    Tint(r, g, b, a)

}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("TintHelper")
@JSExportAll
object TintDelegate {

  val Red: TintDelegate     = new TintDelegate(1, 0, 0, 1)
  val Green: TintDelegate   = new TintDelegate(0, 1, 0, 1)
  val Blue: TintDelegate    = new TintDelegate(0, 0, 1, 1)
  val Yellow: TintDelegate  = new TintDelegate(1, 1, 0, 1)
  val Magenta: TintDelegate = new TintDelegate(1, 0, 1, 1)
  val Cyan: TintDelegate    = new TintDelegate(0, 1, 1, 1)
  val White: TintDelegate   = new TintDelegate(1, 1, 1, 1)
  val Black: TintDelegate   = new TintDelegate(0, 0, 0, 1)
  val Normal: TintDelegate  = White
  val None: TintDelegate    = White
  val Zero: TintDelegate    = new TintDelegate(0, 0, 0, 0)

}
