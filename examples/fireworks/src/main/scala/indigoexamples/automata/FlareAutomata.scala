package indigoexamples.automata

import indigo._
import indigoextras.subsystems._
import indigoextras.geometry.Vertex

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
      Seconds.zero
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
              events   <- Projectiles.emitTrailEvents(position, tint, Millis(25).toSeconds)
            } yield AutomatonUpdate(List(r.moveTo(position)), events)

          case _ =>
            Signal.fixed(AutomatonUpdate.empty)

        }

  }

}
