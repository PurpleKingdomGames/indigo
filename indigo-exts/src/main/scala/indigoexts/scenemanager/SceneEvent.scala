package indigoexts.scenemanager

import indigo.gameengine.events.ViewEvent

sealed trait SceneEvent                 extends ViewEvent
case object NextScene                   extends SceneEvent
case object PreviousScene               extends SceneEvent
case class JumpToScene(name: SceneName) extends SceneEvent
