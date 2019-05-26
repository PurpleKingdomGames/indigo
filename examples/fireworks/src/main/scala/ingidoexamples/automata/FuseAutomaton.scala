package ingidoexamples.automata

import indigo._
import indigoexts.subsystems.automata._
import ingidoexamples.Assets
import ingidoexamples.model.Fuse

object FuseAutomaton {

  val MaxFuseLength: Int =
    1500

  val poolKey: AutomataPoolKey =
    AutomataPoolKey("fuse")

  val automaton: Automaton =
    Automaton(
      poolKey,
      Assets.cross,
      Millis(MaxFuseLength.toLong)
    ).withOnCullEvent { seed =>
      seed.payload match {
        case Some(Fuse(_, _, rocket)) =>
          Option(
            RocketAutomaton.spawnEvent(rocket)
          )
          
        case _ =>
          None
      }

    }

  def spawnEvent(fuse: Fuse): AutomataEvent.Spawn =
    AutomataEvent.Spawn(poolKey, fuse.position, Some(fuse.length), Some(fuse))

}
