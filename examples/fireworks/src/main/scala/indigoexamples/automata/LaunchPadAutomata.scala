package indigoexamples.automata

import indigo._
import indigoexts.subsystems.automata._
import indigoexamples.Assets
import indigoexamples.model.LaunchPad
import indigoexts.geometry.Vertex

object LaunchPadAutomata {

  val MinCountDown: Int = 100
  val MaxCountDown: Int = 1000

  val poolKey: AutomataPoolKey =
    AutomataPoolKey("launchPad")

  val automaton: Automaton =
    Automaton(
      Assets.cross,
      Millis(0)
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
