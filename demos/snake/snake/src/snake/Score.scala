package snake

import indigo._
import indigoextras.subsystems._
import snake.init.GameAssets

object Score {

  val poolKey: AutomataPoolKey =
    AutomataPoolKey("points")

  def automataSubSystem(scoreAmount: String, fontKey: FontKey): Automata =
    Automata(
      poolKey,
      Automaton(
        AutomatonNode.Fixed(Text(scoreAmount, 0, 0, 1, fontKey, GameAssets.fontMaterial).alignCenter),
        Seconds(1.5)
      ).withModifier(ModiferFunctions.signal),
      BindingKey("score")
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

    val signal: SignalReader[(AutomatonSeedValues, SceneNode), AutomatonUpdate] =
      SignalReader { case (seed, sceneGraphNode) =>
        sceneGraphNode match {
          case t: Text[_] =>
            workOutPosition(seed).map { position =>
              AutomatonUpdate(Batch(t.moveTo(position)), Batch.empty)
            }

          case _ =>
            Signal.fixed(AutomatonUpdate.empty)
        }
      }

  }

}
