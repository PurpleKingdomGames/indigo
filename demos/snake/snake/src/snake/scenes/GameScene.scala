package snake.scenes

import indigo._
import indigoexts.lenses._
import indigoexts.scenemanager._
import indigo.AsString._

import indigoexts.subsystems.SubSystem

import snake.gamelogic.{ModelLogic, ViewLogic}
import snake.model.{GameModel, SnakeGameModel, SnakeViewModel}
import snake.gamelogic.Score
import snake.init.GameAssets

object GameScene extends Scene[SnakeGameModel, SnakeViewModel] {
  type SceneModel     = GameModel
  type SceneViewModel = SnakeViewModel

  val name: SceneName = SceneName("game scene")

  val sceneModelLens: Lens[SnakeGameModel, GameModel] =
    SnakeGameModel.Lenses.gameLens

  val sceneViewModelLens: Lens[SnakeViewModel, SnakeViewModel] =
    Lens.keepLatest

  val sceneSubSystems: Set[SubSystem] =
    Set(Score.automataSubSystem(ModelLogic.ScoreIncrement.show, GameAssets.fontKey))

  def updateSceneModel(gameTime: GameTime, gameModel: GameModel, inputState: InputState, dice: Dice): GlobalEvent => Outcome[GameModel] =
    ModelLogic.update(gameTime, gameModel)

  def updateSceneViewModel(
      gameTime: GameTime,
      gameModel: GameModel,
      snakeViewModel: SnakeViewModel,
      inputState: InputState,
      dice: Dice
  ): Outcome[SnakeViewModel] =
    Outcome(snakeViewModel)

  def updateSceneView(
      gameTime: GameTime,
      gameModel: GameModel,
      snakeViewModel: SnakeViewModel,
      inputState: InputState,
      boundaryLocator: BoundaryLocator
  ): SceneUpdateFragment =
    ViewLogic.update(gameModel, snakeViewModel)
}
