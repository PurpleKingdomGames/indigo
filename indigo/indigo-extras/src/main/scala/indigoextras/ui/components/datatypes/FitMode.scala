package indigoextras.ui.components.datatypes

/** Fit mode describes how dynamic bounds decide to expand and shink based on their contents or the available space, in
  * one dimension, i.e. width or height. This allows us to say "fill the available width but shrink to fit the contents
  * vertically."
  */
enum FitMode derives CanEqual:

  /** Fills the available space in one dimension, this is like BoundsType.Inherit in only one dimension.
    */
  case Available

  /** Fills the available space in one dimension, plus an offset amount, which can be negative.
    */
  case Offset(offset: Int)

  /** Fits the size of the group's contents in one dimension.
    */
  case Content

  /** Fixes the size in one dimension.
    */
  case Fixed(units: Int)

  /** Fills the available space in one dimension, but only up to a certain percentage of the available space.
    */
  case Relative(amount: Double)
