package ingidoexamples.model

import indigo._
import indigoexts.subsystems.automata.AutomatonPayload
import ingidoexamples.automata.LaunchPadAutomaton
import indigoexts.geometry.Vertex

final case class LaunchPad(position: Vertex, countDown: Millis, rocket: Rocket) extends AutomatonPayload

object LaunchPad {

  def generateLaunchPad(dice: Dice): LaunchPad = {
    val startPosition: Vertex =
      Vertex(dice.rollDouble, 0)

    LaunchPad(
      startPosition,
      Millis(dice.roll(LaunchPadAutomaton.MaxCountDown - LaunchPadAutomaton.MinCountDown).toLong + LaunchPadAutomaton.MinCountDown),
      Rocket.generateRocket(dice)
    )
  }

}
