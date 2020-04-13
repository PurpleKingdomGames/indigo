package indigojs.delegates

import scala.scalajs.js.annotation._
import indigo.shared.ClearColor

// indigodoc entity:class name:ClearColor
// param name:r type:Double range:"0.0 to 1.0" desc:"Red amount"
// param name:g type:Double range:"0.0 to 1.0" desc:"Green amount"
// param name:b type:Double range:"0.0 to 1.0" desc:"Blue amount"
// param name:a type:Double range:"0.0 to 1.0" desc:"Alpha amount"
// desc "
// Describes the color that will be used to clear the screen. Visible
// wherever there are no other elements on the screen.
// "
// example ```
// new ClearColor(1, 0, 0, 1) // red
// ```
@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("ClearColor")
final class ClearColorDelegate(_r: Double, _g: Double, _b: Double, _a: Double) {

  @JSExport
  val r = _r

  @JSExport
  val g = _g

  @JSExport
  val b = _b

  @JSExport
  val a = _a

  def toInternal: ClearColor =
    new ClearColor(r, g, b, a)
}
