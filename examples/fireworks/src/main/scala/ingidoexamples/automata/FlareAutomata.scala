package ingidoexamples.automata

import indigoexts.subsystems.automata.AutomataPoolKey
import indigoexts.subsystems.automata.Automata
import indigoexts.subsystems.automata.Automaton
import ingidoexamples.Assets
import indigo.shared.time.Millis
import indigoexts.subsystems.automata.AutomatonSeedValues
import indigo.shared.scenegraph.SceneGraphNode
import indigo.shared.temporal.Signal
import indigoexts.subsystems.automata.AutomatonUpdate
import indigoexts.subsystems.automata.AutomataEvent
import indigo.shared.datatypes.Point

object FlareAutomata {

  val poolKey: AutomataPoolKey =
    AutomataPoolKey("flare")

  val automaton: Automaton =
    Automaton(
      Assets.cross,
      Millis(0)
    ).withModifier(ModifierFunctions.signal)

  val automata: Automata =
    Automata(poolKey, automaton, Automata.Layer.Game)

  def spawnEvent: AutomataEvent.Spawn =
    AutomataEvent.Spawn(
      poolKey,
      Point.zero,
      Some(Millis(1000)),
      None
    )

  object ModifierFunctions {

    def signal: (AutomatonSeedValues, SceneGraphNode) => Signal[AutomatonUpdate] =
      ???

  }

}
