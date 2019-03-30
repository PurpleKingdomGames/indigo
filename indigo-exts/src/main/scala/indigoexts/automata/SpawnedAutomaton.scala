package indigoexts.automata

import indigo.GameTime.Millis

final class SpawnedAutomaton(val automaton: Automaton, val seedValues: AutomatonSeedValues) {
  def isAlive(currentTime: Millis): Boolean =
    seedValues.createdAt + automaton.lifespan > currentTime

  def updateDelta(frameDelta: Millis): SpawnedAutomaton =
    new SpawnedAutomaton(automaton, seedValues.updateTimeAlive(frameDelta))
}
object SpawnedAutomaton {

  def apply(automaton: Automaton, seedValues: AutomatonSeedValues): SpawnedAutomaton =
    new SpawnedAutomaton(automaton, seedValues)

}