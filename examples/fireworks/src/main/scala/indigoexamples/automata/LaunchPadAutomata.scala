package indigoexamples.automata

import indigo._
import indigoextras.subsystems.automata._
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
      Assets.cross,
      Seconds.zero
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
    Automata(poolKey, automaton, Automata.Layer.Game)

  def spawnEvent(launchPad: LaunchPad, toScreenSpace: Vertex => Point): AutomataEvent.Spawn =
    AutomataEvent.Spawn(
      poolKey,
      toScreenSpace(launchPad.position),
      Some(launchPad.countDown),
      Some(launchPad)
    )

}
