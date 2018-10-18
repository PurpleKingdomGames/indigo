package indigoframework

import indigo.UpdatedModel
import indigo.gameengine.events.GameEvent
import indigo.shared.GameDefinition

object GameModelHelper {

  def initialModel(startupData: StartupData): GameModel =
    GameModel(startupData.gameDefinition)

  def updateModel(state: GameModel): GameEvent => UpdatedModel[GameModel] = { _ =>
    state
  }

}

case class GameModel(gameDefinition: GameDefinition)
