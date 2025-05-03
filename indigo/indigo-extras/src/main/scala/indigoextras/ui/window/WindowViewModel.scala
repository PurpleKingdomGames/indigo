package indigoextras.ui.window

import indigo.*
import indigo.syntax.*
import indigoextras.ui.datatypes.Bounds
import indigoextras.ui.datatypes.UIContext

final case class WindowViewModel[ReferenceData](
    id: WindowId,
    modelHashCode: Int,
    pointerIsOver: Boolean,
    magnification: Int
):

  def update[A](
      context: UIContext[ReferenceData],
      model: Window[A, ReferenceData],
      event: GlobalEvent
  ): Outcome[WindowViewModel[ReferenceData]] =
    WindowViewModel.updateViewModel(context, model, this)(event)

object WindowViewModel:

  def initial[ReferenceData](id: WindowId, magnification: Int): WindowViewModel[ReferenceData] =
    WindowViewModel(
      id,
      0,
      false,
      magnification
    )

  def updateViewModel[A, ReferenceData](
      context: UIContext[ReferenceData],
      model: Window[A, ReferenceData],
      viewModel: WindowViewModel[ReferenceData]
  ): GlobalEvent => Outcome[WindowViewModel[ReferenceData]] =
    case FrameTick if model.bounds.hashCode() != viewModel.modelHashCode =>
      Outcome(redraw(model, viewModel))

    case WindowInternalEvent.Redraw =>
      Outcome(redraw(model, viewModel))

    case PointerEvent.PointerMove(pt)
        if viewModel.pointerIsOver && !model
          .bounds(context.frame.viewport.toSize, context.magnification)
          .toScreenSpace(context.snapGrid)
          .contains(pt) =>
      Outcome(viewModel.copy(pointerIsOver = false))
        .addGlobalEvents(WindowEvent.PointerOut(model.id))

    case PointerEvent.PointerMove(pt)
        if !viewModel.pointerIsOver && model
          .bounds(context.frame.viewport.toSize, context.magnification)
          .toScreenSpace(context.snapGrid)
          .contains(pt) =>
      Outcome(viewModel.copy(pointerIsOver = true))
        .addGlobalEvents(WindowEvent.PointerOver(model.id))

    case _ =>
      Outcome(viewModel)

  private def redraw[A, ReferenceData](
      // context: UIContext[ReferenceData],
      model: Window[A, ReferenceData],
      viewModel: WindowViewModel[ReferenceData]
  ): WindowViewModel[ReferenceData] =
    viewModel.copy(
      modelHashCode = model.bounds.hashCode()
    )
