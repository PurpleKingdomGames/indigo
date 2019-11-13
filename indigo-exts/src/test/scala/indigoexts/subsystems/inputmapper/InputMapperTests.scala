package indigoexts.subsystems.inputmapper

import utest._
import indigo.shared.events.KeyboardEvent
import indigo.shared.constants.KeyCode
import indigo.shared.constants.Keys
import indigo.shared.events.InputEvent
import indigo.shared.time.GameTime
import indigo.shared.dice.Dice

object InputMapperTests extends TestSuite {

  case object Jump extends InputAction
  case object Duck extends InputAction

  val tests: Tests =
    Tests {

      "An input mapper can be initialised" - {

        val mapper =
          InputMapper.subsystem(
            KeyboardEvent.KeyPress(Keys.UP_ARROW)  -> Jump,
            KeyboardEvent.KeyPress(Keys.KEY_W)     -> Jump,
            KeyboardEvent.KeyDown(Keys.DOWN_ARROW) -> Duck
          )

        val actual: List[(InputEvent, InputAction)] =
          mapper.toMappingsList

        val expected =
          List(
            KeyboardEvent.KeyPress(Keys.UP_ARROW)  -> Jump,
            KeyboardEvent.KeyDown(Keys.DOWN_ARROW) -> Duck,
            KeyboardEvent.KeyPress(Keys.KEY_W)     -> Jump
          )

        mappingsEqual(actual, expected) ==> true

      }

      "Mappings can be added" - {

        val mapper =
          InputMapper.subsystem(
            KeyboardEvent.KeyPress(Keys.UP_ARROW) -> Jump,
            KeyboardEvent.KeyPress(Keys.KEY_W)    -> Jump
          )

        // Remap Up arrow to Duck, add Down arrow as Jump.
        val event =
          InputMapperEvent.AddMappings(
            List(
              KeyboardEvent.KeyPress(Keys.UP_ARROW)  -> Duck,
              KeyboardEvent.KeyDown(Keys.DOWN_ARROW) -> Jump
            )
          )

        val actual =
          (mapper.update(GameTime.zero, Dice.loaded(1))(event)).state.toMappingsList

        val expected =
          List(
            KeyboardEvent.KeyPress(Keys.UP_ARROW)  -> Duck,
            KeyboardEvent.KeyDown(Keys.DOWN_ARROW) -> Jump,
            KeyboardEvent.KeyPress(Keys.KEY_W)     -> Jump
          )

        mappingsEqual(actual, expected) ==> true

      }

      "Mappings can be removed" - {

        val mapper =
          InputMapper.subsystem(
            KeyboardEvent.KeyPress(Keys.UP_ARROW)  -> Jump,
            KeyboardEvent.KeyPress(Keys.KEY_W)     -> Jump,
            KeyboardEvent.KeyDown(Keys.DOWN_ARROW) -> Duck
          )

        // Remove all arrow key mappings
        val event =
          InputMapperEvent.RemoveMappings(List(KeyboardEvent.KeyPress(Keys.UP_ARROW), KeyboardEvent.KeyDown(Keys.DOWN_ARROW)))

        val actual =
          (mapper.update(GameTime.zero, Dice.loaded(1))(event)).state.toMappingsList

        val expected =
          List(
            KeyboardEvent.KeyPress(Keys.KEY_W) -> Jump
          )

        mappingsEqual(actual, expected) ==> true

      }

      "It should map an input event to an action" - {

        val eUp = KeyboardEvent.KeyPress(Keys.UP_ARROW)
        val eDn = KeyboardEvent.KeyDown(Keys.DOWN_ARROW)
        val eKw = KeyboardEvent.KeyPress(Keys.KEY_W)

        val mapper =
          InputMapper.subsystem(
            eUp -> Jump,
            eDn -> Duck,
            eKw -> Jump
          )

        (mapper.update(GameTime.zero, Dice.loaded(1))(InputMapperEvent.Input(eUp))).globalEvents.length ==> 1
        (mapper.update(GameTime.zero, Dice.loaded(1))(InputMapperEvent.Input(eUp))).globalEvents.head ==> InputMapperEvent.Action(Jump)
        (mapper.update(GameTime.zero, Dice.loaded(1))(InputMapperEvent.Input(eDn))).globalEvents.head ==> InputMapperEvent.Action(Duck)
        (mapper.update(GameTime.zero, Dice.loaded(1))(InputMapperEvent.Input(eKw))).globalEvents.head ==> InputMapperEvent.Action(Jump)

      }

    }

  def mappingsEqual(a: List[InputMapper.Mapping], b: List[InputMapper.Mapping]): Boolean = {
    val res = a.length == b.length && a.forall(b.contains)

    if (res) res
    else {
      println("a> " + a)
      println("b> " + b)
      res
    }
  }

}
