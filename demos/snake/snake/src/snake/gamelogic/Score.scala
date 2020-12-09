package snake.gamelogic

import indigo._
import indigoextras.subsystems._

object Score {

  val poolKey: AutomataPoolKey =
    AutomataPoolKey("points")

  def automataSubSystem(scoreAmount: String, fontKey: FontKey): Automata =
    Automata(
      poolKey,
      Automaton(
        AutomatonNode.Fixed(Text(scoreAmount, 0, 0, 1, fontKey).alignCenter),
        Seconds(1.5)
      ).withModifier(ModiferFunctions.signal),
      Automata.Layer.UI
    )

  val spawnEvent: Point => AutomataEvent =
    position => AutomataEvent.Spawn(poolKey, position, None, None)

  object ModiferFunctions {

    val workOutPosition: AutomatonSeedValues => Signal[Point] =
      seed =>
        Signal { time =>
          seed.spawnedAt +
            Point(
              0,
              -(30d * (seed.progression(time))).toInt
            )
        }

    val signal: SignalReader[(AutomatonSeedValues, SceneGraphNode), AutomatonUpdate] =
      SignalReader {
        case (seed, sceneGraphNode) =>
          sceneGraphNode match {
            case t: Text =>
              workOutPosition(seed).map { position =>
                AutomatonUpdate(List(t.moveTo(position)), Nil)
              }

            case _ =>
              Signal.fixed(AutomatonUpdate.empty)
          }
      }

  }

}
