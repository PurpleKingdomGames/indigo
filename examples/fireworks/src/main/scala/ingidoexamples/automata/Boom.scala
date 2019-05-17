package ingidoexamples.automata

import indigo._
import indigoexts.subsystems.automata._

import ingidoexamples.Assets

object Boom {

  val poolKey: AutomataPoolKey =
    AutomataPoolKey("boom")

  val automaton: Automaton =
    Automaton(
      poolKey,
      Assets.cross,
      Millis(1)
    ).withModifier(
      (_, renderable) => Signal.fixed(SceneUpdateFragment.empty.addGameLayerNodes(renderable))
    )

}
