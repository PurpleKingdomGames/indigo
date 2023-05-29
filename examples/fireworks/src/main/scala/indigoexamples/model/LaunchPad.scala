package indigoexamples.model

import indigo.*
import indigoexamples.automata.LaunchPadAutomata
import indigoextras.subsystems.AutomatonPayload

final case class LaunchPad(position: Vertex, countDown: Seconds, rocket: Rocket) extends AutomatonPayload

object LaunchPad {

  def generateLaunchPad(dice: Dice): LaunchPad = {
    val startPosition: Vertex =
      Vertex((dice.rollDouble * 2) - 1.0d, 0)

    val diff: Seconds =
      Millis(
        dice
          .roll(
            (LaunchPadAutomata.MaxCountDown - LaunchPadAutomata.MinCountDown).toMillis.toInt
          )
          .toLong
      ).toSeconds

    val countDown: Seconds =
      LaunchPadAutomata.MinCountDown + diff

    LaunchPad(startPosition, countDown, Rocket.generateRocket(dice, startPosition))
  }

}
