package snake.model

import indigoexts.lenses._
import snake.gamelogic.ModelLogic
import snake.init.SnakeStartupData

case class SnakeGameModel(startupData: SnakeStartupData, gameModel: GameModel) {
  def reset: SnakeGameModel =
    this.copy(gameModel = ModelLogic.initialModel(startupData.gridSize, gameModel.controlScheme))
}
object SnakeGameModel {

  def initialModel(startupData: SnakeStartupData, controlScheme: ControlScheme): SnakeGameModel =
    SnakeGameModel(
      startupData = startupData,
      gameModel = ModelLogic.initialModel(startupData.gridSize, controlScheme)
    )

  object Lenses {
    val gameLens: Lens[SnakeGameModel, GameModel] =
      Lens(_.gameModel, (m, v) => m.copy(gameModel = v))

    val controlSchemeLens: Lens[GameModel, ControlScheme] =
      Lens(_.controlScheme, (m, v) => m.copy(controlScheme = v))

    val controlSchemeAccessors: Lens[SnakeGameModel, ControlScheme] =
      gameLens andThen controlSchemeLens
  }

}
