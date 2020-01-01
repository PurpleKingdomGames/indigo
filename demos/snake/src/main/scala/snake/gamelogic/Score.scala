package snake.gamelogic

import indigo._
import indigoexts.subsystems.automata._
import indigo.syntax._

object Score {

  val poolKey: AutomataPoolKey =
    AutomataPoolKey("points")

  def automataSubSystem(scoreAmount: String, fontKey: FontKey): Automata =
    Automata.empty
      .add(
        Automaton(
          poolKey,
          Text(scoreAmount, 0, 0, 1, fontKey).alignCenter,
          Millis(1500)
        ).withModifier(ModiferFunctions.signal)
      )

  val spawnEvent: Point => AutomataEvent =
    position => AutomataEvent.Spawn(poolKey, position)

  object ModiferFunctions {

    val input: AutomatonSeedValues => Signal[AutomatonSeedValues] =
      seedValues => Signal.fixed(seedValues)

    val multiplierSF: SignalFunction[AutomatonSeedValues, Double] =
      SignalFunction((s: AutomatonSeedValues) => s.timeAliveDelta.toDouble / s.lifeSpan.toDouble)

    val positionSF: SignalFunction[AutomatonSeedValues, Point] =
      SignalFunction((_: AutomatonSeedValues).spawnedAt)

    val mapSeedToPosition: SignalFunction[AutomatonSeedValues, Point] =
      (multiplierSF &&& positionSF) >>>
        SignalFunction {
          case (multiplier: Double, spawnedAt: Point) =>
            spawnedAt + Point(0, -(30 * multiplier).toInt)
        }

    val signal: (AutomatonSeedValues, Renderable) => Signal[Outcome[Renderable]] =
      (seed, renderable) =>
        (input(seed) |> mapSeedToPosition).map { position =>
          Outcome(
            renderable match {
              case t: Text =>
                t.moveTo(position)

              case r =>
                r
            }
          )
        }

  }

}
