package indigoframework

import indigo.Outcome
import indigo.gameengine.events.GlobalEvent
import indigo.shared.GameDefinition

object GameModelHelper {

  def initialModel(startupData: StartupData): GameModel =
    GameModel(startupData.gameDefinition)

  def updateModel(state: GameModel): GlobalEvent => Outcome[GameModel] = { _ =>
    Outcome(state)
  }

}

final case class GameModel(gameDefinition: GameDefinition)
