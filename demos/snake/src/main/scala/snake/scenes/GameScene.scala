package snake.scenes

import indigo._
import indigoexts.lenses._
import indigoexts.scenemanager._
import snake.gamelogic.{ModelLogic, ViewLogic}
import snake.model.{GameModel, SnakeGameModel, SnakeViewModel}

object GameScene extends Scene[SnakeGameModel, SnakeViewModel] {
  type SceneModel     = GameModel
  type SceneViewModel = SnakeViewModel

  val name: SceneName = SceneName("game scene")

  val sceneModelLens: Lens[SnakeGameModel, GameModel] =
    SnakeGameModel.Lenses.gameLens

  val sceneViewModelLens: Lens[SnakeViewModel, SnakeViewModel] =
    Lens.keepLatest

  def updateSceneModel(gameTime: GameTime, gameModel: GameModel, dice: Dice): GlobalEvent => Outcome[GameModel] =
    ModelLogic.update(gameTime, gameModel)

  def updateSceneViewModel(
      gameTime: GameTime,
      gameModel: GameModel,
      snakeViewModel: SnakeViewModel,
      frameInputEvents: FrameInputEvents,
      dice: Dice
  ): Outcome[SnakeViewModel] =
    Outcome(snakeViewModel)

  def updateSceneView(
      gameTime: GameTime,
      gameModel: GameModel,
      snakeViewModel: SnakeViewModel,
      frameInputEvents: FrameInputEvents
  ): SceneUpdateFragment =
    ViewLogic.update(gameModel, snakeViewModel)
}
