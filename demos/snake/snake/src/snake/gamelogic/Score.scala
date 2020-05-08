package snake.gamelogic

import indigo._
import indigoexts.subsystems.automata._

object Score {

  val poolKey: AutomataPoolKey =
    AutomataPoolKey("points")

  def automataSubSystem(scoreAmount: String, fontKey: FontKey): Automata =
    Automata(
      poolKey,
      Automaton(
        Text(scoreAmount, 0, 0, 1, fontKey).alignCenter,
        Seconds(1.5)
      ).withModifier(ModiferFunctions.signal),
      Automata.Layer.UI
    )

  val spawnEvent: Point => AutomataEvent =
    position => AutomataEvent.Spawn(poolKey, position, None, None)

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

    val signal: (AutomatonSeedValues, SceneGraphNode) => Signal[AutomatonUpdate] =
      (seed, sceneGraphNode) =>
        (input(seed) |> mapSeedToPosition).map { position =>
          AutomatonUpdate(
            sceneGraphNode match {
              case t: Text =>
                List(t.moveTo(position))

              case _ =>
                Nil
            },
            Nil
          )
        }

  }

}
