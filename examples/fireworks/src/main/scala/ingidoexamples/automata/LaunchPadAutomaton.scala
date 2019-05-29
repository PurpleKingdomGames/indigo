package ingidoexamples.automata

import indigo._
import indigoexts.subsystems.automata._
import ingidoexamples.Assets
import ingidoexamples.model.LaunchPad

object LaunchPadAutomaton {

  val MinCountDown: Int = 100
  val MaxCountDown: Int = 750

  val poolKey: AutomataPoolKey =
    AutomataPoolKey("launchPad")

  val automaton: Automaton =
    Automaton(
      poolKey,
      Assets.cross,
      Millis(MaxCountDown.toLong)
    ).withOnCullEvent { seed =>
      seed.payload match {
        case Some(LaunchPad(_, _, rocket)) =>
          Option(
            RocketAutomaton.spawnEvent(rocket)
          )

        case _ =>
          None
      }

    }

  def spawnEvent(launchPad: LaunchPad): AutomataEvent.Spawn =
    AutomataEvent.Spawn(poolKey, launchPad.position, Some(launchPad.countDown), Some(launchPad))

}
