package indigo.gameengine

import indigo.shared.dice.Dice
import indigo.shared.Outcome
import indigo.shared.time.GameTime
import indigo.shared.events.{GlobalEvent, InputState}
import indigo.shared.scenegraph.SceneUpdateFragment

trait FrameProcessor[Model, ViewModel] {
  def run(
      model: Model,
      viewModel: ViewModel,
      gameTime: GameTime,
      globalEvents: List[GlobalEvent],
      inputState: InputState,
      dice: Dice
  ): Outcome[(Model, ViewModel, Option[SceneUpdateFragment])]

  def runSkipView(
      model: Model,
      viewModel: ViewModel,
      gameTime: GameTime,
      globalEvents: List[GlobalEvent],
      inputState: InputState,
      dice: Dice
  ): Outcome[(Model, ViewModel, Option[SceneUpdateFragment])]
}
