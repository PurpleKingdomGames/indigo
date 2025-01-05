package indigoextras.ui.components.datatypes

enum SwitchState derives CanEqual:
  case On, Off

  def toggle: SwitchState =
    this match
      case On  => Off
      case Off => On

  def toBoolean: Boolean =
    this match
      case On  => true
      case Off => false

  def isOn: Boolean =
    this == On

  def isOff: Boolean =
    this == Off
