package ingidoexamples.automata

import indigoexts.subsystems.automata._
import ingidoexamples.model.TrailParticle
import indigo._
import ingidoexamples.Assets

object TrailAutomaton {

  val poolKey: AutomataPoolKey =
    AutomataPoolKey("trail")

  def spawnEvent(at: Point): AutomataEvent.Spawn =
    AutomataEvent.Spawn(
      poolKey,
      at,
      None,
      Some(TrailParticle.create)
    )

  val automaton: Automaton =
    Automaton(
      Assets.cross,
      Millis(500)
    ).withModifier(Modifer.signal)

  object Modifer {

    def present(r: Graphic, position: Point): SignalFunction[TrailParticle, AutomatonUpdate] =
      SignalFunction { tp =>
        AutomatonUpdate.withNodes(
          r.moveTo(position)
            .withAlpha(tp.alpha)
            .withTint(Tint.Cyan)
        )
      }

    val signal: (AutomatonSeedValues, SceneGraphNode) => Signal[AutomatonUpdate] =
      (sa, r) =>
        r match {
          case g: Graphic =>
            TrailParticle.particle(sa.lifeSpan) |> present(g, sa.spawnedAt)

          case _ =>
            Signal.fixed(AutomatonUpdate.empty)
        }

  }

}
