package indigoextras.ui.window

import indigo.*
import indigoextras.ui.datatypes.UIContext

object WindowView:

  def present[A, ReferenceData](
      context: UIContext[ReferenceData],
      model: Window[A, ReferenceData],
      viewModel: WindowViewModel[ReferenceData]
  ): Outcome[Layer] =
    model.component
      .present(
        context.withParentBounds(model.bounds),
        model.content
      )
      .flatMap {
        case l: Layer.Content =>
          model.background(WindowContext.from(model, viewModel)).map { windowChrome =>
            Layer.Stack(
              windowChrome,
              l
            )
          }

        case l: Layer.Stack =>
          model.background(WindowContext.from(model, viewModel)).map { windowChrome =>
            l.prepend(windowChrome)
          }
      }
