package ingidoexamples.model

import indigo._
import indigoexts.subsystems.automata.AutomatonPayload
import ingidoexamples.automata.LaunchPadAutomaton

final case class LaunchPad(position: Vector2, countDown: Millis, rocket: Rocket) extends AutomatonPayload

object LaunchPad {

  def generateLaunchPad(dice: Dice, min: Point, max: Point, screenDimensions: Rectangle): LaunchPad = {
    val startPosition: Vector2 =
      Vector2(dice.rollDouble, 0)

    LaunchPad(
      startPosition,
      Millis(dice.roll(LaunchPadAutomaton.MaxCountDown - LaunchPadAutomaton.MinCountDown).toLong + LaunchPadAutomaton.MinCountDown),
      Rocket.generateRocket(dice, startPosition, screenDimensions)
    )
  }

}
