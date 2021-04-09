package indigoexamples.automata

import indigo._
import indigoextras.subsystems._
import indigoexamples.Assets
import indigoexamples.model.LaunchPad
import indigoextras.geometry.Vertex

object LaunchPadAutomata {

  val MinCountDown: Seconds = Seconds(0.1)
  val MaxCountDown: Seconds = Seconds(1.0)

  val poolKey: AutomataPoolKey =
    AutomataPoolKey("launchPad")

  val automaton: Automaton =
    Automaton(
      AutomatonNode.Fixed(Assets.cross),
      Seconds.zero,
      (pt: Point, node: SceneNode) =>
        node match {
          case g: Graphic =>
            g.moveTo(pt)

          case _ =>
            node
        }
    ).withOnCullEvent { seed =>
      seed.payload match {
        case Some(LaunchPad(_, _, rocket)) =>
          List(
            RocketAutomata.spawnEvent(rocket, seed.spawnedAt)
          )

        case _ =>
          Nil
      }
    }

  val automata: Automata =
    Automata(poolKey, automaton)

  def spawnEvent(launchPad: LaunchPad, toScreenSpace: Vertex => Point): AutomataEvent.Spawn =
    AutomataEvent.Spawn(
      poolKey,
      toScreenSpace(launchPad.position),
      Some(launchPad.countDown),
      Some(launchPad)
    )

}
