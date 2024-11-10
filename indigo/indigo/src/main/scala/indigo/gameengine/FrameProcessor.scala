package indigo.gameengine

import indigo.platform.renderer.Renderer
import indigo.shared.BoundaryLocator
import indigo.shared.Context
import indigo.shared.Outcome
import indigo.shared.collections.Batch
import indigo.shared.dice.Dice
import indigo.shared.events.GlobalEvent
import indigo.shared.events.InputState
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.time.GameTime

trait FrameProcessor[StartUpData, Model, ViewModel]:
  def run(
      model: => Model,
      viewModel: => ViewModel,
      globalEvents: Batch[GlobalEvent],
      context: => Context[StartUpData]
  ): Outcome[(Model, ViewModel, SceneUpdateFragment)]
