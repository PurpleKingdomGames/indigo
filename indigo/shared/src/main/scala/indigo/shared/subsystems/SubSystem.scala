package indigo.shared.subsystems

import indigo.shared.Outcome
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.FrameContext

trait SubSystem {
  type EventType

  val eventFilter: GlobalEvent => Option[EventType]

  def update(context: FrameContext): EventType => Outcome[SubSystem]

  def render(context: FrameContext): SceneUpdateFragment
}
