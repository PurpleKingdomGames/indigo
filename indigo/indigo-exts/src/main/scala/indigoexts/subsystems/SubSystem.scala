package indigoexts.subsystems

import indigo.shared.time.GameTime
import indigo.shared.Outcome
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.dice.Dice

trait SubSystem {
  type EventType

  val eventFilter: GlobalEvent => Option[EventType]

  def update(gameTime: GameTime, dice: Dice): EventType => Outcome[SubSystem]

  def render(gameTime: GameTime): SceneUpdateFragment
}
