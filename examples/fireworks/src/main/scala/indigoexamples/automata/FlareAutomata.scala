package indigoexamples.automata

import indigo.*
import indigoexamples.Assets
import indigoexamples.model.Flare
import indigoexamples.model.Projectiles
import indigoextras.subsystems.*

object FlareAutomata {

  val poolKey: AutomataPoolKey =
    AutomataPoolKey("flare")

  def automata(toScreenSpace: Vertex => Point): Automata =
    Automata(poolKey, automaton(toScreenSpace))

  def automaton(toScreenSpace: Vertex => Point): Automaton =
    Automaton(
      AutomatonNode.Fixed(Assets.cross),
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

    def signal(toScreenSpace: Vertex => Point): SignalReader[(AutomatonSeedValues, SceneNode), AutomatonUpdate] =
      SignalReader {
        case (sa, n) =>
          (sa.payload, n) match {
            case (Some(Flare(_, moveSignal, tint)), r: Graphic[_]) =>
              for {
                position <- moveSignal |> SignalFunction(toScreenSpace)
                events   <- Projectiles.emitTrailEvents(position, tint, Millis(25).toSeconds)
              } yield AutomatonUpdate(Batch(r.moveTo(position)), events)

            case _ =>
              Signal.fixed(AutomatonUpdate.empty)

          }
      }

  }

}
