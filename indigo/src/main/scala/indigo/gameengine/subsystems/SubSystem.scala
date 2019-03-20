package indigo.gameengine.subsystems

import indigo.gameengine.GameTime
import indigo.gameengine.Outcome
import indigo.gameengine.events.GlobalEvent
import indigo.gameengine.scenegraph.SceneUpdateFragment

trait SubSystem {
  type EventType

  val eventFilter: GlobalEvent => Option[EventType]

  def update(gameTime: GameTime): EventType => Outcome[SubSystem]

  def render(gameTime: GameTime): SceneUpdateFragment

  def report: String
}
