package com.purplekingdomgames.ninjaassault.view

import com.purplekingdomgames.indigo.gameengine.events.{FrameInputEvents, ViewEvent}
import com.purplekingdomgames.indigo.gameengine.scenegraph.{SceneGraphNode, SceneUpdateFragment, Text}
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.AlignCenter
import com.purplekingdomgames.ninjaassault.JumpToGame
import com.purplekingdomgames.ninjaassault.settings.{Assets, Settings}

object MenuView {

  def draw(frameInputEvents: FrameInputEvents): SceneUpdateFragment = {
    val events: List[ViewEvent] =
      if (frameInputEvents.wasMouseClickedWithin(Settings.screenArea)) List(JumpToGame)
      else Nil

    SceneUpdateFragment.empty
      .addGameLayerNodes(gameLayer)
      .addViewEvents(events)
  }

  private def gameLayer: List[SceneGraphNode] =
    List(
      Text("CLICK TO START",
           Settings.activeHorizontalCenter,
           Settings.activeScreenHeight - 10 - Assets.fontInfo.unknownChar.bounds.height,
           1,
           Assets.fontKey).withAlignment(AlignCenter)
    )
}
