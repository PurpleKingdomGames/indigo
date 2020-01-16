package indigoexts.entry

import indigo.GameTime
import indigo.Dice
import indigo.GlobalEvent
import indigo.Outcome
import indigo.InputState
import indigo.SceneUpdateFragment
import indigoexts.subsystems.SubSystemsRegister
import indigo.shared.abstractions.syntax._

final class GameWithSubSystems[Model](val model: Model, val subSystemsRegister: SubSystemsRegister)
object GameWithSubSystems {

  def apply[Model](model: Model, subSystemsRegister: SubSystemsRegister): GameWithSubSystems[Model] =
    new GameWithSubSystems[Model](model, subSystemsRegister)

  def update[Model](
      modelUpdate: (GameTime, Model, Dice) => GlobalEvent => Outcome[Model]
  )(gameTime: GameTime, model: GameWithSubSystems[Model], dice: Dice): GlobalEvent => Outcome[GameWithSubSystems[Model]] =
    e =>
      (modelUpdate(gameTime, model.model, dice)(e), model.subSystemsRegister.update(gameTime, dice)(e))
        .map2((m, s) => GameWithSubSystems(m, s))

  def updateViewModel[Model, ViewModel](
      viewModelUpdate: (GameTime, Model, ViewModel, InputState, Dice) => Outcome[ViewModel]
  )(gameTime: GameTime, model: GameWithSubSystems[Model], viewModel: ViewModel, inputState: InputState, dice: Dice): Outcome[ViewModel] =
    viewModelUpdate(gameTime, model.model, viewModel, inputState, dice)

  def present[Model, ViewModel](
      viewPresent: (GameTime, Model, ViewModel, InputState) => SceneUpdateFragment
  )(gameTime: GameTime, model: GameWithSubSystems[Model], viewModel: ViewModel, inputState: InputState): SceneUpdateFragment =
    viewPresent(gameTime, model.model, viewModel, inputState) |+| model.subSystemsRegister.render(gameTime)
}
