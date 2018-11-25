package indigo.gameengine.subsystems

import indigo.gameengine.GameTime
import indigo.gameengine.events.GlobalEvent
import indigo.gameengine.scenegraph.SceneUpdateFragment

trait SubSystem[Model, EventType] {

  val eventFilter: GlobalEvent => Option[EventType]

  def update(gameTime: GameTime): EventType => UpdatedSubSystem

  def render(gameTime: GameTime): SceneUpdateFragment

  case class UpdatedSubSystem(model: Model, events: List[GlobalEvent]) {
    def addGlobalEvents(globalEvents: GlobalEvent*): UpdatedSubSystem =
      this.copy(events = events ++ globalEvents.toList)
  }
  object UpdatedSubSystem {
    implicit def modelToUpdatedSubSystem(model: Model): UpdatedSubSystem =
      apply(model)

    def apply(model: Model): UpdatedSubSystem =
      UpdatedSubSystem(model, Nil)
  }

}
