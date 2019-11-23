package ingidoexamples.model

import indigo._
import indigoexts.uicomponents._
import indigoexts.subsystems.automata._
import ingidoexamples.automata.LaunchPadAutomata

final case class FireworksModel(launchButton: Button) {
  def update(dice: Dice, screenDimensions: Rectangle): Outcome[FireworksModel] =
    Outcome(
      FireworksModel.update(this, dice, screenDimensions)
    )
}

object FireworksModel {

  def initialModel: FireworksModel =
    FireworksModel(Button.default)

  def update(state: FireworksModel, dice: Dice, screenDimensions: Rectangle): FireworksModel =
    state.copy(launchButton = state.launchButton.withUpAction(launchFireworks(dice, screenDimensions)))

  def launchFireworks(dice: Dice, screenDimensions: Rectangle): () => List[AutomataEvent.Spawn] =
    () =>
      List.fill(dice.roll(5) + 5)(
        LaunchPadAutomata.spawnEvent(
          LaunchPad.generateLaunchPad(dice),
          Projectiles.toScreenSpace(screenDimensions)
        )
      )

}
