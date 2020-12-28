package snake.scenes

import indigo._
import indigo.scenes._

import snake.gamelogic.{ModelLogic, ViewLogic}
import snake.model.{GameModel, SnakeGameModel, SnakeViewModel}
import snake.gamelogic.Score
import snake.init.{GameAssets, SnakeStartupData}

object GameScene extends Scene[SnakeStartupData, SnakeGameModel, SnakeViewModel] {
  type SceneModel     = GameModel
  type SceneViewModel = SnakeViewModel

  val name: SceneName = SceneName("game scene")

  val modelLens: Lens[SnakeGameModel, GameModel] =
    SnakeGameModel.Lenses.gameLens

  val viewModelLens: Lens[SnakeViewModel, SnakeViewModel] =
    Lens.keepLatest

  val eventFilters: EventFilters =
    EventFilters.Restricted
      .withViewModelFilter(_ => None)

  val subSystems: Set[SubSystem] =
    Set(Score.automataSubSystem(ModelLogic.ScoreIncrement.toString(), GameAssets.fontKey))

  def updateModel(context: FrameContext[SnakeStartupData], gameModel: GameModel): GlobalEvent => Outcome[GameModel] =
    ModelLogic.update(context.gameTime, context.dice, gameModel, context.startUpData.viewConfig.gridSquareSize)

  def updateViewModel(
      context: FrameContext[SnakeStartupData],
      gameModel: GameModel,
      snakeViewModel: SnakeViewModel
  ): GlobalEvent => Outcome[SnakeViewModel] =
    _ => Outcome(snakeViewModel)

  def present(
      context: FrameContext[SnakeStartupData],
      gameModel: GameModel,
      snakeViewModel: SnakeViewModel
  ): Outcome[SceneUpdateFragment] =
    ViewLogic.update(context.startUpData.viewConfig, gameModel, snakeViewModel)
}
