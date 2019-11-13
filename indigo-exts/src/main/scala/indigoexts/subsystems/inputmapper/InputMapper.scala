package indigoexts.subsystems.inputmapper

import indigoexts.subsystems.SubSystem
import indigo.shared.events.GlobalEvent
import indigo.shared.time.GameTime
import indigo.shared.dice.Dice
import indigo.shared.Outcome
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.events.InputEvent

final class InputMapper(mappings: Map[InputEvent, InputAction]) extends SubSystem {

  type EventType = InputMapperEvent

  val eventFilter: GlobalEvent => Option[InputMapperEvent] = {
    case e: InputEvent =>
      Some(InputMapperEvent.Input(e))

    case e: InputMapperEvent.Input =>
      Some(e)

    case e: InputMapperEvent.AddMappings =>
      Some(e)

    case e: InputMapperEvent.RemoveMappings =>
      Some(e)

    case _: InputMapperEvent.Action =>
      None

    case _ =>
      None
  }

  def update(gameTime: GameTime, dice: Dice): InputMapperEvent => Outcome[InputMapper] = {
    case InputMapperEvent.Input(e) =>
      Outcome(this).addGlobalEvents(mappings.get(e).map(InputMapperEvent.Action.apply).toList)

    case InputMapperEvent.AddMappings(inputMappings) =>
      Outcome(new InputMapper(mappings ++ inputMappings.toMap))

    case InputMapperEvent.RemoveMappings(inputEvents) =>
      Outcome(new InputMapper(mappings -- inputEvents))

    case InputMapperEvent.Action(_) =>
      Outcome(this)
  }

  def render(gameTime: GameTime): SceneUpdateFragment =
    SceneUpdateFragment.empty

  def report: String =
    "Input Mapper"

  def toMappingsList: List[InputMapper.Mapping] =
    mappings.toList

}

object InputMapper {

  type Mapping = (InputEvent, InputAction)

  def subsystem[Event <: InputEvent, Action <: InputAction](inputMappings: (Event, Action)*): InputMapper =
    new InputMapper(inputMappings.toMap)

}

trait InputAction

sealed trait InputMapperEvent extends GlobalEvent
object InputMapperEvent {
  final case class Input(event: InputEvent)                               extends InputMapperEvent
  final case class Action(event: InputAction)                             extends InputMapperEvent
  final case class RemoveMappings(inputEvent: List[InputEvent])           extends InputMapperEvent
  final case class AddMappings(mappings: List[(InputEvent, InputAction)]) extends InputMapperEvent
  object AddMappings {
    def apply(inputMappings: (InputEvent, InputAction)*): AddMappings =
      AddMappings(inputMappings.toList)
  }
}
