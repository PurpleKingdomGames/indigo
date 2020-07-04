package snake.scenes

import indigo._
import indigo.scenes._
import snake.model.{SnakeGameModel, SnakeViewModel}
import snake.init.{GameAssets, Settings, SnakeStartupData}

object GameOverScene extends Scene[SnakeStartupData, SnakeGameModel, SnakeViewModel] {
  type SceneModel     = Int
  type SceneViewModel = Unit

  val name: SceneName = SceneName("game over")

  val sceneModelLens: Lens[SnakeGameModel, Int] =
    Lens(_.gameModel.score, (m, _) => m)

  val sceneViewModelLens: Lens[SnakeViewModel, Unit] =
    Lens.fixed(())

  val sceneSubSystems: Set[SubSystem] =
    Set()

  def updateSceneModel(context: FrameContext[SnakeStartupData], pointsScored: Int): GlobalEvent => Outcome[Int] = {
    case KeyboardEvent.KeyUp(Keys.SPACE) =>
      Outcome(pointsScored)
        .addGlobalEvents(SceneEvent.JumpTo(StartScene.name))

    case _ =>
      Outcome(pointsScored)
  }

  def updateSceneViewModel(
      context: FrameContext[SnakeStartupData],
      pointsScored: Int,
      sceneViewModel: Unit
  ): Outcome[Unit] =
    Outcome(())

  def updateSceneView(
      context: FrameContext[SnakeStartupData],
      pointsScored: Int,
      sceneViewModel: Unit
  ): SceneUpdateFragment = {
    val horizontalCenter: Int = (Settings.viewportWidth / Settings.magnificationLevel) / 2
    val verticalMiddle: Int   = (Settings.viewportHeight / Settings.magnificationLevel) / 2

    SceneUpdateFragment.empty
      .addUiLayerNodes(
        Text("Game Over!", horizontalCenter, verticalMiddle - 20, 1, GameAssets.fontKey).alignCenter,
        Text(s"You scored: ${pointsScored.toString()} pts!", horizontalCenter, verticalMiddle - 5, 1, GameAssets.fontKey).alignCenter,
        Text("(hit space to restart)", horizontalCenter, 220, 1, GameAssets.fontKey).alignCenter
      )
  }
}
