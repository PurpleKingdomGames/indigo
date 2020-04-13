package indigojs.delegates

import scala.scalajs.js.annotation._
import indigo.shared.datatypes.RGB

// indigodoc entity:class name:RGB
// param name:r type:Double range:"0.0 to 1.0" desc:"Red amount"
// param name:g type:Double range:"0.0 to 1.0" desc:"Green amount"
// param name:b type:Double range:"0.0 to 1.0" desc:"Blue amount"
// param name:a type:Double range:"0.0 to 1.0" desc:"Alpha amount"
// desc "
// Used in lots of places to describe a color tint.
// "
// example ```
// new RGB(1, 0, 0, 1) // red
// ```
@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("RGB")
final class RGBDelegate(_r: Double, _g: Double, _b: Double) {

  @JSExport
  val r = _r

  @JSExport
  val g = _g

  @JSExport
  val b = _b

  // indigodoc entity:method name:withRed
  // arg name:newRed type:Double range:"0.0 to 1.0"
  // return RGB
  @JSExport
  def withRed(newRed: Double): RGBDelegate =
    new RGBDelegate(newRed, g, b)

  // indigodoc entity:method name:withGreen
  // arg name:newGreen type:Double range:"0.0 to 1.0"
  // return RGB
  @JSExport
  def withGreen(newGreen: Double): RGBDelegate =
    new RGBDelegate(r, newGreen, b)

  // indigodoc entity:method name:withBlue
  // arg name:newBlue type:Double range:"0.0 to 1.0"
  // return RGB
  @JSExport
  def withBlue(newBlue: Double): RGBDelegate =
    new RGBDelegate(r, g, newBlue)

  // indigodoc entity:method name:toClearColor
  // return ClearColor
  @JSExport
  def toClearColor: ClearColorDelegate =
    new ClearColorDelegate(r, g, b, 1.0)

  @JSExport
  def concat(other: RGBDelegate): RGBDelegate =
    (this, other) match {
      case (RGBDelegate.None, x) =>
        x
      case (x, RGBDelegate.None) =>
        x
      case (x, y) =>
        new RGBDelegate(x.r + y.r, x.g + y.g, x.b + y.b)
    }

  def toInternal: RGB =
    RGB(r, g, b)

}

// indigodoc entity:static name:RGBAHelper
// desc "
// Static values for various tints.
// "
// example ```
// RGBAHelper.Magenta
// ```
@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("RGBHelper")
@JSExportAll
object RGBDelegate {

  // indigodoc entity:value name:Red type:RGB
  val Red: RGBDelegate = new RGBDelegate(1, 0, 0)

  // indigodoc entity:value name:Green type:RGB
  val Green: RGBDelegate = new RGBDelegate(0, 1, 0)

  // indigodoc entity:value name:Blue type:RGB
  val Blue: RGBDelegate = new RGBDelegate(0, 0, 1)

  // indigodoc entity:value name:Yellow type:RGB
  val Yellow: RGBDelegate = new RGBDelegate(1, 1, 0)

  // indigodoc entity:value name:Magenta type:RGB
  val Magenta: RGBDelegate = new RGBDelegate(1, 0, 1)

  // indigodoc entity:value name:Cyan type:RGB
  val Cyan: RGBDelegate = new RGBDelegate(0, 1, 1)

  // indigodoc entity:value name:White type:RGB
  val White: RGBDelegate = new RGBDelegate(1, 1, 1)

  // indigodoc entity:value name:Black type:RGB
  val Black: RGBDelegate = new RGBDelegate(0, 0, 0)

  // indigodoc entity:value name:Normal type:RGB
  val Normal: RGBDelegate = White

  // indigodoc entity:value name:None type:RGB
  val None: RGBDelegate = White

  // indigodoc entity:value name:Zero type:RGB
  val Zero: RGBDelegate = new RGBDelegate(0, 0, 0)

}
