package indigojs.delegates

import scala.scalajs.js.annotation._

import indigo.shared.constants.Key

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Key")
final class KeyDelegate(_code: Int, _key: String) {

  @JSExport
  val code = _code
  @JSExport
  val key = _key

}

object KeyDelegate {
  def fromKey(keyCode: Key): KeyDelegate =
    new KeyDelegate(keyCode.code, keyCode.key)
}
