package indigoexts.subsystems.inputmapper

import indigoexts.subsystems.SubSystem
import indigo.shared.events.GlobalEvent
import indigo.shared.time.GameTime
import indigo.shared.dice.Dice
import indigo.shared.Outcome
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.events.InputEvent

final class InputMapper extends SubSystem {

  type EventType = InputMapperEvent

  val eventFilter: GlobalEvent => Option[InputMapperEvent] = {
    case e: InputEvent =>
      Some(InputMapperEvent.Input(e))

    // case InputMapperEvent.AddMappings    => ???
    // case InputMapperEvent.RemoveMappings => ???

    case _ =>
      None
  }

  def update(gameTime: GameTime, dice: Dice): InputMapperEvent => Outcome[InputMapper] = {
    case InputMapperEvent.Input(e) =>
      ???
  }

  def render(gameTime: GameTime): SceneUpdateFragment =
    SceneUpdateFragment.empty

  def report: String =
    "Input Mapper"

}

object InputMapper {
  //
}

sealed trait InputMapperEvent extends GlobalEvent
object InputMapperEvent {
  final case class Input(e: InputEvent) extends InputMapperEvent
  // final case class RemoveMappings(???) extends InputMapperEvent
  // final case class AddMappings(???) extends InputMapperEvent
}
