package com.purplekingdomgames.indigoexts.scenemanager

import com.purplekingdomgames.indigo.gameengine.events.ViewEvent

sealed trait SceneEvent                 extends ViewEvent
case object NextScene                   extends SceneEvent
case object PreviousScene               extends SceneEvent
case class JumpToScene(name: SceneName) extends SceneEvent
