package indigo.facades

import org.scalajs.dom.CanvasRenderingContext2D

import scala.scalajs.js
import scala.scalajs.js.annotation.*

@SuppressWarnings(Array("scalafix:DisableSyntax.var"))
@js.native
@JSGlobal
class IndigoCanvasRenderingContext2D extends CanvasRenderingContext2D {

  /** Text direction, left to right, right to left
    *
    * "ltr" || "rtl" || "inherit"
    */
  var direction: String = js.native

}

@SuppressWarnings(Array("scalafix:DisableSyntax.var"))
@js.native
@JSGlobal
class IndigoTextMetrics extends js.Object {
  var width: Double                    = js.native
  var actualBoundingBoxLeft: Double    = js.native
  var actualBoundingBoxRight: Double   = js.native
  var actualBoundingBoxAscent: Double  = js.native
  var actualBoundingBoxDescent: Double = js.native
}
