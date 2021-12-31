package indigoexamples.automata

import indigo._
import indigoexamples.Assets
import indigoexamples.model.Projectiles
import indigoexamples.model.Rocket
import indigoextras.geometry.Vertex
import indigoextras.subsystems._

object RocketAutomata {

  val poolKey: AutomataPoolKey =
    AutomataPoolKey("rocket")

  def automata(toScreenSpace: Vertex => Point): Automata =
    Automata(poolKey, automaton(toScreenSpace))

  def automaton(toScreenSpace: Vertex => Point): Automaton =
    Automaton(
      AutomatonNode.Fixed(Assets.cross),
      Seconds.zero
    ).withModifier(ModifierFunctions.signal(toScreenSpace))
      .withOnCullEvent(launchFlares)

  val launchFlares: AutomatonSeedValues => List[GlobalEvent] = seed => {
    seed.payload match {
      case Some(Rocket(_, _, flares, _)) =>
        flares.map(f => FlareAutomata.spawnEvent(f))

      case _ =>
        Nil
    }
  }

  def spawnEvent(rocket: Rocket, launchPadPosition: Point): AutomataEvent.Spawn =
    AutomataEvent.Spawn(poolKey, launchPadPosition, Some(rocket.flightTime), Some(rocket))

  object ModifierFunctions {

    def signal(toScreenSpace: Vertex => Point): SignalReader[(AutomatonSeedValues, SceneNode), AutomatonUpdate] =
      SignalReader {
        case (sa, n) =>
          (sa.payload, n) match {
            case (Some(Rocket(_, moveSignal, _, tint)), r: Graphic[_]) =>
              for {
                position <- moveSignal |> SignalFunction(toScreenSpace)
                events   <- Projectiles.emitTrailEvents(position, tint, Millis(25).toSeconds)
              } yield AutomatonUpdate(List(r.moveTo(position)), events)

            case _ =>
              Signal.fixed(AutomatonUpdate.empty)
          }
      }

  }
}
