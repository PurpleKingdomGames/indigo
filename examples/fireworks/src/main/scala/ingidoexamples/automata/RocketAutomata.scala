package ingidoexamples.automata

import indigo._
import indigoexts.subsystems.automata._
import ingidoexamples.Assets
import ingidoexamples.model.Rocket
import ingidoexamples.model.Projectiles
import indigoexts.geometry.Vertex

object RocketAutomata {

  val poolKey: AutomataPoolKey =
    AutomataPoolKey("rocket")

  def automata(toScreenSpace: Vertex => Point): Automata =
    Automata(poolKey, automaton(toScreenSpace), Automata.Layer.Game)

  def automaton(toScreenSpace: Vertex => Point): Automaton =
    Automaton(
      Assets.cross,
      Millis(0)
    ).withModifier(ModifierFunctions.signal(toScreenSpace))
      .withOnCullEvent(launchFlares)

  val launchFlares: AutomatonSeedValues => List[GlobalEvent] = seed => {
    seed.payload match {
      case Some(Rocket(_, _, flares)) =>
        flares.map(f => FlareAutomata.spawnEvent(f))

      case _ =>
        Nil
    }
  }

  def spawnEvent(rocket: Rocket, launchPadPosition: Point): AutomataEvent.Spawn =
    AutomataEvent.Spawn(poolKey, launchPadPosition, Some(rocket.flightTime), Some(rocket))

  object ModifierFunctions {

    def signal(toScreenSpace: Vertex => Point): (AutomatonSeedValues, SceneGraphNode) => Signal[AutomatonUpdate] =
      (sa, n) =>
        (sa.payload, n) match {
          case (Some(Rocket(_, moveSignal, _)), r: Renderable) =>
            for {
              position <- moveSignal |> SignalFunction(toScreenSpace)
              events   <- Projectiles.emitTrailEvents(position)
            } yield AutomatonUpdate(List(r.moveTo(position)), events)

          case _ =>
            Signal.fixed(AutomatonUpdate.empty)
        }

  }
}
