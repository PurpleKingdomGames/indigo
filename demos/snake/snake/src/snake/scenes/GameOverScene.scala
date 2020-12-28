package snake.scenes

import indigo._
import indigo.scenes._
import snake.model.{SnakeGameModel, SnakeViewModel}
import snake.init.{GameAssets, SnakeStartupData}

object GameOverScene extends Scene[SnakeStartupData, SnakeGameModel, SnakeViewModel] {
  type SceneModel     = Int
  type SceneViewModel = Unit

  val name: SceneName =
    SceneName("game over")

  val modelLens: Lens[SnakeGameModel, Int] =
    Lens.readOnly(_.gameModel.score)

  val viewModelLens: Lens[SnakeViewModel, Unit] =
    Lens.unit

  val eventFilters: EventFilters =
    EventFilters.Restricted
      .withViewModelFilter(_ => None)

  val subSystems: Set[SubSystem] =
    Set()

  def updateModel(context: FrameContext[SnakeStartupData], pointsScored: Int): GlobalEvent => Outcome[Int] = {
    case KeyboardEvent.KeyUp(Key.SPACE) =>
      Outcome(pointsScored)
        .addGlobalEvents(SceneEvent.JumpTo(StartScene.name))

    case _ =>
      Outcome(pointsScored)
  }

  def updateViewModel(
      context: FrameContext[SnakeStartupData],
      pointsScored: Int,
      sceneViewModel: Unit
  ): GlobalEvent => Outcome[Unit] =
    _ => Outcome(sceneViewModel)

  def present(
      context: FrameContext[SnakeStartupData],
      pointsScored: Int,
      sceneViewModel: Unit
  ): Outcome[SceneUpdateFragment] =
    Outcome {
      val horizontalCenter: Int = context.startUpData.viewConfig.horizontalCenter
      val verticalMiddle: Int   = context.startUpData.viewConfig.verticalMiddle

      SceneUpdateFragment.empty
        .addUiLayerNodes(
          Text("Game Over!", horizontalCenter, verticalMiddle - 20, 1, GameAssets.fontKey).alignCenter,
          Text(s"You scored: ${pointsScored.toString()} pts!", horizontalCenter, verticalMiddle - 5, 1, GameAssets.fontKey).alignCenter,
          Text("(hit space to restart)", horizontalCenter, 220, 1, GameAssets.fontKey).alignCenter
        )
    }
}
