package ingidoexamples.model

import indigo._
import indigoexts.subsystems.automata.AutomatonPayload
import ingidoexamples.automata.LaunchPadAutomaton

final case class LaunchPad(position: Point, length: Millis, rocket: Rocket) extends AutomatonPayload

object LaunchPad {

  def generateLaunchPad(dice: Dice, min: Point, max: Point): LaunchPad = {
    val position: Point =
      Point(min.x + dice.roll(max.x - min.x), min.y)

    val rocket: Rocket =
      Rocket(
        position,
        Rocket.createRocketArcSignal(
          dice,
          position,
          position - Point(0, 100) - Point(dice.roll(200) - 100, dice.roll(100)),
          Millis(((dice.roll(20) + 10) * 100).toLong) // between 1 and 3 seconds...
        )
      )

    LaunchPad(
      position,
      Millis(dice.roll(LaunchPadAutomaton.MaxCountDown - 500).toLong + 500L),
      rocket
    )
  }

}
