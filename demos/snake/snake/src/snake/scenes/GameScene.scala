package snake.scenes

import indigo._
import indigo.scenes._
import indigo.AsString._

import snake.gamelogic.{ModelLogic, ViewLogic}
import snake.model.{GameModel, SnakeGameModel, SnakeViewModel}
import snake.gamelogic.Score
import snake.init.{GameAssets, SnakeStartupData}

object GameScene extends Scene[SnakeStartupData, SnakeGameModel, SnakeViewModel] {
  type SceneModel     = GameModel
  type SceneViewModel = SnakeViewModel

  val name: SceneName = SceneName("game scene")

  val sceneModelLens: Lens[SnakeGameModel, GameModel] =
    SnakeGameModel.Lenses.gameLens

  val sceneViewModelLens: Lens[SnakeViewModel, SnakeViewModel] =
    Lens.keepLatest

  val sceneSubSystems: Set[SubSystem] =
    Set(Score.automataSubSystem(ModelLogic.ScoreIncrement.show, GameAssets.fontKey))

  def updateSceneModel(context: FrameContext[SnakeStartupData], gameModel: GameModel): GlobalEvent => Outcome[GameModel] =
    ModelLogic.update(context.gameTime, context.dice, gameModel)

  def updateSceneViewModel(
      context: FrameContext[SnakeStartupData],
      gameModel: GameModel,
      snakeViewModel: SnakeViewModel
  ): Outcome[SnakeViewModel] =
    Outcome(snakeViewModel)

  def updateSceneView(
      context: FrameContext[SnakeStartupData],
      gameModel: GameModel,
      snakeViewModel: SnakeViewModel
  ): SceneUpdateFragment =
    ViewLogic.update(gameModel, snakeViewModel)
}
