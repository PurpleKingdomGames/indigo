package indigo.shared.constants

import indigo.shared.EqualTo
import indigo.shared.EqualTo._

final case class Key(code: Int, key: String) {
  def isPrintable: Boolean =
    (key !== "") && Keys.printable.map(_.code).contains(this.code)

  def ===(other: Key): Boolean =
    implicitly[EqualTo[Key]].equal(this, other)

  @SuppressWarnings(Array("org.wartremover.warts.IsInstanceOf"))
  override def equals(that: Any): Boolean =
    that match {
      case that: Key =>
        that.isInstanceOf[Key] && this.code === that.code
      case _ => false
    }
}
object Key {
  implicit val equals: EqualTo[Key] = {
    val eqI = implicitly[EqualTo[Int]]
    EqualTo.create((a, b) => eqI.equal(a.code, b.code))
  }
}

object Keys {

  implicit private def intToKey(i: Int): Key =
    Key(i, "")

  implicit private def intToKey(t: (Int, String)): Key =
    Key(t._1, t._2)

  val BACKSPACE: Key        = 8
  val TAB: Key              = 9
  val ENTER: Key            = 13 -> "\n"
  val SHIFT: Key            = 16
  val CTRL: Key             = 17
  val ALT: Key              = 18
  val PAUSE_BREAK: Key      = 19
  val CAPS_LOCK: Key        = 20
  val ESCAPE: Key           = 27
  val SPACE: Key            = 32 -> " "
  val PAGE_UP: Key          = 33
  val PAGE_DOWN: Key        = 34
  val END: Key              = 35
  val HOME: Key             = 36
  val LEFT_ARROW: Key       = 37
  val UP_ARROW: Key         = 38
  val RIGHT_ARROW: Key      = 39
  val DOWN_ARROW: Key       = 40
  val INSERT: Key           = 45
  val DELETE: Key           = 46
  val KEY_0: Key            = 48 -> "0"
  val KEY_1: Key            = 49 -> "1"
  val KEY_2: Key            = 50 -> "2"
  val KEY_3: Key            = 51 -> "3"
  val KEY_4: Key            = 52 -> "4"
  val KEY_5: Key            = 53 -> "5"
  val KEY_6: Key            = 54 -> "6"
  val KEY_7: Key            = 55 -> "7"
  val KEY_8: Key            = 56 -> "8"
  val KEY_9: Key            = 57 -> "9"
  val KEY_A: Key            = 65 -> "A"
  val KEY_B: Key            = 66 -> "B"
  val KEY_C: Key            = 67 -> "C"
  val KEY_D: Key            = 68 -> "D"
  val KEY_E: Key            = 69 -> "E"
  val KEY_F: Key            = 70 -> "F"
  val KEY_G: Key            = 71 -> "G"
  val KEY_H: Key            = 72 -> "H"
  val KEY_I: Key            = 73 -> "I"
  val KEY_J: Key            = 74 -> "J"
  val KEY_K: Key            = 75 -> "K"
  val KEY_L: Key            = 76 -> "L"
  val KEY_M: Key            = 77 -> "M"
  val KEY_N: Key            = 78 -> "N"
  val KEY_O: Key            = 79 -> "O"
  val KEY_P: Key            = 80 -> "P"
  val KEY_Q: Key            = 81 -> "Q"
  val KEY_R: Key            = 82 -> "R"
  val KEY_S: Key            = 83 -> "S"
  val KEY_T: Key            = 84 -> "T"
  val KEY_U: Key            = 85 -> "U"
  val KEY_V: Key            = 86 -> "V"
  val KEY_W: Key            = 87 -> "W"
  val KEY_X: Key            = 88 -> "X"
  val KEY_Y: Key            = 89 -> "Y"
  val KEY_Z: Key            = 90 -> "Z"
  val LEFT_WINDOW_KEY: Key  = 91
  val RIGHT_WINDOW_KEY: Key = 92
  val SELECT_KEY: Key       = 93
  val NUMPAD_0: Key         = 96 -> "0"
  val NUMPAD_1: Key         = 97 -> "1"
  val NUMPAD_2: Key         = 98 -> "2"
  val NUMPAD_3: Key         = 99 -> "3"
  val NUMPAD_4: Key         = 100 -> "4"
  val NUMPAD_5: Key         = 101 -> "5"
  val NUMPAD_6: Key         = 102 -> "6"
  val NUMPAD_7: Key         = 103 -> "7"
  val NUMPAD_8: Key         = 104 -> "8"
  val NUMPAD_9: Key         = 105 -> "9"
  val MULTIPLY: Key         = 106 -> "*"
  val ADD: Key              = 107 -> "+"
  val SUBTRACT: Key         = 109 -> "-"
  val DECIMAL_POINT: Key    = 110 -> "."
  val DIVIDE: Key           = 111 -> "/"
  val F1: Key               = 112
  val F2: Key               = 113
  val F3: Key               = 114
  val F4: Key               = 115
  val F5: Key               = 116
  val F6: Key               = 117
  val F7: Key               = 118
  val F8: Key               = 119
  val F9: Key               = 120
  val F10: Key              = 121
  val F11: Key              = 122
  val F12: Key              = 123
  val NUM_LOCK: Key         = 144
  val SCROLL_LOCK: Key      = 145
  val SEMI_COLON: Key       = 186 -> ";"
  val EQUAL_SIGN: Key       = 187 -> "="
  val COMMA: Key            = 188 -> ","
  val DASH: Key             = 189 -> "-"
  val PERIOD: Key           = 190 -> "."
  val FORWARD_SLASH: Key    = 191 -> "/"
  val GRAVE_ACCENT: Key     = 192
  val OPEN_BRACKET: Key     = 219 -> "("
  val BACK_SLASH: Key       = 220 -> "\\"
  val CLOSE_BRAKET: Key     = 221 -> ")"
  val SINGLE_QUOTE: Key     = 222 -> "\'"

  val printable: List[Key] =
    List(
      SPACE,
      KEY_0,
      KEY_1,
      KEY_2,
      KEY_3,
      KEY_4,
      KEY_5,
      KEY_6,
      KEY_7,
      KEY_8,
      KEY_9,
      KEY_A,
      KEY_B,
      KEY_C,
      KEY_D,
      KEY_E,
      KEY_F,
      KEY_G,
      KEY_H,
      KEY_I,
      KEY_J,
      KEY_K,
      KEY_L,
      KEY_M,
      KEY_N,
      KEY_O,
      KEY_P,
      KEY_Q,
      KEY_R,
      KEY_S,
      KEY_T,
      KEY_U,
      KEY_V,
      KEY_W,
      KEY_X,
      KEY_Y,
      KEY_Z,
      NUMPAD_0,
      NUMPAD_1,
      NUMPAD_2,
      NUMPAD_3,
      NUMPAD_4,
      NUMPAD_5,
      NUMPAD_6,
      NUMPAD_7,
      NUMPAD_8,
      NUMPAD_9,
      MULTIPLY,
      ADD,
      SUBTRACT,
      DECIMAL_POINT,
      DIVIDE,
      SEMI_COLON,
      EQUAL_SIGN,
      COMMA,
      DASH,
      PERIOD,
      FORWARD_SLASH,
      GRAVE_ACCENT,
      OPEN_BRACKET,
      BACK_SLASH,
      CLOSE_BRAKET,
      SINGLE_QUOTE
    )
}
