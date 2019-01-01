package indigo.gameengine.subsystems

import indigo.gameengine.GameTime
import indigo.gameengine.events.GlobalEvent
import indigo.gameengine.scenegraph.SceneUpdateFragment

trait SubSystem {
  type EventType

  val eventFilter: GlobalEvent => Option[EventType]

  def update(gameTime: GameTime): EventType => UpdatedSubSystem

  def render(gameTime: GameTime): SceneUpdateFragment

  def report: String
}

final case class UpdatedSubSystem(subSystem: SubSystem, events: List[GlobalEvent]) {
  def addGlobalEvents(globalEvents: GlobalEvent*): UpdatedSubSystem =
    this.copy(events = events ++ globalEvents.toList)
}
object UpdatedSubSystem {
  def apply(subSystem: SubSystem): UpdatedSubSystem =
    UpdatedSubSystem(subSystem, Nil)
}
