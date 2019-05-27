package ingidoexamples.model

import indigo._
import indigoexts.subsystems.automata.AutomatonPayload
import ingidoexamples.automata.LaunchPadAutomaton

final case class LaunchPad(position: Point, length: Millis, rocket: Rocket) extends AutomatonPayload

object LaunchPad {

  def generateLaunchPad(dice: Dice, min: Point, max: Point, screenDimensions: Rectangle): LaunchPad = {
    val startPosition: Point =
      Point(min.x + dice.roll(max.x - min.x), min.y)

    LaunchPad(
      startPosition,
      Millis(dice.roll(LaunchPadAutomaton.MaxCountDown - 500).toLong + 500L),
      Rocket.generateRocket(dice, startPosition, screenDimensions)
    )
  }

}
