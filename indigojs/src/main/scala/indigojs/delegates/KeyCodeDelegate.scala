package indigojs.delegates

import scala.scalajs.js.annotation._

import indigo.shared.constants.KeyCode

@JSExportTopLevel("KeyCode")
final class KeyCodeDelegate(val code: Int, val printableCharacter: String)

object KeyCodeDelegate {
  def fromKeyCode(keyCode: KeyCode): KeyCodeDelegate =
    new KeyCodeDelegate(keyCode.code, keyCode.printableCharacter)
}
