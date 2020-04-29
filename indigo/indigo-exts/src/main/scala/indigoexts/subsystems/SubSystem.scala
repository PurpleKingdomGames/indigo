package indigoexts.subsystems

import indigo.shared.time.GameTime
import indigo.shared.Outcome
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.dice.Dice
import indigo.shared.events.InputState

trait SubSystem {
  type EventType

  val eventFilter: GlobalEvent => Option[EventType]

  def update(gameTime: GameTime, inputState: InputState, dice: Dice): EventType => Outcome[SubSystem]

  def render(gameTime: GameTime): SceneUpdateFragment
}
