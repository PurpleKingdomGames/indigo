package com.purplekingdomgames.ninjaassault.view

import com.purplekingdomgames.indigo.gameengine.events.{FrameInputEvents, GlobalSignals}
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.ninjaassault.model._
import com.purplekingdomgames.ninjaassault.settings.Assets

object View {

  def draw(gameModel: NinjaAssaultGameModel, frameInputEvents: FrameInputEvents): SceneUpdateFragment =
    gameModel.activeScene match {
      case Scene.Logo(_) => LogoView.draw(frameInputEvents) |+| SceneUpdateFragment.empty.addUiLayerNodes(cursor)
      case Scene.Menu(_) => MenuView.draw(frameInputEvents) |+| SceneUpdateFragment.empty.addUiLayerNodes(cursor)
      case Scene.Game(_) => GameView.draw() |+| SceneUpdateFragment.empty.addUiLayerNodes(cursor)
    }

  def cursor: Graphic =
    Graphic(
      GlobalSignals.MousePosition.x,
      GlobalSignals.MousePosition.y,
      32,
      32,
      1000,
      Assets.cursorRef
    )

}
