package indigoexamples.automata

import indigo._
import indigoexamples.Assets
import indigoexamples.model.TrailParticle
import indigoextras.subsystems._

object TrailAutomata {

  val poolKey: AutomataPoolKey =
    AutomataPoolKey("trail")

  val automaton: Automaton =
    Automaton(
      AutomatonNode.Fixed(Assets.cross),
      Seconds(0.25)
    ).withModifier(Modifer.signal)

  val automata: Automata =
    Automata(poolKey, automaton)
      .withMaxPoolSize(500)

  def spawnEvent(at: Point, tint: RGBA): AutomataEvent.Spawn =
    AutomataEvent.Spawn(
      poolKey,
      at,
      None,
      Some(TrailParticle.create(tint))
    )

  object Modifer {

    def present(r: Graphic[_], position: Point, tint: RGBA): SignalFunction[Double, AutomatonUpdate] =
      SignalFunction { alpha =>
        AutomatonUpdate(
          r.moveTo(position)
            .modifyMaterial {
              case m: Material.ImageEffects =>
                m.withAlpha(alpha).withTint(tint)

              case m => m
            }
        )
      }

    val signal: SignalReader[(AutomatonSeedValues, SceneNode), AutomatonUpdate] =
      SignalReader {
        case (sa, n) =>
          (sa.payload, n) match {
            case (Some(TrailParticle(_, t)), g: Graphic[_]) =>
              TrailParticle.fade(sa.lifeSpan) |> present(g, sa.spawnedAt, t)

            case _ =>
              Signal.fixed(AutomatonUpdate.empty)
          }
      }

  }

}
