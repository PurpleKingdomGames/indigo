package indigoexamples

import indigo._
import indigoexts.subsystems.SubSystem

final case class HelloSubSystem(message: String, fontKey: FontKey) extends SubSystem {
  type EventType = GlobalEvent

  val eventFilter: GlobalEvent => Option[EventType] = _ => None

  def update(gameTime: GameTime, dice: Dice): EventType => Outcome[SubSystem] = _ => Outcome(this)

  def render(gameTime: GameTime): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addUiLayerNodes(Text(message, 20, 50, 1, fontKey))
}
