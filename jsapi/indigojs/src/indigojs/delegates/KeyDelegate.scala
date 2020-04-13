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

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Keys")
@JSExportAll
object KeysDelegate {

  implicit private def intToKey(i: Int): KeyDelegate =
    new KeyDelegate(i, "")

  implicit private def intToKey(t: (Int, String)): KeyDelegate =
    new KeyDelegate(t._1, t._2)

  val BACKSPACE: KeyDelegate        = 8
  val TAB: KeyDelegate              = 9
  val ENTER: KeyDelegate            = 13 -> "\n"
  val SHIFT: KeyDelegate            = 16
  val CTRL: KeyDelegate             = 17
  val ALT: KeyDelegate              = 18
  val PAUSE_BREAK: KeyDelegate      = 19
  val CAPS_LOCK: KeyDelegate        = 20
  val ESCAPE: KeyDelegate           = 27
  val SPACE: KeyDelegate            = 32 -> " "
  val PAGE_UP: KeyDelegate          = 33
  val PAGE_DOWN: KeyDelegate        = 34
  val END: KeyDelegate              = 35
  val HOME: KeyDelegate             = 36
  val LEFT_ARROW: KeyDelegate       = 37
  val UP_ARROW: KeyDelegate         = 38
  val RIGHT_ARROW: KeyDelegate      = 39
  val DOWN_ARROW: KeyDelegate       = 40
  val INSERT: KeyDelegate           = 45
  val DELETE: KeyDelegate           = 46
  val KEY_0: KeyDelegate            = 48 -> "0"
  val KEY_1: KeyDelegate            = 49 -> "1"
  val KEY_2: KeyDelegate            = 50 -> "2"
  val KEY_3: KeyDelegate            = 51 -> "3"
  val KEY_4: KeyDelegate            = 52 -> "4"
  val KEY_5: KeyDelegate            = 53 -> "5"
  val KEY_6: KeyDelegate            = 54 -> "6"
  val KEY_7: KeyDelegate            = 55 -> "7"
  val KEY_8: KeyDelegate            = 56 -> "8"
  val KEY_9: KeyDelegate            = 57 -> "9"
  val KEY_A: KeyDelegate            = 65 -> "A"
  val KEY_B: KeyDelegate            = 66 -> "B"
  val KEY_C: KeyDelegate            = 67 -> "C"
  val KEY_D: KeyDelegate            = 68 -> "D"
  val KEY_E: KeyDelegate            = 69 -> "E"
  val KEY_F: KeyDelegate            = 70 -> "F"
  val KEY_G: KeyDelegate            = 71 -> "G"
  val KEY_H: KeyDelegate            = 72 -> "H"
  val KEY_I: KeyDelegate            = 73 -> "I"
  val KEY_J: KeyDelegate            = 74 -> "J"
  val KEY_K: KeyDelegate            = 75 -> "K"
  val KEY_L: KeyDelegate            = 76 -> "L"
  val KEY_M: KeyDelegate            = 77 -> "M"
  val KEY_N: KeyDelegate            = 78 -> "N"
  val KEY_O: KeyDelegate            = 79 -> "O"
  val KEY_P: KeyDelegate            = 80 -> "P"
  val KEY_Q: KeyDelegate            = 81 -> "Q"
  val KEY_R: KeyDelegate            = 82 -> "R"
  val KEY_S: KeyDelegate            = 83 -> "S"
  val KEY_T: KeyDelegate            = 84 -> "T"
  val KEY_U: KeyDelegate            = 85 -> "U"
  val KEY_V: KeyDelegate            = 86 -> "V"
  val KEY_W: KeyDelegate            = 87 -> "W"
  val KEY_X: KeyDelegate            = 88 -> "X"
  val KEY_Y: KeyDelegate            = 89 -> "Y"
  val KEY_Z: KeyDelegate            = 90 -> "Z"
  val LEFT_WINDOW_KEY: KeyDelegate  = 91
  val RIGHT_WINDOW_KEY: KeyDelegate = 92
  val SELECT_KEY: KeyDelegate       = 93
  val NUMPAD_0: KeyDelegate         = 96 -> "0"
  val NUMPAD_1: KeyDelegate         = 97 -> "1"
  val NUMPAD_2: KeyDelegate         = 98 -> "2"
  val NUMPAD_3: KeyDelegate         = 99 -> "3"
  val NUMPAD_4: KeyDelegate         = 100 -> "4"
  val NUMPAD_5: KeyDelegate         = 101 -> "5"
  val NUMPAD_6: KeyDelegate         = 102 -> "6"
  val NUMPAD_7: KeyDelegate         = 103 -> "7"
  val NUMPAD_8: KeyDelegate         = 104 -> "8"
  val NUMPAD_9: KeyDelegate         = 105 -> "9"
  val MULTIPLY: KeyDelegate         = 106 -> "*"
  val ADD: KeyDelegate              = 107 -> "+"
  val SUBTRACT: KeyDelegate         = 109 -> "-"
  val DECIMAL_POINT: KeyDelegate    = 110 -> "."
  val DIVIDE: KeyDelegate           = 111 -> "/"
  val F1: KeyDelegate               = 112
  val F2: KeyDelegate               = 113
  val F3: KeyDelegate               = 114
  val F4: KeyDelegate               = 115
  val F5: KeyDelegate               = 116
  val F6: KeyDelegate               = 117
  val F7: KeyDelegate               = 118
  val F8: KeyDelegate               = 119
  val F9: KeyDelegate               = 120
  val F10: KeyDelegate              = 121
  val F11: KeyDelegate              = 122
  val F12: KeyDelegate              = 123
  val NUM_LOCK: KeyDelegate         = 144
  val SCROLL_LOCK: KeyDelegate      = 145
  val SEMI_COLON: KeyDelegate       = 186 -> ";"
  val EQUAL_SIGN: KeyDelegate       = 187 -> "="
  val COMMA: KeyDelegate            = 188 -> ","
  val DASH: KeyDelegate             = 189 -> "-"
  val PERIOD: KeyDelegate           = 190 -> "."
  val FORWARD_SLASH: KeyDelegate    = 191 -> "/"
  val GRAVE_ACCENT: KeyDelegate     = 192
  val OPEN_BRACKET: KeyDelegate     = 219 -> "("
  val BACK_SLASH: KeyDelegate       = 220 -> "\\"
  val CLOSE_BRAKET: KeyDelegate     = 221 -> ")"
  val SINGLE_QUOTE: KeyDelegate     = 222 -> "\'"

}
