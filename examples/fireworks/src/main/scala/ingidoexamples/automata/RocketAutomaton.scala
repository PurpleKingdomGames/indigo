package ingidoexamples.automata

import indigo._
import indigoexts.subsystems.automata._
import ingidoexamples.Assets
import ingidoexamples.model.Rocket

object RocketAutomaton {

  val poolKey: AutomataPoolKey =
    AutomataPoolKey("rocket")

  val automaton: Automaton =
    Automaton(
      poolKey,
      Assets.cross,
      Millis(1000)
    )

  def spawnEvent(rocket: Rocket): AutomataEvent.Spawn =
    AutomataEvent.Spawn(poolKey, rocket.startPosition, None, None)

}
