package indigoexts.subsystems.inputmapper

import indigoexts.subsystems.SubSystem
import indigo.shared.events.GlobalEvent
import indigo.shared.time.GameTime
import indigo.shared.dice.Dice
import indigo.shared.Outcome
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.events.InputEvent

final class InputMapper(mappings: Map[InputEvent, List[GlobalEvent]]) extends SubSystem {

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

    case _ =>
      None
  }

  def update(gameTime: GameTime, dice: Dice): InputMapperEvent => Outcome[InputMapper] = {
    case InputMapperEvent.Input(e) =>
      Outcome(this).addGlobalEvents(mappings.get(e).toList.flatten)

    case InputMapperEvent.AddMappings(inputMappings) =>
      Outcome(new InputMapper(mappings ++ inputMappings))

    case InputMapperEvent.RemoveMappings(inputEvents) =>
      Outcome(new InputMapper(mappings -- inputEvents))
  }

  def render(gameTime: GameTime): SceneUpdateFragment =
    SceneUpdateFragment.empty

  def toMappingsList: List[(InputEvent, List[GlobalEvent])] =
    mappings.toList

}

object InputMapper {

  def subsystem(inputMappings: (InputEvent, List[GlobalEvent])*): InputMapper =
    new InputMapper(inputMappings.toMap)

}

sealed trait InputMapperEvent extends GlobalEvent
object InputMapperEvent {
  final case class Input(event: InputEvent)                      extends InputMapperEvent
  final case class RemoveMappings(inputEvents: List[InputEvent]) extends InputMapperEvent
  object RemoveMappings {
    def apply(inputMappings: InputEvent*): RemoveMappings =
      RemoveMappings(inputMappings.toList)
  }
  final case class AddMappings(mappings: List[(InputEvent, List[GlobalEvent])]) extends InputMapperEvent
  object AddMappings {
    def apply(inputMappings: (InputEvent, List[GlobalEvent])*): AddMappings =
      AddMappings(inputMappings.toList)
  }
}
