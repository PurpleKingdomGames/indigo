package indigoexamples.automata

import indigo._
import indigoexts.subsystems.automata.AutomataPoolKey
import indigoexts.subsystems.automata.Automata
import indigoexts.subsystems.automata.Automaton
import indigoexts.subsystems.automata.AutomatonUpdate
import indigoexts.subsystems.automata.AutomataEvent
import indigoexts.subsystems.automata.AutomatonSeedValues
import indigoexts.geometry.Vertex

import indigoexamples.Assets
import indigoexamples.model.Flare
import indigoexamples.model.Projectiles

object FlareAutomata {

  val poolKey: AutomataPoolKey =
    AutomataPoolKey("flare")

  def automata(toScreenSpace: Vertex => Point): Automata =
    Automata(poolKey, automaton(toScreenSpace), Automata.Layer.Game)

  def automaton(toScreenSpace: Vertex => Point): Automaton =
    Automaton(
      Assets.cross,
      Millis(0)
    ).withModifier(ModifierFunctions.signal(toScreenSpace))

  def spawnEvent(flare: Flare): AutomataEvent.Spawn =
    AutomataEvent.Spawn(
      poolKey,
      Point.zero,
      Some(flare.flightTime),
      Some(flare)
    )

  object ModifierFunctions {

    def signal(toScreenSpace: Vertex => Point): (AutomatonSeedValues, SceneGraphNode) => Signal[AutomatonUpdate] =
      (sa, n) =>
        (sa.payload, n) match {
          case (Some(Flare(_, moveSignal, tint)), r: Renderable) =>
            for {
              position <- moveSignal |> SignalFunction(toScreenSpace)
              events   <- Projectiles.emitTrailEvents(position, tint, 3l)
            } yield AutomatonUpdate(List(r.moveTo(position)), events)

          case _ =>
            Signal.fixed(AutomatonUpdate.empty)

        }

  }

}
