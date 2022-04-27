package indigo.shared.subsystems

import indigo.shared.Outcome
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneUpdateFragment

/** SubSystems are mini Indigo games. They contain similar functions to the full games, and allow you to
  * compartmentalise parts of your game logic. They are strictly separated from the rest of your game, and can only
  * communicate with your game via events. They hold their own state, and rely on the composable nature of
  * `SceneUpdateFragment`s to (optionally) present game elements back to the player. Sub systems can be used for all
  * sorts of things, such as rendering parallax backgrounds or encapsulating communication with a browser / network.
  */
trait SubSystem {
  type EventType
  type SubSystemModel

  def id: SubSystemId

  def eventFilter: GlobalEvent => Option[EventType]

  def initialModel: Outcome[SubSystemModel]

  def update(context: SubSystemFrameContext, model: SubSystemModel): EventType => Outcome[SubSystemModel]

  def present(context: SubSystemFrameContext, model: SubSystemModel): Outcome[SceneUpdateFragment]
}

object SubSystem {

  def apply[Event, Model](
      _id: SubSystemId,
      _eventFilter: GlobalEvent => Option[Event],
      _initialModel: Outcome[Model],
      _update: (SubSystemFrameContext, Model) => Event => Outcome[Model],
      _present: (SubSystemFrameContext, Model) => Outcome[SceneUpdateFragment]
  ): SubSystem =
    new SubSystem {
      type EventType      = Event
      type SubSystemModel = Model

      def id: SubSystemId =
        _id

      def eventFilter: GlobalEvent => Option[EventType] =
        _eventFilter

      def initialModel: Outcome[SubSystemModel] =
        _initialModel

      def update(context: SubSystemFrameContext, model: SubSystemModel): EventType => Outcome[SubSystemModel] =
        _update(context, model)

      def present(context: SubSystemFrameContext, model: SubSystemModel): Outcome[SceneUpdateFragment] =
        _present(context, model)
    }

}
