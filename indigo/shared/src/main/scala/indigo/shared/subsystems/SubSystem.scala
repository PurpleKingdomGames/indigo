package indigo.shared.subsystems

import indigo.shared.Outcome
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.FrameContext

trait SubSystem {
  type EventType
  type SubSystemModel

  def eventFilter: GlobalEvent => Option[EventType]

  def initialModel: SubSystemModel

  def update(context: FrameContext, model: SubSystemModel): EventType => Outcome[SubSystemModel]

  def render(context: FrameContext, model: SubSystemModel): SceneUpdateFragment
}

object SubSystem {

  def apply[Event, Model](
      _eventFilter: GlobalEvent => Option[Event],
      _initialModel: Model,
      _update: (FrameContext, Model) => Event => Outcome[Model],
      _render: (FrameContext, Model) => SceneUpdateFragment
  ): SubSystem =
    new SubSystem {
      type EventType      = Event
      type SubSystemModel = Model

      def eventFilter: GlobalEvent => Option[EventType] =
        _eventFilter

      def initialModel: SubSystemModel =
        _initialModel

      def update(context: FrameContext, model: SubSystemModel): EventType => Outcome[SubSystemModel] =
        _update(context, model)

      def render(context: FrameContext, model: SubSystemModel): SceneUpdateFragment =
        _render(context, model)
    }

}
