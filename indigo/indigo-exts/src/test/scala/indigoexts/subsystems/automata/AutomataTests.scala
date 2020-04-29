package indigoexts.subsystems.automata

import utest._
import indigo.shared.scenegraph.Graphic
import indigo.shared.events.GlobalEvent
import indigo.shared.time.GameTime
import indigo.shared.dice.Dice
import indigo.shared.datatypes.Point
import indigo.shared.Outcome
import indigo.shared.assets.AssetName
import indigo.shared.datatypes.Material
import indigo.shared.time.Seconds
import indigo.shared.events.InputState

object AutomataTests extends TestSuite {

  final case class MyCullEvent(message: String) extends GlobalEvent

  val eventInstance =
    MyCullEvent("Hello, I'm dead.")

  val poolKey: AutomataPoolKey =
    AutomataPoolKey("test")

  val automaton: Automaton =
    Automaton(
      Graphic(0, 0, 10, 10, 1, Material.Textured(AssetName("fish"))),
      Seconds(0.1)
    ).withOnCullEvent { _ =>
      List(eventInstance)
    }

  val automata: Automata =
    Automata(poolKey, automaton, Automata.Layer.Game)

  val tests: Tests =
    Tests {

      "culling an automaton should result in an event" - {

        val farmWithAutomaton: Automata =
          automata
            .update(GameTime.zero, InputState.default, Dice.loaded(1))(AutomataEvent.Spawn(poolKey, Point.zero, None, None))
            .state

        // 1 ms over the lifespan, so should be culled
        val outcome: Outcome[Automata] =
          farmWithAutomaton
            .update(GameTime.is(Seconds(0.15)), InputState.default, Dice.loaded(1))(AutomataEvent.Cull)

        outcome.state.liveAutomataCount ==> 0
        outcome.globalEvents.head ==> eventInstance

      }

    }

}
