package com.purplekingdomgames.indigoframework

import com.purplekingdomgames.indigo.gameengine._
import com.purplekingdomgames.shared.GameDefinition

object GameModelHelper {

  def initialModel(startupData: StartupData): GameModel =
    GameModel(startupData.gameDefinition)

  def updateModel(state: GameModel): GameEvent => GameModel = {
    _ =>
      state
  }

}

case class GameModel(gameDefinition: GameDefinition)

