package indigoexts.subsystems.inputmapper

import utest._
import indigo.shared.events.KeyboardEvent
import indigo.shared.constants.Key
import indigo.shared.constants.Keys
import indigo.shared.events.InputEvent
import indigo.shared.time.GameTime
import indigo.shared.dice.Dice
import indigo.shared.events.GlobalEvent
import indigo.shared.events.InputState

object InputMapperTests extends TestSuite {

  import indigoexts.subsystems.FakeFrameContext._

  case object Jump extends GlobalEvent
  case object Duck extends GlobalEvent

  val tests: Tests =
    Tests {

      "An input mapper can be initialised" - {

        val mapper =
          InputMapper.subsystem(
            KeyboardEvent.KeyUp(Keys.UP_ARROW)  -> List(Jump),
            KeyboardEvent.KeyUp(Keys.KEY_W)     -> List(Jump),
            KeyboardEvent.KeyDown(Keys.DOWN_ARROW) -> List(Duck)
          )

        val actual: List[(InputEvent, List[GlobalEvent])] =
          mapper.toMappingsList

        val expected =
          List(
            KeyboardEvent.KeyUp(Keys.UP_ARROW)  -> List(Jump),
            KeyboardEvent.KeyDown(Keys.DOWN_ARROW) -> List(Duck),
            KeyboardEvent.KeyUp(Keys.KEY_W)     -> List(Jump)
          )

        mappingsEqual(actual, expected) ==> true

      }

      "Mappings can be added" - {

        val mapper =
          InputMapper.subsystem(
            KeyboardEvent.KeyUp(Keys.UP_ARROW) -> List(Jump),
            KeyboardEvent.KeyUp(Keys.KEY_W)    -> List(Jump)
          )

        // Remap Up arrow to Duck, add Down arrow as Jump.
        val event =
          InputMapperEvent.AddMappings(
            KeyboardEvent.KeyUp(Keys.UP_ARROW)  -> List(Duck),
            KeyboardEvent.KeyDown(Keys.DOWN_ARROW) -> List(Jump)
          )

        val actual =
          (mapper.update(context(1))(event)).state.toMappingsList

        val expected =
          List(
            KeyboardEvent.KeyUp(Keys.UP_ARROW)  -> List(Duck),
            KeyboardEvent.KeyDown(Keys.DOWN_ARROW) -> List(Jump),
            KeyboardEvent.KeyUp(Keys.KEY_W)     -> List(Jump)
          )

        mappingsEqual(actual, expected) ==> true

      }

      "Mappings can be removed" - {

        val mapper =
          InputMapper.subsystem(
            KeyboardEvent.KeyUp(Keys.UP_ARROW)  -> List(Jump),
            KeyboardEvent.KeyUp(Keys.KEY_W)     -> List(Jump),
            KeyboardEvent.KeyDown(Keys.DOWN_ARROW) -> List(Duck)
          )

        // Remove all arrow key mappings
        val event =
          InputMapperEvent.RemoveMappings(List(KeyboardEvent.KeyUp(Keys.UP_ARROW), KeyboardEvent.KeyDown(Keys.DOWN_ARROW)))

        val actual =
          (mapper.update(context(1))(event)).state.toMappingsList

        val expected =
          List(
            KeyboardEvent.KeyUp(Keys.KEY_W) -> List(Jump)
          )

        mappingsEqual(actual, expected) ==> true

      }

      "It should map an input event to an action" - {

        val eUp = KeyboardEvent.KeyUp(Keys.UP_ARROW)
        val eDn = KeyboardEvent.KeyDown(Keys.DOWN_ARROW)
        val eKw = KeyboardEvent.KeyUp(Keys.KEY_W)

        val mapper =
          InputMapper.subsystem(
            eUp -> List(Jump),
            eDn -> List(Duck),
            eKw -> List(Jump)
          )

        (mapper.update(context(1))(InputMapperEvent.Input(eUp))).globalEvents.length ==> 1
        (mapper.update(context(1))(InputMapperEvent.Input(eUp))).globalEvents.head ==> Jump
        (mapper.update(context(1))(InputMapperEvent.Input(eDn))).globalEvents.head ==> Duck
        (mapper.update(context(1))(InputMapperEvent.Input(eKw))).globalEvents.head ==> Jump

      }

    }

  def mappingsEqual(a: List[(InputEvent, List[GlobalEvent])], b: List[(InputEvent, List[GlobalEvent])]): Boolean = {
    val res = a.length == b.length && a.forall(b.contains)

    if (res) res
    else {
      res
    }
  }

}
