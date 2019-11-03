package ingidoexamples.automata

import indigo._
import indigoexts.subsystems.automata._

import ingidoexamples.Assets

object Boom {

  val poolKey: AutomataPoolKey =
    AutomataPoolKey("boom")

  val automaton: Automaton =
    Automaton(
      Assets.cross,
      Millis(1)
    ).withModifier(
      (_, renderable) => Signal.fixed(AutomatonUpdate.withNodes(renderable))
    )

}
