package ingidoexamples.automata

import indigo._
import indigoexts.subsystems.automata._
import ingidoexamples.Assets
import ingidoexamples.model.Rocket

object RocketAutomaton {

  val poolKey: AutomataPoolKey =
    AutomataPoolKey("rocket")

  val automaton: Automaton =
    Automaton(
      poolKey,
      Assets.cross,
      Millis(1000)
    ).withModifier(ModifierFunctions.signal)

  def spawnEvent(rocket: Rocket): AutomataEvent.Spawn =
    AutomataEvent.Spawn(poolKey, rocket.startPosition, None, Some(rocket))

  object ModifierFunctions {

    val signal: (AutomatonSeedValues, Renderable) => Signal[SceneUpdateFragment] =
      (sa, r) =>
        sa.payload match {
          case Some(Rocket(_, moveSignal)) =>
            Signal.create { t =>
              SceneUpdateFragment.empty
                .addGameLayerNodes(r.moveTo(moveSignal.at(t)))
            }

          case _ =>
            Signal.fixed(
              SceneUpdateFragment.empty
                .addGameLayerNodes(r.moveTo(sa.spawnedAt))
            )
        }

  }
}
