package indigoexamples.automata

import indigoexts.subsystems.automata._
import indigoexamples.model.TrailParticle
import indigo._
import indigoexamples.Assets

object TrailAutomata {

  val poolKey: AutomataPoolKey =
    AutomataPoolKey("trail")

  val automaton: Automaton =
    Automaton(
      Assets.cross,
      Millis(250)
    ).withModifier(Modifer.signal)

  val automata: Automata =
    Automata(poolKey, automaton, Automata.Layer.Game)
      .withMaxPoolSize(500)

  def spawnEvent(at: Point, tint: Tint): AutomataEvent.Spawn =
    AutomataEvent.Spawn(
      poolKey,
      at,
      None,
      Some(TrailParticle.create(tint))
    )

  object Modifer {

    def present(r: Graphic, position: Point, tint: Tint): SignalFunction[Double, AutomatonUpdate] =
      SignalFunction { alpha =>
        AutomatonUpdate.withNodes(
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
