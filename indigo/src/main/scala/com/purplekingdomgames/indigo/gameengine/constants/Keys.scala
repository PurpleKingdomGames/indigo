package com.purplekingdomgames.indigo.gameengine.constants

import scala.language.implicitConversions

case class KeyCode(code: Int, printableCharacter: String) {
  def ===(other: KeyCode): Boolean =
    code == other.code

  def isPrintable: Boolean =
    printableCharacter != ""
}

object Keys {

  implicit private def intToKeyCode(i: Int): KeyCode =
    KeyCode(i, "")

  implicit private def intToKeyCode(t: (Int, String)): KeyCode =
    KeyCode(t._1, t._2)

  val BACKSPACE: KeyCode = 8
  val TAB: KeyCode = 9
  val ENTER: KeyCode = 13 -> "\n"
  val SHIFT: KeyCode = 16
  val CTRL: KeyCode = 17
  val ALT: KeyCode = 18
  val PAUSE_BREAK: KeyCode = 19
  val CAPS_LOCK: KeyCode = 20
  val ESCAPE: KeyCode = 27
  val SPACE: KeyCode = 32 -> " "
  val PAGE_UP: KeyCode = 33
  val PAGE_DOWN: KeyCode = 34
  val END: KeyCode = 35
  val HOME: KeyCode = 36
  val LEFT_ARROW: KeyCode = KeyCode(37, "")
  val UP_ARROW: KeyCode = 38
  val RIGHT_ARROW: KeyCode = 39
  val DOWN_ARROW: KeyCode = 40
  val INSERT: KeyCode = 45
  val DELETE: KeyCode = 46
  val KEY_0: KeyCode = 48 -> "0"
  val KEY_1: KeyCode = 49 -> "1"
  val KEY_2: KeyCode = 50 -> "2"
  val KEY_3: KeyCode = 51 -> "3"
  val KEY_4: KeyCode = 52 -> "4"
  val KEY_5: KeyCode = 53 -> "5"
  val KEY_6: KeyCode = 54 -> "6"
  val KEY_7: KeyCode = 55 -> "7"
  val KEY_8: KeyCode = 56 -> "8"
  val KEY_9: KeyCode = 57 -> "9"
  val KEY_A: KeyCode = 65 -> "A"
  val KEY_B: KeyCode = 66 -> "B"
  val KEY_C: KeyCode = 67 -> "C"
  val KEY_D: KeyCode = 68 -> "D"
  val KEY_E: KeyCode = 69 -> "E"
  val KEY_F: KeyCode = 70 -> "F"
  val KEY_G: KeyCode = 71 -> "G"
  val KEY_H: KeyCode = 72 -> "H"
  val KEY_I: KeyCode = 73 -> "I"
  val KEY_J: KeyCode = 74 -> "J"
  val KEY_K: KeyCode = 75 -> "K"
  val KEY_L: KeyCode = 76 -> "L"
  val KEY_M: KeyCode = 77 -> "M"
  val KEY_N: KeyCode = 78 -> "N"
  val KEY_O: KeyCode = 79 -> "O"
  val KEY_P: KeyCode = 80 -> "P"
  val KEY_Q: KeyCode = 81 -> "Q"
  val KEY_R: KeyCode = 82 -> "R"
  val KEY_S: KeyCode = 83 -> "S"
  val KEY_T: KeyCode = 84 -> "T"
  val KEY_U: KeyCode = 85 -> "U"
  val KEY_V: KeyCode = 86 -> "V"
  val KEY_W: KeyCode = 87 -> "W"
  val KEY_X: KeyCode = 88 -> "X"
  val KEY_Y: KeyCode = 89 -> "Y"
  val KEY_Z: KeyCode = 90 -> "Z"
  val LEFT_WINDOW_KEY: KeyCode = 91
  val RIGHT_WINDOW_KEY: KeyCode = 92
  val SELECT_KEY: KeyCode = 93
  val NUMPAD_0: KeyCode = 96 -> "0"
  val NUMPAD_1: KeyCode = 97 -> "1"
  val NUMPAD_2: KeyCode = 98 -> "2"
  val NUMPAD_3: KeyCode = 99 -> "3"
  val NUMPAD_4: KeyCode = 100 -> "4"
  val NUMPAD_5: KeyCode = 101 -> "5"
  val NUMPAD_6: KeyCode = 102 -> "6"
  val NUMPAD_7: KeyCode = 103 -> "7"
  val NUMPAD_8: KeyCode = 104 -> "8"
  val NUMPAD_9: KeyCode = 105 -> "9"
  val MULTIPLY: KeyCode = 106 -> "*"
  val ADD: KeyCode = 107 -> "+"
  val SUBTRACT: KeyCode = 109 -> "-"
  val DECIMAL_POINT: KeyCode = 110 -> "."
  val DIVIDE: KeyCode = 111 -> "/"
  val F1: KeyCode = 112
  val F2: KeyCode = 113
  val F3: KeyCode = 114
  val F4: KeyCode = 115
  val F5: KeyCode = 116
  val F6: KeyCode = 117
  val F7: KeyCode = 118
  val F8: KeyCode = 119
  val F9: KeyCode = 120
  val F10: KeyCode = 121
  val F11: KeyCode = 122
  val F12: KeyCode = 123
  val NUM_LOCK: KeyCode = 144
  val SCROLL_LOCK: KeyCode = 145
  val SEMI_COLON: KeyCode = 186 -> ";"
  val EQUAL_SIGN: KeyCode = 187 -> "="
  val COMMA: KeyCode = 188 -> ","
  val DASH: KeyCode = 189 -> "-"
  val PERIOD: KeyCode = 190 -> "."
  val FORWARD_SLASH: KeyCode = 191 -> "/"
  val GRAVE_ACCENT: KeyCode = 192
  val OPEN_BRACKET: KeyCode = 219 -> "("
  val BACK_SLASH: KeyCode = 220 -> "\\"
  val CLOSE_BRAKET: KeyCode = 221 -> ")"
  val SINGLE_QUOTE: KeyCode = 222 -> "\'"

  val safeKeyCodes: List[KeyCode] =
    List(
      BACKSPACE,
      TAB,
      ENTER,
      SHIFT,
      CTRL,
      ALT,
      PAUSE_BREAK,
      CAPS_LOCK,
      ESCAPE,
      SPACE,
      PAGE_UP,
      PAGE_DOWN,
      END,
      HOME,
      LEFT_ARROW,
      UP_ARROW,
      RIGHT_ARROW,
      DOWN_ARROW,
      INSERT,
      DELETE,
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
      LEFT_WINDOW_KEY,
      RIGHT_WINDOW_KEY,
      SELECT_KEY,
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
      F1,
      F2,
      F3,
      F4,
      F5,
      F6,
      F7,
      F8,
      F9,
      F10,
      F11,
      F12,
      NUM_LOCK,
      SCROLL_LOCK,
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

  def codeToKeyCode(code: Int): Option[KeyCode] =
    safeKeyCodes.find(_.code == code)

  def isSafeKeyCode(code: Int): Boolean =
    codeToKeyCode(code).isDefined

  def keyCodeToChar(code: Int): String =
    codeToKeyCode(code).map(_.printableCharacter).getOrElse("")
}
