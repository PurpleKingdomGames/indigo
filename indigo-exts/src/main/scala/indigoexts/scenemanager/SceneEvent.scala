package indigoexts.scenemanager

import indigo.gameengine.events.FrameEvent

sealed trait SceneEvent extends FrameEvent
object SceneEvent {
  case object Next                   extends SceneEvent
  case object Previous               extends SceneEvent
  case class JumpTo(name: SceneName) extends SceneEvent
}
