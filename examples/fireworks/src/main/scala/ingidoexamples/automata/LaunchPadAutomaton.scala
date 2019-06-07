package ingidoexamples.automata

import indigo._
import indigoexts.subsystems.automata._
import ingidoexamples.Assets
import ingidoexamples.model.LaunchPad

object LaunchPadAutomaton {

  val MinCountDown: Int = 100
  val MaxCountDown: Int = 750

  val poolKey: AutomataPoolKey =
    AutomataPoolKey("launchPad")

  val automaton: Automaton =
    Automaton(
      poolKey,
      Assets.cross,
      Millis(MaxCountDown.toLong)
    ).withOnCullEvent { seed =>
      seed.payload match {
        case Some(LaunchPad(_, _, rocket)) =>
          Option(
            RocketAutomaton.spawnEvent(rocket, seed.spawnedAt)
          )

        case _ =>
          None
      }

    }

  def spawnEvent(launchPad: LaunchPad, screenDimensions: Rectangle): AutomataEvent.Spawn =
    AutomataEvent.Spawn(
      poolKey,
      Point(
        (((screenDimensions.width / 2).toDouble * launchPad.position.x) + (screenDimensions.width / 4)).toInt,
        screenDimensions.height - 5
      ),
      Some(launchPad.countDown),
      Some(launchPad)
    )

}
