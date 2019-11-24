package ingidoexamples.automata

import indigoexts.subsystems.automata._
import ingidoexamples.model.TrailParticle
import indigo._
import ingidoexamples.Assets

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

  def spawnEvent(at: Point): AutomataEvent.Spawn =
    AutomataEvent.Spawn(
      poolKey,
      at,
      None,
      Some(TrailParticle.create)
    )

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
