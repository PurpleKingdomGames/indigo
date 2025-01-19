package indigo.facades

import scala.scalajs.js
import scala.scalajs.js.annotation.*

@js.native
@JSGlobal
class FontFace(val family: String, val source: String) extends js.Object {

  def load(): scala.scalajs.js.Promise[FontFace] =
    js.native

}
