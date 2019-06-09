package indigoexts.subsystems.automata

import indigo.shared.time.Millis

final case class SpawnedAutomaton(automaton: Automaton, seedValues: AutomatonSeedValues) {
  def isAlive(currentTime: Millis): Boolean =
    seedValues.createdAt + seedValues.lifeSpan > currentTime

  def updateDelta(frameDelta: Millis): SpawnedAutomaton =
    this.copy(seedValues = seedValues.updateDelta(frameDelta))
}
