package indigo.entry

import indigo.GlobalEvent
import indigo.Outcome
import indigo.SceneUpdateFragment
import indigo.shared.subsystems.SubSystemsRegister
import indigo.shared.subsystems.SubSystemFrameContext._
import indigo.shared.FrameContext

object GameWithSubSystems {

  def update[StartUpData, Model](
      subSystemsRegister: SubSystemsRegister,
      modelUpdate: (FrameContext[StartUpData], Model) => GlobalEvent => Outcome[Model]
  ): (FrameContext[StartUpData], Model) => GlobalEvent => Outcome[Model] =
    (frameContext, model) => e => Outcome.merge(modelUpdate(frameContext, model)(e), subSystemsRegister.update(frameContext.forSubSystems)(e)) { case (m, _) => m }

  def updateViewModel[StartUpData, Model, ViewModel](
      viewModelUpdate: (FrameContext[StartUpData], Model, ViewModel) => GlobalEvent => Outcome[ViewModel]
  ): (FrameContext[StartUpData], Model, ViewModel) => GlobalEvent => Outcome[ViewModel] =
    (frameContext, model, viewModel) => e => viewModelUpdate(frameContext, model, viewModel)(e)

  def present[StartUpData, Model, ViewModel](
      subSystemsRegister: SubSystemsRegister,
      viewPresent: (FrameContext[StartUpData], Model, ViewModel) => SceneUpdateFragment
  ): (FrameContext[StartUpData], Model, ViewModel) => SceneUpdateFragment =
    (frameContext, model, viewModel) => viewPresent(frameContext, model, viewModel) |+| subSystemsRegister.render(frameContext.forSubSystems)

}
