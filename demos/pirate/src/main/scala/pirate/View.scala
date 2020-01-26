package pirate

import indigo._

object View {

  def present(viewModel: ViewModel, pirateState: PirateState): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addGameLayerNodes(Assets.backgroundGraphic)
      .addGameLayerNodes(
        viewModel.waterReflections.play(),
        viewModel.waterReflections.moveBy(150, 30).play(),
        viewModel.waterReflections.moveBy(-100, 60).play(),
        viewModel.flag.play(),
        Assets.levelGraphic,
        drawPirate(viewModel.captain, pirateState)
      )
      .withAudio(
        SceneAudio(
          SceneAudioSource(
            BindingKey(Assets.shanty),
            PlaybackPattern.SingleTrackLoop(
              Track(Assets.shanty)
            )
          )
        )
      )

  def drawPirate(captain: Sprite, pirateState: PirateState): Sprite =
    pirateState match {
      case PirateState.Idle =>
        captain
          .changeCycle(CycleLabel("Idle"))
          .play()

      case PirateState.Jump =>
        captain
          .changeCycle(CycleLabel("Jump"))
          .play()

      case PirateState.MoveLeft =>
        captain
          .flipHorizontal(true)
          .moveBy(-20, 0)
          .changeCycle(CycleLabel("Run"))
          .play()

      case PirateState.MoveRight =>
        captain
          .changeCycle(CycleLabel("Run"))
          .play()

      case PirateState.Falling =>
        captain
          .changeCycle(CycleLabel("Fall"))
          .play()
    }

}
