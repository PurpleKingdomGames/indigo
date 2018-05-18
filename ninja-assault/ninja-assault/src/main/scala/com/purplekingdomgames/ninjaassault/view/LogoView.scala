package com.purplekingdomgames.ninjaassault.view

import com.purplekingdomgames.indigo.gameengine.events.{FrameInputEvents, ViewEvent}
import com.purplekingdomgames.indigo.gameengine.scenegraph.{Graphic, SceneGraphNode, SceneUpdateFragment}
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.Point
import com.purplekingdomgames.ninjaassault.JumpToMenu
import com.purplekingdomgames.ninjaassault.settings.{Assets, Settings}

object LogoView {

  def draw(frameInputEvents: FrameInputEvents): SceneUpdateFragment = {
    val events: List[ViewEvent] =
      if (frameInputEvents.wasMouseClickedWithin(Settings.screenArea)) List(JumpToMenu)
      else Nil

    SceneUpdateFragment.empty
      .addGameLayerNodes(gameLayer)
      .addViewEvents(events)
  }

  private def gameLayer: List[SceneGraphNode] =
    List(
      Graphic(0, 0, 256, 256, 1, Assets.logoRef).moveTo(Settings.activeScreenCenter - Point(128, 128))
    )

}
