package indigoextras.ui.window

import indigoextras.ui.datatypes.Bounds

final case class WindowContext(
    bounds: Bounds,
    hasFocus: Boolean,
    pointerIsOver: Boolean,
    magnification: Int
)

object WindowContext:

  def from(model: Window[?, ?], viewModel: WindowViewModel[?]): WindowContext =
    WindowContext(
      model.bounds,
      model.hasFocus,
      viewModel.pointerIsOver,
      viewModel.magnification
    )
