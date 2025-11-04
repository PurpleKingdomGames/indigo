package indigo.shared.constants

import indigo.shared.collections.Batch

enum KeyCode(val value: String) derives CanEqual:
  case Backspace          extends KeyCode("Backspace")
  case Tab                extends KeyCode("Tab")
  case Enter              extends KeyCode("Enter")
  case PauseBreak         extends KeyCode("PauseBreak")
  case CapsLock           extends KeyCode("CapsLock")
  case Escape             extends KeyCode("Escape")
  case Space              extends KeyCode("Space")
  case PageUp             extends KeyCode("PageUp")
  case PageDown           extends KeyCode("PageDown")
  case End                extends KeyCode("End")
  case Home               extends KeyCode("Home")
  case ArrowLeft          extends KeyCode("ArrowLeft")
  case ArrowUp            extends KeyCode("ArrowUp")
  case ArrowRight         extends KeyCode("ArrowRight")
  case ArrowDown          extends KeyCode("ArrowDown")
  case Insert             extends KeyCode("Insert")
  case Delete             extends KeyCode("Delete")
  case Minus              extends KeyCode("Minus")
  case Equal              extends KeyCode("Equal")
  case BracketLeft        extends KeyCode("BracketLeft")
  case BracketRight       extends KeyCode("BracketRight")
  case ControlLeft        extends KeyCode("ControlLeft")
  case ControlRight       extends KeyCode("ControlRight")
  case Semicolon          extends KeyCode("Semicolon")
  case Quote              extends KeyCode("Quote")
  case Backquote          extends KeyCode("Backquote")
  case ShiftLeft          extends KeyCode("ShiftLeft")
  case Backslash          extends KeyCode("Backslash")
  case Comma              extends KeyCode("Comma")
  case Period             extends KeyCode("Period")
  case Slash              extends KeyCode("Slash")
  case ShiftRight         extends KeyCode("ShiftRight")
  case AltLeft            extends KeyCode("AltLeft")
  case AltRight           extends KeyCode("AltRight")
  case Pause              extends KeyCode("Pause")
  case ScrollLock         extends KeyCode("ScrollLock")
  case Props              extends KeyCode("Props")
  case Undo               extends KeyCode("Undo")
  case Select             extends KeyCode("Select")
  case Copy               extends KeyCode("Copy")
  case Open               extends KeyCode("Open")
  case Paste              extends KeyCode("Paste")
  case Find               extends KeyCode("Find")
  case Cut                extends KeyCode("Cut")
  case Help               extends KeyCode("Help")
  case Numpad0            extends KeyCode("Numpad0")
  case Numpad1            extends KeyCode("Numpad1")
  case Numpad2            extends KeyCode("NumPad2")
  case Numpad3            extends KeyCode("Numpad3")
  case Numpad4            extends KeyCode("NumPad4")
  case Numpad5            extends KeyCode("Numpad5")
  case Numpad6            extends KeyCode("NumPad6")
  case Numpad7            extends KeyCode("Numpad7")
  case Numpad8            extends KeyCode("NumPad8")
  case Numpad9            extends KeyCode("NumPad9")
  case NumpadDecimal      extends KeyCode("NumpadDecimal")
  case NumpadMultiply     extends KeyCode("NumpadMultiply")
  case NumpadEqual        extends KeyCode("NumpadEqual")
  case NumpadComma        extends KeyCode("NumpadComma")
  case NumpadEnter        extends KeyCode("NumpadEnter")
  case NumpadDivide       extends KeyCode("NumpadDivide")
  case NumpadAdd          extends KeyCode("NumpadAdd")
  case NumpadSubtract     extends KeyCode("NumpadSubtract")
  case NumpadParenLeft    extends KeyCode("NumpadParenLeft")
  case NumpadParenRight   extends KeyCode("NumpadParenRight")
  case IntlBackslash      extends KeyCode("IntlBackslash")
  case KanaMode           extends KeyCode("KanaMode")
  case Convert            extends KeyCode("Convert")
  case NonConvert         extends KeyCode("NonConvert")
  case Lang1              extends KeyCode("Lang1")
  case Lang2              extends KeyCode("Lang2")
  case Lang3              extends KeyCode("Lang3")
  case Lang4              extends KeyCode("Lang4")
  case Lang5              extends KeyCode("Lang5")
  case IntlRo             extends KeyCode("IntlRo")
  case IntlYen            extends KeyCode("IntlYen")
  case MediaTrackPrevious extends KeyCode("MediaTrackPrevious")
  case MediaTrackNext     extends KeyCode("MediaTrackNext")
  case MediaPlayPause     extends KeyCode("MediaPlayPause")
  case MediaStop          extends KeyCode("MediaStop")
  case MediaSelect        extends KeyCode("MediaSelect")
  case AudioVolumeMute    extends KeyCode("AudioVolumeMute")
  case AudioVolumeDown    extends KeyCode("AudioVolumeDown")
  case AudioVolumeUp      extends KeyCode("AudioVolumeUp")
  case LaunchApp1         extends KeyCode("LaunchApp1")
  case LaunchApp2         extends KeyCode("LaunchApp2")
  case LaunchMail         extends KeyCode("LaunchMail")
  case BrowserHome        extends KeyCode("BrowserHome")
  case BrowserSearch      extends KeyCode("BrowserSearch")
  case BrowserFavorites   extends KeyCode("BrowserFavorites")
  case BrowserRefresh     extends KeyCode("BrowserRefresh")
  case BrowserStop        extends KeyCode("BrowserStop")
  case BrowserForward     extends KeyCode("BrowserForward")
  case BrowserBack        extends KeyCode("BrowserBack")
  case PrintScreen        extends KeyCode("PrintScreen")
  case NumLock            extends KeyCode("NumLock")
  case MetaLeft           extends KeyCode("MetaLeft")
  case MetaRight          extends KeyCode("MetaRight")
  case ContextMenu        extends KeyCode("ContextMenu")
  case Again              extends KeyCode("Again")
  case Power              extends KeyCode("Power")
  case Sleep              extends KeyCode("Sleep")
  case WakeUp             extends KeyCode("WakeUp")
  case Eject              extends KeyCode("Eject")
  case F1                 extends KeyCode("F1")
  case F2                 extends KeyCode("F2")
  case F3                 extends KeyCode("F3")
  case F4                 extends KeyCode("F4")
  case F5                 extends KeyCode("F5")
  case F6                 extends KeyCode("F6")
  case F7                 extends KeyCode("F7")
  case F8                 extends KeyCode("F8")
  case F9                 extends KeyCode("F9")
  case F10                extends KeyCode("F10")
  case F11                extends KeyCode("F11")
  case F12                extends KeyCode("F12")
  case F13                extends KeyCode("F13")
  case F14                extends KeyCode("F14")
  case F15                extends KeyCode("F15")
  case F16                extends KeyCode("F16")
  case F17                extends KeyCode("F17")
  case F18                extends KeyCode("F18")
  case F19                extends KeyCode("F19")
  case F20                extends KeyCode("F20")
  case F21                extends KeyCode("F21")
  case F22                extends KeyCode("F22")
  case F23                extends KeyCode("F23")
  case F24                extends KeyCode("F24")
  case Digit0             extends KeyCode("Digit0")
  case Digit1             extends KeyCode("Digit1")
  case Digit2             extends KeyCode("Digit2")
  case Digit3             extends KeyCode("Digit3")
  case Digit4             extends KeyCode("Digit4")
  case Digit5             extends KeyCode("Digit5")
  case Digit6             extends KeyCode("Digit6")
  case Digit7             extends KeyCode("Digit7")
  case Digit8             extends KeyCode("Digit8")
  case Digit9             extends KeyCode("Digit9")
  case KeyA               extends KeyCode("KeyA")
  case KeyB               extends KeyCode("KeyB")
  case KeyC               extends KeyCode("KeyC")
  case KeyD               extends KeyCode("KeyD")
  case KeyE               extends KeyCode("KeyE")
  case KeyF               extends KeyCode("KeyF")
  case KeyG               extends KeyCode("KeyG")
  case KeyH               extends KeyCode("KeyH")
  case KeyI               extends KeyCode("KeyI")
  case KeyJ               extends KeyCode("KeyJ")
  case KeyK               extends KeyCode("KeyK")
  case KeyL               extends KeyCode("KeyL")
  case KeyM               extends KeyCode("KeyM")
  case KeyN               extends KeyCode("KeyN")
  case KeyO               extends KeyCode("KeyO")
  case KeyP               extends KeyCode("KeyP")
  case KeyQ               extends KeyCode("KeyQ")
  case KeyR               extends KeyCode("KeyR")
  case KeyS               extends KeyCode("KeyS")
  case KeyT               extends KeyCode("KeyT")
  case KeyU               extends KeyCode("KeyU")
  case KeyV               extends KeyCode("KeyV")
  case KeyW               extends KeyCode("KeyW")
  case KeyX               extends KeyCode("KeyX")
  case KeyY               extends KeyCode("KeyY")
  case KeyZ               extends KeyCode("KeyZ")
  case Unidentified       extends KeyCode("Unidentified")

object KeyCode:
  def fromString(value: String) = value match {
    case "VolumeUp"   => KeyCode.AudioVolumeUp
    case "VolumeDown" => KeyCode.AudioVolumeDown
    case "VolumeMute" => KeyCode.AudioVolumeMute
    case _ =>
      KeyCode.values
        .find(_.value == value)
        .getOrElse(KeyCode.Unidentified)
  }

  lazy val printable: Batch[KeyCode] = Batch(
    KeyCode.Space,
    KeyCode.Digit0,
    KeyCode.Digit1,
    KeyCode.Digit2,
    KeyCode.Digit3,
    KeyCode.Digit4,
    KeyCode.Digit5,
    KeyCode.Digit6,
    KeyCode.Digit7,
    KeyCode.Digit8,
    KeyCode.Digit9,
    KeyCode.KeyA,
    KeyCode.KeyB,
    KeyCode.KeyC,
    KeyCode.KeyD,
    KeyCode.KeyE,
    KeyCode.KeyF,
    KeyCode.KeyG,
    KeyCode.KeyH,
    KeyCode.KeyI,
    KeyCode.KeyJ,
    KeyCode.KeyK,
    KeyCode.KeyL,
    KeyCode.KeyM,
    KeyCode.KeyN,
    KeyCode.KeyO,
    KeyCode.KeyP,
    KeyCode.KeyQ,
    KeyCode.KeyR,
    KeyCode.KeyS,
    KeyCode.KeyT,
    KeyCode.KeyU,
    KeyCode.KeyV,
    KeyCode.KeyW,
    KeyCode.KeyX,
    KeyCode.KeyY,
    KeyCode.KeyZ,
    KeyCode.Numpad0,
    KeyCode.Numpad1,
    KeyCode.Numpad2,
    KeyCode.Numpad3,
    KeyCode.Numpad4,
    KeyCode.Numpad5,
    KeyCode.Numpad6,
    KeyCode.Numpad7,
    KeyCode.Numpad8,
    KeyCode.Numpad9,
    KeyCode.NumpadDecimal,
    KeyCode.NumpadMultiply,
    KeyCode.NumpadEqual,
    KeyCode.NumpadComma,
    KeyCode.NumpadDivide,
    KeyCode.NumpadAdd,
    KeyCode.NumpadEnter,
    KeyCode.NumpadSubtract,
    KeyCode.NumpadParenLeft,
    KeyCode.NumpadParenRight,
    KeyCode.Backslash,
    KeyCode.Comma,
    KeyCode.Period,
    KeyCode.Slash,
    KeyCode.Minus,
    KeyCode.Equal,
    KeyCode.BracketLeft,
    KeyCode.BracketRight,
    KeyCode.Semicolon,
    KeyCode.Quote,
    KeyCode.Backquote,
    KeyCode.Tab,
    KeyCode.Enter
  )
