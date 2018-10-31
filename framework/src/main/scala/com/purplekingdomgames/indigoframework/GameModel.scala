package indigoframework

import indigo.UpdatedModel
import indigo.gameengine.events.GlobalEvent
import indigo.shared.GameDefinition

object GameModelHelper {

  def initialModel(startupData: StartupData): GameModel =
    GameModel(startupData.gameDefinition)

  def updateModel(state: GameModel): GlobalEvent => UpdatedModel[GameModel] = { _ =>
    state
  }

}

case class GameModel(gameDefinition: GameDefinition)
