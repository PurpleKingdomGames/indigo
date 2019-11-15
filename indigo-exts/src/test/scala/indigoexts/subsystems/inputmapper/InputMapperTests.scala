package indigoexts.subsystems.inputmapper

import utest._
import indigo.shared.events.KeyboardEvent
import indigo.shared.constants.KeyCode
import indigo.shared.constants.Keys
import indigo.shared.events.InputEvent
import indigo.shared.time.GameTime
import indigo.shared.dice.Dice
import indigo.shared.events.GlobalEvent

object InputMapperTests extends TestSuite {

  case object Jump extends GlobalEvent
  case object Duck extends GlobalEvent

  val tests: Tests =
    Tests {

      "An input mapper can be initialised" - {

        val mapper =
          InputMapper.subsystem(
            KeyboardEvent.KeyPress(Keys.UP_ARROW)  -> List(Jump),
            KeyboardEvent.KeyPress(Keys.KEY_W)     -> List(Jump),
            KeyboardEvent.KeyDown(Keys.DOWN_ARROW) -> List(Duck)
          )

        val actual: List[(InputEvent, List[GlobalEvent])] =
          mapper.toMappingsList

        val expected =
          List(
            KeyboardEvent.KeyPress(Keys.UP_ARROW)  -> List(Jump),
            KeyboardEvent.KeyDown(Keys.DOWN_ARROW) -> List(Duck),
            KeyboardEvent.KeyPress(Keys.KEY_W)     -> List(Jump)
          )

        mappingsEqual(actual, expected) ==> true

      }

      "Mappings can be added" - {

        val mapper =
          InputMapper.subsystem(
            KeyboardEvent.KeyPress(Keys.UP_ARROW) -> List(Jump),
            KeyboardEvent.KeyPress(Keys.KEY_W)    -> List(Jump)
          )

        // Remap Up arrow to Duck, add Down arrow as Jump.
        val event =
          InputMapperEvent.AddMappings(
            KeyboardEvent.KeyPress(Keys.UP_ARROW)  -> List(Duck),
            KeyboardEvent.KeyDown(Keys.DOWN_ARROW) -> List(Jump)
          )

        val actual =
          (mapper.update(GameTime.zero, Dice.loaded(1))(event)).state.toMappingsList

        val expected =
          List(
            KeyboardEvent.KeyPress(Keys.UP_ARROW)  -> List(Duck),
            KeyboardEvent.KeyDown(Keys.DOWN_ARROW) -> List(Jump),
            KeyboardEvent.KeyPress(Keys.KEY_W)     -> List(Jump)
          )

        mappingsEqual(actual, expected) ==> true

      }

      "Mappings can be removed" - {

        val mapper =
          InputMapper.subsystem(
            KeyboardEvent.KeyPress(Keys.UP_ARROW)  -> List(Jump),
            KeyboardEvent.KeyPress(Keys.KEY_W)     -> List(Jump),
            KeyboardEvent.KeyDown(Keys.DOWN_ARROW) -> List(Duck)
          )

        // Remove all arrow key mappings
        val event =
          InputMapperEvent.RemoveMappings(List(KeyboardEvent.KeyPress(Keys.UP_ARROW), KeyboardEvent.KeyDown(Keys.DOWN_ARROW)))

        val actual =
          (mapper.update(GameTime.zero, Dice.loaded(1))(event)).state.toMappingsList

        val expected =
          List(
            KeyboardEvent.KeyPress(Keys.KEY_W) -> List(Jump)
          )

        mappingsEqual(actual, expected) ==> true

      }

      "It should map an input event to an action" - {

        val eUp = KeyboardEvent.KeyPress(Keys.UP_ARROW)
        val eDn = KeyboardEvent.KeyDown(Keys.DOWN_ARROW)
        val eKw = KeyboardEvent.KeyPress(Keys.KEY_W)

        val mapper =
          InputMapper.subsystem(
            eUp -> List(Jump),
            eDn -> List(Duck),
            eKw -> List(Jump)
          )

        (mapper.update(GameTime.zero, Dice.loaded(1))(InputMapperEvent.Input(eUp))).globalEvents.length ==> 1
        (mapper.update(GameTime.zero, Dice.loaded(1))(InputMapperEvent.Input(eUp))).globalEvents.head ==> Jump
        (mapper.update(GameTime.zero, Dice.loaded(1))(InputMapperEvent.Input(eDn))).globalEvents.head ==> Duck
        (mapper.update(GameTime.zero, Dice.loaded(1))(InputMapperEvent.Input(eKw))).globalEvents.head ==> Jump

      }

    }

  def mappingsEqual(a: List[(InputEvent, List[GlobalEvent])], b: List[(InputEvent, List[GlobalEvent])]): Boolean = {
    val res = a.length == b.length && a.forall(b.contains)

    if (res) res
    else {
      println("a> " + a)
      println("b> " + b)
      res
    }
  }

}
