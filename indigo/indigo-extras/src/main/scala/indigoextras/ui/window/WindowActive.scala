package indigoextras.ui.window

enum WindowActive derives CanEqual:
  case Active, InActive

  def isActive: Boolean =
    this match
      case Active   => true
      case InActive => false

  def isInActive: Boolean =
    !isActive
