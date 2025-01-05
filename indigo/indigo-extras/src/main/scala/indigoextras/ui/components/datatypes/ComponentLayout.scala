package indigoextras.ui.components.datatypes

/** `ComponentLayout` instructs a `ComponentGroup` how it should layout the components it contains. They are always
  * placed one after another, optionally with some padding unless the layout type is `None`.
  */
enum ComponentLayout:
  case Horizontal(padding: Padding, overflow: Overflow)
  case Vertical(padding: Padding)

  def withPadding(value: Padding): ComponentLayout =
    this match
      case Horizontal(_, overflow) => Horizontal(value, overflow)
      case Vertical(_)             => Vertical(value)

  def givePadding: Padding =
    this match
      case Horizontal(padding, _) => padding
      case Vertical(padding)      => padding

  def topPadding: Int    = givePadding.top
  def rightPadding: Int  = givePadding.right
  def bottomPadding: Int = givePadding.bottom
  def leftPadding: Int   = givePadding.left

object ComponentLayout:

  object Horizontal:
    def apply(): Horizontal =
      Horizontal(Padding.zero, Overflow.Hidden)
    def apply(padding: Padding): Horizontal =
      Horizontal(padding, Overflow.Hidden)
    def apply(overflow: Overflow): Horizontal =
      Horizontal(Padding.zero, overflow)

    extension (h: Horizontal) def withOverflow(value: Overflow): Horizontal = h.copy(overflow = value)

  object Vertical:
    def apply(): Vertical =
      Vertical(Padding.zero)
