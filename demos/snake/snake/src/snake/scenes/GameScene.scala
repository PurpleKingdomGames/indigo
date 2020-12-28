package snake.scenes

import indigo._
import indigo.scenes._

import snake.model.{GameModel, SnakeGameModel, SnakeViewModel}
import snake.Score
import snake.init.{GameAssets, SnakeStartupData}

object GameScene extends Scene[SnakeStartupData, SnakeGameModel, SnakeViewModel] {
  type SceneModel     = GameModel
  type SceneViewModel = Group

  val name: SceneName =
    SceneName("game scene")

  val modelLens: Lens[SnakeGameModel, GameModel] =
    SnakeGameModel.Lenses.gameLens

  val viewModelLens: Lens[SnakeViewModel, Group] =
    Lens.readOnly(_.walls)

  val eventFilters: EventFilters =
    EventFilters.Restricted
      .withViewModelFilter(_ => None)

  val subSystems: Set[SubSystem] =
    Set(Score.automataSubSystem(GameModel.ScoreIncrement.toString(), GameAssets.fontKey))

  def updateModel(context: FrameContext[SnakeStartupData], gameModel: GameModel): GlobalEvent => Outcome[GameModel] =
    GameModel.update(context.gameTime, context.dice, gameModel, context.startUpData.viewConfig.gridSquareSize)

  def updateViewModel(
      context: FrameContext[SnakeStartupData],
      gameModel: GameModel,
      walls: Group
  ): GlobalEvent => Outcome[Group] =
    _ => Outcome(walls)

  def present(
      context: FrameContext[SnakeStartupData],
      gameModel: GameModel,
      walls: Group
  ): Outcome[SceneUpdateFragment] =
    GameView.update(context.startUpData.viewConfig, gameModel, walls, context.startUpData.staticAssets)
}
