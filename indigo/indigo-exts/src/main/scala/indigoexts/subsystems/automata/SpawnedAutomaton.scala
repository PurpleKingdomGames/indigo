package indigoexts.subsystems.automata

import indigo.shared.time.{Millis, Seconds}

final class SpawnedAutomaton(val automaton: Automaton, val seedValues: AutomatonSeedValues) {
  def isAlive(currentTime: Millis): Boolean =
    seedValues.createdAt + seedValues.lifeSpan > currentTime

  def updateDelta(frameDelta: Seconds): SpawnedAutomaton =
    SpawnedAutomaton(automaton, seedValues.updateDelta(frameDelta))
}

object SpawnedAutomaton {

  def apply(automaton: Automaton, seedValues: AutomatonSeedValues): SpawnedAutomaton =
    new SpawnedAutomaton(automaton, seedValues)

}
