package ingidoexamples.automata

import indigo._
import indigoexts.subsystems.automata._
import indigo.syntax._

import ingidoexamples.Assets

object CrossAutomaton {

  def spawnAt(position: Point): AutomataEvent.Spawn =
    AutomataEvent.Spawn(CrossAutomaton.poolKey, position, None)

  val poolKey: AutomataPoolKey = AutomataPoolKey("cross")

  val automaton: Automaton =
    Automaton(
      poolKey,
      Assets.cross,
      Millis(1500)
    ).withModifier(ModiferFunctions.signal)

  object ModiferFunctions {

    val multiplierS: AutomatonSeedValues => Signal[Double] =
      seed => Signal.fixed(seed.timeAliveDelta.toDouble / seed.lifeSpan.toDouble)

    val spawnPositionS: AutomatonSeedValues => Signal[Point] =
      seed => Signal.fixed(seed.spawnedAt)

    val renderableS: Renderable => Signal[Renderable] =
      renderable => Signal.fixed(renderable)

    val positionSF: SignalFunction[(Double, Point), Point] =
      SignalFunction {
        case (multiplier, spawnedAt) =>
          spawnedAt + Point(0, -(30 * multiplier).toInt)
      }

    val newPosition: AutomatonSeedValues => Signal[Point] =
      seed => Signal.product(multiplierS(seed), spawnPositionS(seed)) |> positionSF

    val signal: (AutomatonSeedValues, Renderable) => Signal[SceneUpdateFragment] =
      (seed, renderable) =>
        newPosition(seed).map {
          case position =>
            SceneUpdateFragment.empty.addGameLayerNodes(
              renderable match {
                case g: Graphic =>
                  g.moveTo(position)

                case r =>
                  r
              }
            )
        }

  }
}
