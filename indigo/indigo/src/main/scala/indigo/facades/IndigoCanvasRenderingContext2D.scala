package indigo.facades

import org.scalajs.dom.raw.CanvasRenderingContext2D

import scala.scalajs.js
import scala.scalajs.js.annotation._

@js.native
@JSGlobal
class IndigoCanvasRenderingContext2D extends CanvasRenderingContext2D {

  /** Text direction, left to right, right to left
    *
    * "ltr" || "rtl" || "inherit"
    */
  var direction: String = js.native

}
