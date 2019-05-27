package ingidoexamples.model

import indigo._
import indigoexts.uicomponents._
import indigoexts.subsystems.automata._
import ingidoexamples.automata.LaunchPadAutomaton

final case class FireworksModel(launchButton: Button) {
  def update(dice: Dice, viewportSize: Point): Outcome[FireworksModel] =
    Outcome(
      FireworksModel.update(this, dice, viewportSize)
    )
}

object FireworksModel {

  def initialModel: FireworksModel =
    FireworksModel(Button.default)

  def launchFireworks(dice: Dice, viewportSize: Point): () => List[AutomataEvent.Spawn] = {
    // Only launch from the central third of the baseline.
    val diff = viewportSize.x / 3
    val p1   = Point(diff, viewportSize.y - 5)
    val p2   = Point(diff + diff, viewportSize.y - 5)

    val events =
      List.fill(dice.roll(5))(LaunchPadAutomaton.spawnEvent(LaunchPad.generateLaunchPad(dice, p1, p2)))

    () => events
  }

  def update(state: FireworksModel, dice: Dice, viewportSize: Point): FireworksModel =
    state.copy(launchButton = state.launchButton.withUpAction(launchFireworks(dice, viewportSize)))

}
