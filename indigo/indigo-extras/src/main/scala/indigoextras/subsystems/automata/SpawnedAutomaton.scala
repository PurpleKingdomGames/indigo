package indigoextras.subsystems.automata

import indigo.shared.time.Seconds

final class SpawnedAutomaton(val automaton: Automaton, val seedValues: AutomatonSeedValues) {
  def isAlive(currentTime: Seconds): Boolean =
    seedValues.createdAt + seedValues.lifeSpan > currentTime
}

object SpawnedAutomaton {

  def apply(automaton: Automaton, seedValues: AutomatonSeedValues): SpawnedAutomaton =
    new SpawnedAutomaton(automaton, seedValues)

}
