package indigoextras.ui.components.datatypes

final case class ScrollOptions(
    scrollMode: ScrollMode,
    minScrollSpeed: Int,
    maxScrollSpeed: Int
):

  def withScrollMode(value: ScrollMode): ScrollOptions =
    copy(scrollMode = value)

  def withMaxScrollSpeed(value: Int): ScrollOptions =
    copy(maxScrollSpeed = value)

  def withMinScrollSpeed(value: Int): ScrollOptions =
    copy(minScrollSpeed = value)

  def isEnabled: Boolean =
    scrollMode != ScrollMode.None

  def isDisabled: Boolean =
    scrollMode == ScrollMode.None

object ScrollOptions:
  val default: ScrollOptions = ScrollOptions(ScrollMode.Vertical, 1, 10)

enum ScrollMode derives CanEqual:
  case None
  case Vertical
