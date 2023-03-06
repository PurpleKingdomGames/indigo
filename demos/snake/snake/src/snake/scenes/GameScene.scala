package snake.scenes

import indigo.*
import indigo.scenes.*

import snake.model.{GameModel, ViewModel}
import snake.Score
import snake.init.{GameAssets, StartupData}

object GameScene extends Scene[StartupData, GameModel, ViewModel]:
  type SceneModel     = GameModel
  type SceneViewModel = Group

  val name: SceneName =
    SceneName("game scene")

  val modelLens: Lens[GameModel, GameModel] =
    Lens.keepLatest

  val viewModelLens: Lens[ViewModel, Group] =
    Lens.readOnly(_.walls)

  val eventFilters: EventFilters =
    EventFilters.Restricted
      .withViewModelFilter(_ => None)

  val subSystems: Set[SubSystem] =
    Set(Score.automataSubSystem(GameModel.ScoreIncrement.toString(), GameAssets.fontKey))

  def updateModel(context: SceneContext[StartupData], gameModel: GameModel): GlobalEvent => Outcome[GameModel] =
    gameModel.update(context.gameTime, context.dice, context.startUpData.viewConfig.gridSquareSize)

  def updateViewModel(
      context: SceneContext[StartupData],
      gameModel: GameModel,
      walls: Group
  ): GlobalEvent => Outcome[Group] =
    _ => Outcome(walls)

  def present(
      context: SceneContext[StartupData],
      gameModel: GameModel,
      walls: Group
  ): Outcome[SceneUpdateFragment] =
    GameView.update(context.startUpData.viewConfig, gameModel, walls, context.startUpData.staticAssets)
