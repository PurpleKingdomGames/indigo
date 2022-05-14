package indigo.gameengine

import indigo.shared.BoundaryLocator
import indigo.shared.Outcome
import indigo.shared.dice.Dice
import indigo.shared.events.GlobalEvent
import indigo.shared.events.InputState
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.time.GameTime

trait FrameProcessor[StartUpData, Model, ViewModel]:
  def run(
      startUpData: => StartUpData,
      model: => Model,
      viewModel: => ViewModel,
      gameTime: GameTime,
      globalEvents: List[GlobalEvent],
      inputState: InputState,
      dice: Dice,
      boundaryLocator: BoundaryLocator
  ): Outcome[(Model, ViewModel, SceneUpdateFragment)]
