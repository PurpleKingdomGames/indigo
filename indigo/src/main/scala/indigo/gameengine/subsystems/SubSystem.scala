package indigo.gameengine.subsystems

import indigo.time.GameTime
import indigo.gameengine.Outcome
import indigo.gameengine.events.GlobalEvent
import indigo.gameengine.scenegraph.SceneUpdateFragment
import indigo.dice.Dice

trait SubSystem {
  type EventType

  val eventFilter: GlobalEvent => Option[EventType]

  def update(gameTime: GameTime, dice: Dice): EventType => Outcome[SubSystem]

  def render(gameTime: GameTime): SceneUpdateFragment

  def report: String
}
