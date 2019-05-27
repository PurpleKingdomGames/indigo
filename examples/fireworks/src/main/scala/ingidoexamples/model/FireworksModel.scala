package ingidoexamples.model

import indigo._
import indigoexts.uicomponents._
import indigoexts.subsystems.automata._
import ingidoexamples.automata.LaunchPadAutomaton

final case class FireworksModel(launchButton: Button) {
  def update(dice: Dice, screenDimensions: Rectangle): Outcome[FireworksModel] =
    Outcome(
      FireworksModel.update(this, dice, screenDimensions)
    )
}

object FireworksModel {

  def initialModel: FireworksModel =
    FireworksModel(Button.default)

  def launchFireworks(dice: Dice, screenDimensions: Rectangle): () => List[AutomataEvent.Spawn] = {
    // Only launch from the central third of the baseline.
    val diff = screenDimensions.width / 3
    val p1   = Point(diff, screenDimensions.height - 5)
    val p2   = Point(diff + diff, screenDimensions.height - 5)

    val events =
      List.fill(dice.roll(5))(LaunchPadAutomaton.spawnEvent(LaunchPad.generateLaunchPad(dice, p1, p2, screenDimensions)))

    () => events
  }

  def update(state: FireworksModel, dice: Dice, screenDimensions: Rectangle): FireworksModel =
    state.copy(launchButton = state.launchButton.withUpAction(launchFireworks(dice, screenDimensions)))

}
