package indigo.shared.subsystems

import indigo.shared.Outcome
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneUpdateFragment

trait SubSystem {
  type EventType
  type SubSystemModel

  def eventFilter: GlobalEvent => Option[EventType]

  def initialModel: SubSystemModel

  def update(context: SubSystemFrameContext, model: SubSystemModel): EventType => Outcome[SubSystemModel]

  def present(context: SubSystemFrameContext, model: SubSystemModel): SceneUpdateFragment
}

object SubSystem {

  def apply[Event, Model](
      _eventFilter: GlobalEvent => Option[Event],
      _initialModel: Model,
      _update: (SubSystemFrameContext, Model) => Event => Outcome[Model],
      _present: (SubSystemFrameContext, Model) => SceneUpdateFragment
  ): SubSystem =
    new SubSystem {
      type EventType      = Event
      type SubSystemModel = Model

      def eventFilter: GlobalEvent => Option[EventType] =
        _eventFilter

      def initialModel: SubSystemModel =
        _initialModel

      def update(context: SubSystemFrameContext, model: SubSystemModel): EventType => Outcome[SubSystemModel] =
        _update(context, model)

      def present(context: SubSystemFrameContext, model: SubSystemModel): SceneUpdateFragment =
        _present(context, model)
    }

}
