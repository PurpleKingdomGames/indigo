package indigo.shared.constants

final case class Key(code: KeyCode, key: String, location: KeyLocation) derives CanEqual {
  def isPrintable: Boolean =
    (key != "") && KeyCode.printable.contains(this.code)

  def isNumeric: Boolean =
    code match {
      case KeyCode.Digit0 | KeyCode.Digit1 | KeyCode.Digit2 | KeyCode.Digit3 | KeyCode.Digit4 | KeyCode.Digit5 |
          KeyCode.Digit6 | KeyCode.Digit7 | KeyCode.Digit8 | KeyCode.Digit9 =>
        true
      case _ =>
        false
    }

  def asNumeric: Option[Int] =
    code match {
      case KeyCode.Digit0 => Some(0)
      case KeyCode.Digit1 => Some(1)
      case KeyCode.Digit2 => Some(2)
      case KeyCode.Digit3 => Some(3)
      case KeyCode.Digit4 => Some(4)
      case KeyCode.Digit5 => Some(5)
      case KeyCode.Digit6 => Some(6)
      case KeyCode.Digit7 => Some(7)
      case KeyCode.Digit8 => Some(8)
      case KeyCode.Digit9 => Some(9)
      case _              => None
    }

  // DO NOT REMOVE
  def ===(other: Key): Boolean =
    if (other.location == KeyLocation.Invariant || location == KeyLocation.Invariant)
      code == other.code
    else
      other.code == this.code && other.location == this.location

  // DO NOT REMOVE
  // This is not an accident, or it was, but now it's a feature...
  // This allows us to pattern match on specific key instances.
  // Hopefully we can replace with Scala 3 enums or something...
  // @SuppressWarnings(Array("scalafix:DisableSyntax.isInstanceOf"))
  override def equals(that: Any): Boolean =
    that match {
      case that: Key =>
        that.isInstanceOf[Key] && this === that
      case _ => false
    }

  override def hashCode: Int = code.hashCode()
}

object Key {
  def apply(code: KeyCode): Key =
    Key(code, "", KeyLocation.Invariant)

  def apply(code: KeyCode, location: KeyLocation): Key =
    Key(code, "", location)

  implicit private def keyCodeToKey(i: KeyCode): Key =
    Key(i, "", KeyLocation.Invariant)

  implicit private def keyCodeToKey(t: (KeyCode, String)): Key =
    Key(t._1, t._2, KeyLocation.Invariant)

  val BACKSPACE: Key        = KeyCode.Backspace
  val TAB: Key              = KeyCode.Tab            -> "\t"
  val ENTER: Key            = KeyCode.Enter          -> "\n"
  val SHIFT: Key            = KeyCode.ShiftLeft
  val CTRL: Key             = KeyCode.ControlLeft
  val ALT: Key              = KeyCode.AltLeft
  val PAUSE_BREAK: Key      = KeyCode.Pause
  val CAPS_LOCK: Key        = KeyCode.CapsLock
  val ESCAPE: Key           = KeyCode.Escape
  val SPACE: Key            = KeyCode.Space          -> " "
  val PAGE_UP: Key          = KeyCode.PageUp
  val PAGE_DOWN: Key        = KeyCode.PageDown
  val END: Key              = KeyCode.End
  val HOME: Key             = KeyCode.Home
  val ARROW_LEFT: Key       = KeyCode.ArrowLeft
  val ARROW_UP: Key         = KeyCode.ArrowUp
  val ARROW_RIGHT: Key      = KeyCode.ArrowRight
  val ARROW_DOWN: Key       = KeyCode.ArrowDown
  val INSERT: Key           = KeyCode.Insert
  val DELETE: Key           = KeyCode.Delete
  val KEY_0: Key            = KeyCode.Digit0         -> "0"
  val KEY_1: Key            = KeyCode.Digit1         -> "1"
  val KEY_2: Key            = KeyCode.Digit2         -> "2"
  val KEY_3: Key            = KeyCode.Digit3         -> "3"
  val KEY_4: Key            = KeyCode.Digit4         -> "4"
  val KEY_5: Key            = KeyCode.Digit5         -> "5"
  val KEY_6: Key            = KeyCode.Digit6         -> "6"
  val KEY_7: Key            = KeyCode.Digit7         -> "7"
  val KEY_8: Key            = KeyCode.Digit8         -> "8"
  val KEY_9: Key            = KeyCode.Digit9         -> "9"
  val KEY_A: Key            = KeyCode.KeyA           -> "A"
  val KEY_B: Key            = KeyCode.KeyB           -> "B"
  val KEY_C: Key            = KeyCode.KeyC           -> "C"
  val KEY_D: Key            = KeyCode.KeyD           -> "D"
  val KEY_E: Key            = KeyCode.KeyE           -> "E"
  val KEY_F: Key            = KeyCode.KeyF           -> "F"
  val KEY_G: Key            = KeyCode.KeyG           -> "G"
  val KEY_H: Key            = KeyCode.KeyH           -> "H"
  val KEY_I: Key            = KeyCode.KeyI           -> "I"
  val KEY_J: Key            = KeyCode.KeyJ           -> "J"
  val KEY_K: Key            = KeyCode.KeyK           -> "K"
  val KEY_L: Key            = KeyCode.KeyL           -> "L"
  val KEY_M: Key            = KeyCode.KeyM           -> "M"
  val KEY_N: Key            = KeyCode.KeyN           -> "N"
  val KEY_O: Key            = KeyCode.KeyO           -> "O"
  val KEY_P: Key            = KeyCode.KeyP           -> "P"
  val KEY_Q: Key            = KeyCode.KeyQ           -> "Q"
  val KEY_R: Key            = KeyCode.KeyR           -> "R"
  val KEY_S: Key            = KeyCode.KeyS           -> "S"
  val KEY_T: Key            = KeyCode.KeyT           -> "T"
  val KEY_U: Key            = KeyCode.KeyU           -> "U"
  val KEY_V: Key            = KeyCode.KeyV           -> "V"
  val KEY_W: Key            = KeyCode.KeyW           -> "W"
  val KEY_X: Key            = KeyCode.KeyX           -> "X"
  val KEY_Y: Key            = KeyCode.KeyY           -> "Y"
  val KEY_Z: Key            = KeyCode.KeyZ           -> "Z"
  val LEFT_META_KEY: Key    = KeyCode.MetaLeft
  val RIGHT_WINDOW_KEY: Key = KeyCode.MetaRight
  val SELECT_KEY: Key       = KeyCode.Select
  val NUMPAD_0: Key         = KeyCode.Numpad0        -> "0"
  val NUMPAD_1: Key         = KeyCode.Numpad1        -> "1"
  val NUMPAD_2: Key         = KeyCode.Numpad2        -> "2"
  val NUMPAD_3: Key         = KeyCode.Numpad3        -> "3"
  val NUMPAD_4: Key         = KeyCode.Numpad4        -> "4"
  val NUMPAD_5: Key         = KeyCode.Numpad5        -> "5"
  val NUMPAD_6: Key         = KeyCode.Numpad6        -> "6"
  val NUMPAD_7: Key         = KeyCode.Numpad7        -> "7"
  val NUMPAD_8: Key         = KeyCode.Numpad8        -> "8"
  val NUMPAD_9: Key         = KeyCode.Numpad9        -> "9"
  val MULTIPLY: Key         = KeyCode.NumpadMultiply -> "*"
  val ADD: Key              = KeyCode.NumpadAdd      -> "+"
  val SUBTRACT: Key         = KeyCode.NumpadSubtract -> "-"
  val DECIMAL_POINT: Key    = KeyCode.NumpadDecimal  -> "."
  val DIVIDE: Key           = KeyCode.NumpadDivide   -> "/"
  val F1: Key               = KeyCode.F1
  val F2: Key               = KeyCode.F2
  val F3: Key               = KeyCode.F3
  val F4: Key               = KeyCode.F4
  val F5: Key               = KeyCode.F5
  val F6: Key               = KeyCode.F6
  val F7: Key               = KeyCode.F7
  val F8: Key               = KeyCode.F8
  val F9: Key               = KeyCode.F9
  val F10: Key              = KeyCode.F10
  val F11: Key              = KeyCode.F11
  val F12: Key              = KeyCode.F12
  val NUM_LOCK: Key         = KeyCode.NumLock
  val SCROLL_LOCK: Key      = KeyCode.ScrollLock
  val SEMI_COLON: Key       = KeyCode.Semicolon      -> ";"
  val EQUAL_SIGN: Key       = KeyCode.Equal          -> "="
  val COMMA: Key            = KeyCode.Comma          -> ","
  val DASH: Key             = KeyCode.Minus          -> "-"
  val PERIOD: Key           = KeyCode.Period         -> "."
  val FORWARD_SLASH: Key    = KeyCode.Slash          -> "/"
  val BACK_QUOTE: Key       = KeyCode.Backquote      -> "`"
  val OPEN_BRACKET: Key     = KeyCode.BracketLeft    -> "("
  val BACK_SLASH: Key       = KeyCode.Backslash      -> "\\"
  val CLOSE_BRACKET: Key    = KeyCode.BracketRight   -> ")"
  val SINGLE_QUOTE: Key     = KeyCode.Quote          -> "\'"
}
