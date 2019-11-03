package ingidoexamples.automata

import indigoexts.subsystems.automata._
import indigo.shared.datatypes.Rectangle

object FireworksAutomata {

  def launchPad: Automata =
    Automata(LaunchPadAutomaton.poolKey, LaunchPadAutomaton.automaton, Automata.Layer.Game)

  def rocket(screenDimensions: Rectangle): Automata =
    Automata(RocketAutomaton.poolKey, RocketAutomaton.automaton(screenDimensions), Automata.Layer.Game)

  def trail: Automata =
    Automata(TrailAutomaton.poolKey, TrailAutomaton.automaton, Automata.Layer.Game)

}
