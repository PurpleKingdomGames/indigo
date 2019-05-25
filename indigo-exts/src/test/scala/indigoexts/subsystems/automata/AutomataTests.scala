package indigoexts.subsystems.automata

import utest._
import indigo.shared.time.Millis
import indigo.shared.scenegraph.Graphic
import indigo.shared.events.GlobalEvent
import indigo.shared.time.GameTime
import indigo.shared.dice.Dice
import indigo.shared.datatypes.Point
import indigo.shared.Outcome

object AutomataTests extends TestSuite {

  final case class MyCullEvent(message: String) extends GlobalEvent

  val eventInstance =
    MyCullEvent("Hello, I'm dead.")

  val poolKey: AutomataPoolKey =
    AutomataPoolKey("test")

  val automaton: Automaton =
    Automaton(
      poolKey,
      Graphic(0, 0, 10, 10, 1, "fish"),
      Millis(100)
    ).withOnCullEvent { _ =>
      Some(eventInstance)
    }

  val inventory: Map[AutomataPoolKey, Automaton] =
    Map(poolKey -> automaton)

  val farm: Automata =
    Automata(
      inventory,
      paddock = Nil
    )

  val tests: Tests =
    Tests {

      "culling an automaton should result in an event" - {

        val farmWithAutomaton: Automata =
          Automata
            .spawn(
              farm,
              GameTime.zero,
              Dice.loaded(1),
              poolKey,
              Point.zero,
              None
            )
            .state

        val outcome: Outcome[Automata] =
          Automata.cullPaddock(
            farmWithAutomaton,
            GameTime.is(Millis(101)) // 1 ms over the lifespan, so should be culled
          )

        outcome.state.paddock.isEmpty ==> true
        outcome.globalEvents.head ==> eventInstance

      }

    }

}
