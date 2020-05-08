package snake.scenes

import indigo._
import indigoexts.lenses.Lens
import indigoexts.scenes._
import snake.model.{SnakeGameModel, SnakeViewModel}
import snake.init.{GameAssets, Settings}
import indigoexts.subsystems.SubSystem

object GameOverScene extends Scene[SnakeGameModel, SnakeViewModel] {
  type SceneModel     = Int
  type SceneViewModel = Unit

  val name: SceneName = SceneName("game over")

  val sceneModelLens: Lens[SnakeGameModel, Int] =
    Lens(_.gameModel.score, (m, _) => m)

  val sceneViewModelLens: Lens[SnakeViewModel, Unit] =
    Lens.fixed(())

  val sceneSubSystems: Set[SubSystem] =
    Set()

  def updateSceneModel(gameTime: GameTime, pointsScored: Int, inputState: InputState, dice: Dice): GlobalEvent => Outcome[Int] = {
    case KeyboardEvent.KeyUp(Keys.SPACE) =>
      Outcome(pointsScored)
        .addGlobalEvents(SceneEvent.JumpTo(StartScene.name))

    case _ =>
      Outcome(pointsScored)
  }

  def updateSceneViewModel(
      gameTime: GameTime,
      pointsScored: Int,
      sceneViewModel: Unit,
      inputState: InputState,
      dice: Dice
  ): Outcome[Unit] =
    Outcome(())

  def updateSceneView(
      gameTime: GameTime,
      pointsScored: Int,
      sceneViewModel: Unit,
      inputState: InputState
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
