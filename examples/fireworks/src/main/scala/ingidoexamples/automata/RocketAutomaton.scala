package ingidoexamples.automata

import indigo._
import indigoexts.subsystems.automata._
import ingidoexamples.Assets
import ingidoexamples.model.Rocket
import indigoexts.geometry.Vertex

object RocketAutomaton {

  val poolKey: AutomataPoolKey =
    AutomataPoolKey("rocket")

  def automaton(screenDimensions: Rectangle): Automaton =
    Automaton(
      poolKey,
      Assets.cross,
      Millis(1000)
    ).withModifier(ModifierFunctions.signal(screenDimensions))

  def spawnEvent(rocket: Rocket, launchPadPosition: Point): AutomataEvent.Spawn =
    AutomataEvent.Spawn(poolKey, launchPadPosition, Some(rocket.flightTime), Some(rocket))

  object ModifierFunctions {

    def toScreenSpace(launchPosition: Point, screenDimensions: Rectangle): SignalFunction[Vertex, Point] =
      SignalFunction { vertex =>
        // This is a positive value, but "Up" is a subtraction...
        val maxAltitude: Int        = ((screenDimensions.height - 5) / 6) * 5
        val maxHorizonalTravel: Int = screenDimensions.width / 2

        Point(
          x = launchPosition.x + (maxHorizonalTravel * vertex.x).toInt,
          y = launchPosition.y - (maxAltitude * vertex.y).toInt
        )
      }

    def signal(screenDimensions: Rectangle): (AutomatonSeedValues, Renderable) => Signal[SceneUpdateFragment] =
      (sa, r) =>
        sa.payload match {
          case Some(Rocket(_, moveSignal)) =>
            Signal.create { t =>
              SceneUpdateFragment.empty
                .addGameLayerNodes(r.moveTo((moveSignal |> toScreenSpace(sa.spawnedAt, screenDimensions)).at(t)))
            }

          case _ =>
            Signal.fixed(
              SceneUpdateFragment.empty
                .addGameLayerNodes(r.moveTo(sa.spawnedAt))
            )
        }

  }
}
