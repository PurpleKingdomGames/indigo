package indigoexts.automata

trait AutomataAliases {
  type Automaton = indigoexts.automata.Automaton

  type AutomataEvent = indigoexts.automata.AutomataEvent
  val AutomataEvent: indigoexts.automata.AutomataEvent.type = indigoexts.automata.AutomataEvent

  type AutomataModifier = indigoexts.automata.AutomataModifier
  val AutomataModifier: indigoexts.automata.AutomataModifier.type = indigoexts.automata.AutomataModifier

  val AutomataFarm: indigoexts.automata.AutomataFarm.type               = indigoexts.automata.AutomataFarm
  val SpawnedAutomaton: indigoexts.automata.SpawnedAutomaton.type       = indigoexts.automata.SpawnedAutomaton
  val AutomatonSeedValues: indigoexts.automata.AutomatonSeedValues.type = indigoexts.automata.AutomatonSeedValues
  val GraphicAutomaton: indigoexts.automata.GraphicAutomaton.type       = indigoexts.automata.GraphicAutomaton
  val SpriteAutomaton: indigoexts.automata.SpriteAutomaton.type         = indigoexts.automata.SpriteAutomaton
  val TextAutomaton: indigoexts.automata.TextAutomaton.type             = indigoexts.automata.TextAutomaton
  val AutomataPoolKey: indigoexts.automata.AutomataPoolKey.type         = indigoexts.automata.AutomataPoolKey
  val AutomataLifeSpan: indigoexts.automata.AutomataLifeSpan.type       = indigoexts.automata.AutomataLifeSpan
}
