package ingidoexamples.automata

import indigoexts.subsystems.automata._
import indigo.shared.datatypes.Rectangle

object FireworksAutomata {

  def subSystem(screenDimensions: Rectangle): Automata =
    Automata.empty
      .add(LaunchPadAutomaton.automaton)
      .add(RocketAutomaton.automaton(screenDimensions))
      .add(TrailAutomaton.automaton)

}
