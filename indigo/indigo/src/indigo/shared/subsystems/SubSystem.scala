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
trait SubSystem[Model]:
  type EventType
  type SubSystemModel
  type ReferenceData

  def id: SubSystemId

  def eventFilter: GlobalEvent => Option[EventType]

  def reference(model: Model): ReferenceData

  def initialModel: Outcome[SubSystemModel]

  def update(
      context: SubSystemContext[ReferenceData],
      model: SubSystemModel
  ): EventType => Outcome[SubSystemModel]

  def present(
      context: SubSystemContext[ReferenceData],
      model: SubSystemModel
  ): Outcome[SceneUpdateFragment]
