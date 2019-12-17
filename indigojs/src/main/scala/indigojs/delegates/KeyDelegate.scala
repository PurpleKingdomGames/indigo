package indigojs.delegates

import scala.scalajs.js.annotation._

import indigo.shared.constants.Key

@JSExportTopLevel("Key")
final class KeyDelegate(val code: Int, val key: String)

object KeyDelegate {
  def fromKey(keyCode: Key): KeyDelegate =
    new KeyDelegate(keyCode.code, keyCode.key)
}
