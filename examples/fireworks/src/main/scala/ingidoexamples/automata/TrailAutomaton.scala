package ingidoexamples.automata

import indigoexts.subsystems.automata._
import ingidoexamples.model.TrailParticle
import indigo.shared.datatypes.Point
import indigo.shared.time.Millis
import ingidoexamples.Assets
import indigo.shared.scenegraph.Renderable
import indigo.shared.temporal.Signal
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.temporal.SignalFunction

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
      poolKey,
      Assets.cross,
      Millis(1000)
    ).withModifier(Modifer.signal)

  object Modifer {

    def present(r: Renderable, position: Point): SignalFunction[TrailParticle, SceneUpdateFragment] =
      SignalFunction { tp =>
        SceneUpdateFragment.empty.addGameLayerNodes(
          r.moveTo(position + Point(0, (30 * tp.fallen).toInt))
            .withAlpha(tp.alpha)
        )
      }

    val signal: (AutomatonSeedValues, Renderable) => Signal[SceneUpdateFragment] =
      (sa, r) => TrailParticle.particle(sa.lifeSpan) |> present(r, sa.spawnedAt)

  }

}
