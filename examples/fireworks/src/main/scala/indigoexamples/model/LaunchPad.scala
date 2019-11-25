package indigoexamples.model

import indigo._
import indigoexts.subsystems.automata.AutomatonPayload
import indigoexamples.automata.LaunchPadAutomata
import indigoexts.geometry.Vertex

final case class LaunchPad(position: Vertex, countDown: Millis, rocket: Rocket) extends AutomatonPayload

object LaunchPad {

  def generateLaunchPad(dice: Dice): LaunchPad = {
    val startPosition: Vertex =
      Vertex((dice.rollDouble * 2) - 1.0d, 0)

    val countDown: Millis =
      Millis(LaunchPadAutomata.MinCountDown + dice.roll(LaunchPadAutomata.MaxCountDown - LaunchPadAutomata.MinCountDown).toLong)

    LaunchPad(startPosition, countDown, Rocket.generateRocket(dice, startPosition))
  }

}
