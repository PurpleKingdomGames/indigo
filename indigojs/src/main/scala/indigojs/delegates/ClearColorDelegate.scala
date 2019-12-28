package indigojs.delegates

import scala.scalajs.js.annotation._
import indigo.shared.ClearColor

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
