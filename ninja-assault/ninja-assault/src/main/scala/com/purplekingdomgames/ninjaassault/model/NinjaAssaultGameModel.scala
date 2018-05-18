package com.purplekingdomgames.ninjaassault.model

import com.purplekingdomgames.indigo.gameengine.events.GameEvent
import com.purplekingdomgames.ninjaassault.{JumpToGame, JumpToMenu}

case class NinjaAssaultGameModel(scenes: List[Scene]) {
  val activeScene: Scene = scenes.find(_.active == true).getOrElse(Scene.Menu(true))

  def makeMenuSceneActive: NinjaAssaultGameModel =
    this.copy(
      scenes = scenes.map {
        case Scene.Menu(_) => Scene.Menu(true)
        case Scene.Logo(_) => Scene.Logo(false)
        case Scene.Game(_) => Scene.Game(false)
      }
    )

  def makeGameSceneActive: NinjaAssaultGameModel =
    this.copy(
      scenes = scenes.map {
        case Scene.Menu(_) => Scene.Menu(false)
        case Scene.Logo(_) => Scene.Logo(false)
        case Scene.Game(_) => Scene.Game(true)
      }
    )

}

object NinjaAssaultGameModel {
  val initialModel: NinjaAssaultGameModel = NinjaAssaultGameModel(
    scenes = Scene.Logo(true) :: Scene.Menu(false) :: Scene.Game(false) :: Nil
  )

  def update(state: NinjaAssaultGameModel): GameEvent => NinjaAssaultGameModel = {
    case JumpToMenu =>
      state.makeMenuSceneActive

    case JumpToGame =>
      state.makeGameSceneActive

    case _ =>
      state
  }

}
