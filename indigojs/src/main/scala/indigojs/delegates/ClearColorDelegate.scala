package indigojs.delegates

import scala.scalajs.js.annotation._
import indigo.shared.ClearColor

@JSExportTopLevel("ClearColor")
final class ClearColorDelegate(r: Double, g: Double, b: Double, a: Double) {
  def toInternal: ClearColor =
    new ClearColor(r, g, b, a)
}
