package indigoextras.ui.components.datatypes

/** Overflow describes what to do in the event that a component's layout position is beyond the bounds of the
  * `ComponentGroup`.
  */
enum Overflow derives CanEqual:
  case Hidden, Wrap
