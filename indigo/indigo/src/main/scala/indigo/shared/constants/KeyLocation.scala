package indigo.shared.constants

enum KeyLocation derives CanEqual:
  case Standard
  case Left
  case Right
  case Numpad
  case Invariant

object KeyLocation:
  def fromInt(i: Int): KeyLocation =
    i match
      case 0 => KeyLocation.Standard
      case 1 => KeyLocation.Left
      case 2 => KeyLocation.Right
      case 3 => KeyLocation.Numpad
      case _ => KeyLocation.Invariant
