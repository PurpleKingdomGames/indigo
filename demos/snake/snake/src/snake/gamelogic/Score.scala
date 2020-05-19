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

    val mapSeedToPosition: SignalFunction[AutomatonSeedValues, Point] =
      SignalFunction { seed =>
        seed.spawnedAt +
          Point(
            0,
            -(30d * (seed.timeAliveDelta.toDouble / seed.lifeSpan.toDouble)).toInt
          )
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
