package pirate.subsystems

import indigo._
import indigoextras.subsystems.automata._
import indigoextras.geometry.Bezier
import indigoextras.geometry.Vertex
import pirate.init.Assets

object CloudsAutomata {

  val signal: (AutomatonSeedValues, SceneGraphNode) => Signal[AutomatonUpdate] =
    (seed, node) =>
      node match {
        case sprite: Sprite =>
          Bezier(Vertex.fromPoint(seed.spawnedAt), Vertex(-100, seed.spawnedAt.y.toDouble))
            .toSignal(seed.lifeSpan)
            .map { vertex =>
              sprite
                .moveTo(vertex.x.toInt, seed.spawnedAt.y)
                .jumpToFrame((seed.randomSeed % 3))
            }
            .map(s => AutomatonUpdate(List(s), Nil))

        case _ =>
          Signal.fixed(AutomatonUpdate.empty)
      }

  val automaton: Automaton =
    Automaton.create(
      Sprite(BindingKey("small clouds"), 0, 0, 45, Assets.Clouds.animationKey),
      Seconds.zero,
      signal,
      _ => Nil
    )

  val poolKey: AutomataPoolKey = AutomataPoolKey("cloud")

  val automata: Automata =
    Automata(
      poolKey,
      automaton,
      Automata.Layer.Game
    ).withMaxPoolSize(15)

}
