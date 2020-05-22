package indigogame.scenemanager

import indigo.shared.events.GlobalEvent

sealed trait SceneEvent extends GlobalEvent
object SceneEvent {
  case object Next                         extends SceneEvent
  case object Previous                     extends SceneEvent
  final case class JumpTo(name: SceneName) extends SceneEvent
}
