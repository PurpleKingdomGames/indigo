package ingidoexamples.automata

import indigo._
import indigoexts.subsystems.automata._
import ingidoexamples.Assets
import ingidoexamples.model.LaunchPad

object LaunchPadAutomaton {

  val MaxCountDown: Int =
    1500

  val poolKey: AutomataPoolKey =
    AutomataPoolKey("fuse")

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

  def spawnEvent(fuse: LaunchPad): AutomataEvent.Spawn =
    AutomataEvent.Spawn(poolKey, fuse.position, Some(fuse.length), Some(fuse))

}
