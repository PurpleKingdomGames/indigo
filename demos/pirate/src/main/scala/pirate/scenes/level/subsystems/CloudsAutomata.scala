package pirate.scenes.level.subsystems

import indigo.*
import indigoextras.subsystems.*
import pirate.core.Assets

/*
An instance of a standard Automata SubSystem, you can think of this
one as a little cloud factory, puffing out new clouds on demand.
 */
object CloudsAutomata:

  /*
  The Signal controls the clouds movement over time, the "over time" part is critical.
  `Signal.Lerp` mean "Linear Interpolation" i.e. smoothly transition from position A
  to position B over a given time frame.
  The important thing here is that the Signal doesn't know what the time actually is
  and relies on being told. Which means you can also go back in time by lying to the
  signal!
  Lerp then is a function `t: Seconds => position: Point`, and to make use of it we
  map the signal to our renderable cloud graphic.
   */
  val signal: SignalReader[(AutomatonSeedValues, SceneNode), AutomatonUpdate] =
    SignalReader { case (seed, node) =>
      node match
        case cloud: Graphic[_] =>
          Signal
            .Lerp(seed.spawnedAt, Point(-150, seed.spawnedAt.y), seed.lifeSpan)
            .map { position =>
              AutomatonUpdate(cloud.moveTo(position))
            }

        case _ =>
          Signal.fixed(AutomatonUpdate.empty)

    }

  // One spawn, the automaton instance will choose "OneOf"
  // these graphics to be drawn as for the rest of its life.
  val automaton: Automaton =
    Automaton(
      AutomatonNode.OneOf(
        Assets.Clouds.cloud1,
        Assets.Clouds.cloud2,
        Assets.Clouds.cloud3
      ),
      Seconds.zero,
      signal,
      _ => Nil
    )

  val poolKey: AutomataPoolKey = AutomataPoolKey("cloud")

  val automata: Automata =
    Automata(
      poolKey,
      automaton,
      BindingKey("small clouds")
    ).withMaxPoolSize(15)
