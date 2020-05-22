package indigoexamples.automata

import indigoextras.subsystems.automata._
import indigoexamples.model.TrailParticle
import indigo._
import indigoexamples.Assets

object TrailAutomata {

  val poolKey: AutomataPoolKey =
    AutomataPoolKey("trail")

  val automaton: Automaton =
    Automaton(
      Assets.cross,
      Seconds(0.25)
    ).withModifier(Modifer.signal)

  val automata: Automata =
    Automata(poolKey, automaton, Automata.Layer.Game)
      .withMaxPoolSize(500)

  def spawnEvent(at: Point, tint: RGBA): AutomataEvent.Spawn =
    AutomataEvent.Spawn(
      poolKey,
      at,
      None,
      Some(TrailParticle.create(tint))
    )

  object Modifer {

    def present(r: Graphic, position: Point, tint: RGBA): SignalFunction[Double, AutomatonUpdate] =
      SignalFunction { alpha =>
        AutomatonUpdate(
          r.moveTo(position)
            .withAlpha(alpha)
            .withTint(tint)
        )
      }

    val signal: (AutomatonSeedValues, SceneGraphNode) => Signal[AutomatonUpdate] =
      (sa, n) =>
        (sa.payload, n) match {
          case (Some(TrailParticle(_, t)), g: Graphic) =>
            TrailParticle.fade(sa.lifeSpan) |> present(g, sa.spawnedAt, t)

          case _ =>
            Signal.fixed(AutomatonUpdate.empty)
        }

  }

}
