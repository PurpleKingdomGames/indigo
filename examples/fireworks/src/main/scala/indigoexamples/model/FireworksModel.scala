package indigoexamples.model

import indigo._
import indigoexts.uicomponents._
import indigoexts.subsystems.automata._
import indigoexamples.automata.LaunchPadAutomata
import indigoexts.geometry.Vertex

final case class FireworksModel(launchButton: Button) {
  def update(dice: Dice, toScreenSpace: Vertex => Point): Outcome[FireworksModel] =
    Outcome(
      FireworksModel.update(this, dice, toScreenSpace)
    )
}

object FireworksModel {

  def initialModel: FireworksModel =
    FireworksModel(Button.default)

  def update(state: FireworksModel, dice: Dice, toScreenSpace: Vertex => Point): FireworksModel =
    state.copy(launchButton = state.launchButton.withUpAction(launchFireworks(dice, toScreenSpace)))

  def launchFireworks(dice: Dice, toScreenSpace: Vertex => Point): () => List[AutomataEvent.Spawn] =
    () =>
      List.fill(dice.roll(5) + 5)(
        LaunchPadAutomata.spawnEvent(
          LaunchPad.generateLaunchPad(dice),
          toScreenSpace
        )
      )

}
