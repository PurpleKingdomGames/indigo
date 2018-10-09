package indigoexts.scenemanager

import indigo.gameengine.events.ViewEvent

sealed trait SceneEvent extends ViewEvent
object SceneEvent {
  case object Next                   extends SceneEvent
  case object Previous               extends SceneEvent
  case class JumpTo(name: SceneName) extends SceneEvent
}
