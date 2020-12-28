package snake.model

import indigo.scenes._
import snake.gamelogic.ModelLogic
import indigoextras.geometry.BoundingBox

final case class SnakeGameModel(gridSize: BoundingBox, gameModel: GameModel) {
  def reset: SnakeGameModel =
    this.copy(gameModel = ModelLogic.initialModel(gridSize, gameModel.controlScheme))
}
object SnakeGameModel {

  def initialModel(gridSize: BoundingBox, controlScheme: ControlScheme): SnakeGameModel =
    SnakeGameModel(
      gridSize = gridSize,
      gameModel = ModelLogic.initialModel(gridSize, controlScheme)
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
