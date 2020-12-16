package indigo.scenes

import indigo.shared.events.GlobalEvent
import indigo.shared.time.Seconds

sealed trait SceneEvent extends GlobalEvent
object SceneEvent {
  case object Next                                                          extends SceneEvent
  case object Previous                                                      extends SceneEvent
  final case class JumpTo(name: SceneName)                                  extends SceneEvent
  final case class SceneChange(from: SceneName, to: SceneName, at: Seconds) extends SceneEvent
}
