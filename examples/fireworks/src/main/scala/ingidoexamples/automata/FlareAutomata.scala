package ingidoexamples.automata

import indigo._
import indigoexts.subsystems.automata.AutomataPoolKey
import indigoexts.subsystems.automata.Automata
import indigoexts.subsystems.automata.Automaton
import indigoexts.subsystems.automata.AutomatonUpdate
import indigoexts.subsystems.automata.AutomataEvent
import indigoexts.subsystems.automata.AutomatonSeedValues
import indigoexts.geometry.Vertex

import ingidoexamples.Assets
import ingidoexamples.model.Flare
import ingidoexamples.model.Projectiles

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

  def spawnEvent(flare: Flare): AutomataEvent.Spawn =
    AutomataEvent.Spawn(
      poolKey,
      flare.startPosition,
      Some(flare.flightTime),
      Some(flare)
    )

  object ModifierFunctions {

    val makeElements: SignalFunction[(Point, Renderable, Vertex), (Point, Renderable)] =
      SignalFunction {
        case (startPosition, renderable, vertex) =>
          (vertex.toPoint + startPosition, renderable)
      }

    val createUpdate: SignalFunction[(Point, Renderable), AutomatonUpdate] =
      SignalFunction.flatLift {
        case (pt, r) =>
          Projectiles.emitTrailEvents(pt).map { es =>
            AutomatonUpdate(List(r.moveTo(pt)), es)
          }
      }

    def signal: (AutomatonSeedValues, SceneGraphNode) => Signal[AutomatonUpdate] =
      (seed, node) =>
        (seed.payload, node) match {
          case (Some(Flare(startPosition, _, signal)), r: Renderable) =>
            signal.map(v => (startPosition, r, v)) |> makeElements >>> createUpdate

          case _ =>
            Signal.fixed(AutomatonUpdate.empty)
        }

  }

}
