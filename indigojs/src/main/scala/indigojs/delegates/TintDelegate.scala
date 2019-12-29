package indigojs.delegates

import scala.scalajs.js.annotation._
import indigo.shared.datatypes.Tint

// indigodoc entity:class name:Tint
// param name:r type:Double range:"0.0 to 1.0" desc:"Red amount"
// param name:g type:Double range:"0.0 to 1.0" desc:"Green amount"
// param name:b type:Double range:"0.0 to 1.0" desc:"Blue amount"
// param name:a type:Double range:"0.0 to 1.0" desc:"Alpha amount"
// desc "
// Used in lots of places to describe a color tint.
// "
// example ```
// new Tint(1, 0, 0, 1) // red
// ```
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

  // indigodoc entity:method name:withRed
  // arg name:newRed type:Double range:"0.0 to 1.0"
  // return Tint
  @JSExport
  def withRed(newRed: Double): TintDelegate =
    new TintDelegate(newRed, g, b, a)

  // indigodoc entity:method name:withGreen
  // arg name:newGreen type:Double range:"0.0 to 1.0"
  // return Tint
  @JSExport
  def withGreen(newGreen: Double): TintDelegate =
    new TintDelegate(r, newGreen, b, a)

  // indigodoc entity:method name:withBlue
  // arg name:newBlue type:Double range:"0.0 to 1.0"
  // return Tint
  @JSExport
  def withBlue(newBlue: Double): TintDelegate =
    new TintDelegate(r, g, newBlue, a)

  // indigodoc entity:method name:withRed
  // arg name:amount type:Double range:"0.0 to 1.0"
  // return Tint
  @JSExport
  def withAmount(amount: Double): TintDelegate =
    new TintDelegate(r, g, b, amount)

  // indigodoc entity:method name:toClearColor
  // return ClearColor
  @JSExport
  def toClearColor: ClearColorDelegate =
    new ClearColorDelegate(r, g, b, a)

  def toInternal: Tint =
    Tint(r, g, b, a)

}

// indigodoc entity:static name:TintHelper
// desc "
// Static values for various tints.
// "
// example ```
// TintHelper.Magenta
// ```
@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("TintHelper")
@JSExportAll
object TintDelegate {

  // indigodoc entity:value name:Red type:Tint
  val Red: TintDelegate     = new TintDelegate(1, 0, 0, 1)

  // indigodoc entity:value name:Green type:Tint
  val Green: TintDelegate   = new TintDelegate(0, 1, 0, 1)

  // indigodoc entity:value name:Blue type:Tint
  val Blue: TintDelegate    = new TintDelegate(0, 0, 1, 1)

  // indigodoc entity:value name:Yellow type:Tint
  val Yellow: TintDelegate  = new TintDelegate(1, 1, 0, 1)

  // indigodoc entity:value name:Magenta type:Tint
  val Magenta: TintDelegate = new TintDelegate(1, 0, 1, 1)

  // indigodoc entity:value name:Cyan type:Tint
  val Cyan: TintDelegate    = new TintDelegate(0, 1, 1, 1)

  // indigodoc entity:value name:White type:Tint
  val White: TintDelegate   = new TintDelegate(1, 1, 1, 1)

  // indigodoc entity:value name:Black type:Tint
  val Black: TintDelegate   = new TintDelegate(0, 0, 0, 1)

  // indigodoc entity:value name:Normal type:Tint
  val Normal: TintDelegate  = White

  // indigodoc entity:value name:None type:Tint
  val None: TintDelegate    = White

  // indigodoc entity:value name:Zero type:Tint
  val Zero: TintDelegate    = new TintDelegate(0, 0, 0, 0)

}
