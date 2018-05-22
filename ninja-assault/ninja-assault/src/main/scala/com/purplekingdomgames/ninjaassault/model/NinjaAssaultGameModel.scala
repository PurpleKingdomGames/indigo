package com.purplekingdomgames.ninjaassault.model

import com.purplekingdomgames.indigo.gameengine.events.GameEvent
import com.purplekingdomgames.ninjaassault.{JumpToGame, JumpToMenu}

case class NinjaAssaultGameModel(scenes: List[Scene]) {
  val activeScene: Scene = scenes.find(_.active == true).getOrElse(Scene.Menu(true))

  def makeMenuSceneActive: NinjaAssaultGameModel =
    this.copy(
      scenes = scenes.map {
        case Scene.Menu(_)    => Scene.Menu(true)
        case Scene.Logo(_)    => Scene.Logo(false)
        case Scene.Game(_, l) => Scene.Game(false, l)
      }
    )

  def makeGameSceneActive: NinjaAssaultGameModel =
    this.copy(
      scenes = scenes.map {
        case Scene.Menu(_)    => Scene.Menu(false)
        case Scene.Logo(_)    => Scene.Logo(false)
        case Scene.Game(_, l) => Scene.Game(true, l)
      }
    )

}

object NinjaAssaultGameModel {
  val initialModel: NinjaAssaultGameModel = NinjaAssaultGameModel(
    scenes = Scene.Logo(true) :: Scene.Menu(false) :: Scene.Game(false, Level.testLevel) :: Nil
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
