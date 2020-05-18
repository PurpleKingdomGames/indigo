package indigogame.entry

import indigo.GlobalEvent
import indigo.Outcome
import indigo.SceneUpdateFragment
import indigoexts.subsystems.SubSystemsRegister
import indigo.shared.abstractions.syntax._
import indigo.shared.FrameContext

final class GameWithSubSystems[Model](val model: Model, val subSystemsRegister: SubSystemsRegister)
object GameWithSubSystems {

  def update[Model](
      modelUpdate: (FrameContext, Model) => GlobalEvent => Outcome[Model]
  ): (FrameContext, GameWithSubSystems[Model]) => GlobalEvent => Outcome[GameWithSubSystems[Model]] =
    (frameContext, model) =>
      e =>
        (modelUpdate(frameContext, model.model)(e), model.subSystemsRegister.update(frameContext)(e))
          .map2((m, s) => new GameWithSubSystems(m, s))

  def updateViewModel[Model, ViewModel](
      viewModelUpdate: (FrameContext, Model, ViewModel) => Outcome[ViewModel]
  ): (FrameContext, GameWithSubSystems[Model], ViewModel) => Outcome[ViewModel] =
    (frameContext, model, viewModel) => viewModelUpdate(frameContext, model.model, viewModel)

  def present[Model, ViewModel](
      viewPresent: (FrameContext, Model, ViewModel) => SceneUpdateFragment
  ): (FrameContext, GameWithSubSystems[Model], ViewModel) => SceneUpdateFragment =
    (frameContext, model, viewModel) => viewPresent(frameContext, model.model, viewModel) |+| model.subSystemsRegister.render(frameContext)

}
