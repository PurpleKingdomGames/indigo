package ingidoexamples.model

import indigo._
import indigoexts.uicomponents._
import ingidoexamples.automata.FuseAutomaton

final case class FireworksModel(launchButton: Button) {
  def update(dice: Dice, min: Point, max: Point): Outcome[FireworksModel] =
    Outcome(
      FireworksModel.update(this, dice, min, max)
    )
}

object FireworksModel {

  def initialModel: FireworksModel =
    FireworksModel(Button.default)

  def launchFireworks(dice: Dice, min: Point, max: Point): () => List[GlobalEvent] =
    () => List.fill(dice.roll(5))(FuseAutomaton.spawnEvent(Fuse.generateFuse(dice, min, max)))

  def update(state: FireworksModel, dice: Dice, min: Point, max: Point): FireworksModel =
    state.copy(launchButton = state.launchButton.withUpAction(launchFireworks(dice, min, max)))

}
