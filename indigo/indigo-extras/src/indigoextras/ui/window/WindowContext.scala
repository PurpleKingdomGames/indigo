package indigoextras.ui.window

import indigoextras.ui.datatypes.Bounds
import indigoextras.ui.datatypes.UIContext

final case class WindowContext[ReferenceData](
    context: UIContext[ReferenceData],
    bounds: Bounds,
    hasFocus: Boolean,
    pointerIsOver: Boolean,
    magnification: Int
)

object WindowContext:

  def from[ReferenceData](
      context: UIContext[ReferenceData],
      model: Window[?, ?],
      viewModel: WindowViewModel[?]
  ): WindowContext[ReferenceData] =
    WindowContext(
      context,
      model.bounds(context.frame.viewport.toSize, context.magnification),
      model.hasFocus,
      viewModel.pointerIsOver,
      viewModel.magnification
    )
