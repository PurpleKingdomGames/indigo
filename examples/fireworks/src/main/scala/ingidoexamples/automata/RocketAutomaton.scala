package ingidoexamples.automata

import indigo._
import indigoexts.subsystems.automata._
import ingidoexamples.Assets
import ingidoexamples.model.Rocket
import indigoexts.geometry.Vertex
import indigo.EqualTo._

object RocketAutomaton {

  val poolKey: AutomataPoolKey =
    AutomataPoolKey("rocket")

  def automaton(screenDimensions: Rectangle): Automaton =
    Automaton(
      Assets.cross,
      Millis(0)
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

    def signal(screenDimensions: Rectangle): (AutomatonSeedValues, SceneGraphNode) => Signal[AutomatonUpdate] =
      (sa, n) =>
        n match {
          case r: Renderable =>
            sa.payload match {
              case Some(Rocket(_, moveSignal)) =>
                Signal.create { t =>
                  val position: Point =
                    (moveSignal |> toScreenSpace(sa.spawnedAt, screenDimensions)).at(t)

                  AutomatonUpdate(
                    List(r.moveTo(position)),
                    if(t.toInt % 2 === 0 ) List(TrailAutomaton.spawnEvent(position)) else Nil
                  )
                }

              case _ =>
                Signal.fixed(
                  AutomatonUpdate.withNodes(r.moveTo(sa.spawnedAt))
                )
            }

          case _ =>
            Signal.fixed(AutomatonUpdate.empty)
        }

  }
}
