package indigo.entry

import indigo.GlobalEvent
import indigo.Outcome
import indigo.SceneUpdateFragment
import indigo.shared.subsystems.SubSystemsRegister
import indigo.shared.FrameContext

object GameWithSubSystems {

  def update[Model](
      subSystemsRegister: SubSystemsRegister,
      modelUpdate: (FrameContext, Model) => GlobalEvent => Outcome[Model]
  ): (FrameContext, Model) => GlobalEvent => Outcome[Model] =
    (frameContext, model) => e => Outcome.merge(modelUpdate(frameContext, model)(e), subSystemsRegister.update(frameContext)(e)) { case (m, _) => m }

  def updateViewModel[Model, ViewModel](
      viewModelUpdate: (FrameContext, Model, ViewModel) => Outcome[ViewModel]
  ): (FrameContext, Model, ViewModel) => Outcome[ViewModel] =
    (frameContext, model, viewModel) => viewModelUpdate(frameContext, model, viewModel)

  def present[Model, ViewModel](
      subSystemsRegister: SubSystemsRegister,
      viewPresent: (FrameContext, Model, ViewModel) => SceneUpdateFragment
  ): (FrameContext, Model, ViewModel) => SceneUpdateFragment =
    (frameContext, model, viewModel) => viewPresent(frameContext, model, viewModel) |+| subSystemsRegister.render(frameContext)

}
