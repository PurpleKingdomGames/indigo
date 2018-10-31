package indigoexts.scenemanager

import indigo.gameengine.events.GlobalEvent

sealed trait SceneEvent extends GlobalEvent
object SceneEvent {
  case object Next                   extends SceneEvent
  case object Previous               extends SceneEvent
  case class JumpTo(name: SceneName) extends SceneEvent
}
