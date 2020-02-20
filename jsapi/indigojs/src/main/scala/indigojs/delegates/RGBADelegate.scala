package indigojs.delegates

import scala.scalajs.js.annotation._
import indigo.shared.datatypes.RGBA

// indigodoc entity:class name:RGBA
// param name:r type:Double range:"0.0 to 1.0" desc:"Red amount"
// param name:g type:Double range:"0.0 to 1.0" desc:"Green amount"
// param name:b type:Double range:"0.0 to 1.0" desc:"Blue amount"
// param name:a type:Double range:"0.0 to 1.0" desc:"Alpha amount"
// desc "
// Used in lots of places to describe a color tint.
// "
// example ```
// new RGBA(1, 0, 0, 1) // red
// ```
@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("RGBA")
final class RGBADelegate(_r: Double, _g: Double, _b: Double, _a: Double) {

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
  // return RGBA
  @JSExport
  def withRed(newRed: Double): RGBADelegate =
    new RGBADelegate(newRed, g, b, a)

  // indigodoc entity:method name:withGreen
  // arg name:newGreen type:Double range:"0.0 to 1.0"
  // return RGBA
  @JSExport
  def withGreen(newGreen: Double): RGBADelegate =
    new RGBADelegate(r, newGreen, b, a)

  // indigodoc entity:method name:withBlue
  // arg name:newBlue type:Double range:"0.0 to 1.0"
  // return RGBA
  @JSExport
  def withBlue(newBlue: Double): RGBADelegate =
    new RGBADelegate(r, g, newBlue, a)

  // indigodoc entity:method name:withRed
  // arg name:amount type:Double range:"0.0 to 1.0"
  // return RGBA
  @JSExport
  def withAmount(amount: Double): RGBADelegate =
    new RGBADelegate(r, g, b, amount)

  // indigodoc entity:method name:toClearColor
  // return ClearColor
  @JSExport
  def toClearColor: ClearColorDelegate =
    new ClearColorDelegate(r, g, b, a)

  @JSExport
  def concat(other: RGBADelegate): RGBADelegate =
    (this, other) match {
      case (RGBADelegate.None, x) =>
        x
      case (x, RGBADelegate.None) =>
        x
      case (x, y) =>
        new RGBADelegate(x.r + y.r, x.g + y.g, x.b + y.b, x.a + y.a)
    }

  def toInternal: RGBA =
    RGBA(r, g, b, a)

}

// indigodoc entity:static name:RGBAHelper
// desc "
// Static values for various tints.
// "
// example ```
// RGBAHelper.Magenta
// ```
@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("RGBAHelper")
@JSExportAll
object RGBADelegate {

  // indigodoc entity:value name:Red type:RGBA
  val Red: RGBADelegate = new RGBADelegate(1, 0, 0, 1)

  // indigodoc entity:value name:Green type:RGBA
  val Green: RGBADelegate = new RGBADelegate(0, 1, 0, 1)

  // indigodoc entity:value name:Blue type:RGBA
  val Blue: RGBADelegate = new RGBADelegate(0, 0, 1, 1)

  // indigodoc entity:value name:Yellow type:RGBA
  val Yellow: RGBADelegate = new RGBADelegate(1, 1, 0, 1)

  // indigodoc entity:value name:Magenta type:RGBA
  val Magenta: RGBADelegate = new RGBADelegate(1, 0, 1, 1)

  // indigodoc entity:value name:Cyan type:RGBA
  val Cyan: RGBADelegate = new RGBADelegate(0, 1, 1, 1)

  // indigodoc entity:value name:White type:RGBA
  val White: RGBADelegate = new RGBADelegate(1, 1, 1, 1)

  // indigodoc entity:value name:Black type:RGBA
  val Black: RGBADelegate = new RGBADelegate(0, 0, 0, 1)

  // indigodoc entity:value name:Normal type:RGBA
  val Normal: RGBADelegate = White

  // indigodoc entity:value name:None type:RGBA
  val None: RGBADelegate = White

  // indigodoc entity:value name:Zero type:RGBA
  val Zero: RGBADelegate = new RGBADelegate(0, 0, 0, 0)

}
