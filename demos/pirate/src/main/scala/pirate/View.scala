package pirate

import indigo._

object View {

  def present(gameTime: GameTime, model: Model, viewModel: ViewModel, screenDimensions: Rectangle): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addGameLayerNodes(Assets.backgroundGraphic)
      .addGameLayerNodes(
        Text("The Cursed Pirate\n@davidjamessmith", 0, 0, 5, Assets.fontKey).alignRight
          .withAlpha(0.5d)
          .moveTo(screenDimensions.right - 5, screenDimensions.bottom - 30),
        viewModel.waterReflections.play(),
        viewModel.waterReflections.moveBy(150, 30).play(),
        viewModel.waterReflections.moveBy(-100, 60).play(),
        viewModel.flag.play(),
        viewModel.helm.play(),
        Assets.Trees.tallTrunkGraphic.moveTo(420, 220),
        Assets.Trees.leftLeaningTrunkGraphic.moveTo(100, 270),
        Assets.Trees.rightLeaningTrunkGraphic.moveTo(25, 150),
        viewModel.backTallPalm.moveTo(420, 210).changeCycle(CycleLabel("P Back")).play(),
        viewModel.frontPalm.moveTo(397, 188).play(),
        viewModel.frontPalm.moveTo(77, 235).play(),
        viewModel.frontPalm.moveTo(37, 104).play(),
        Assets.chestGraphic.moveTo(380, 271),
        Assets.levelGraphic,
        drawPirate(gameTime, model, viewModel.captain)
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

  def drawPirate(gameTime: GameTime, model: Model, captain: Sprite): Sprite = {
    val base = model.pirateState match {
      case PirateState.Idle =>
        captain
          .moveTo(model.position)
          .changeCycle(CycleLabel("Idle"))
          .play()

      case PirateState.MoveLeft =>
        captain
          .moveTo(model.position)
          .flipHorizontal(true)
          .moveBy(-20, 0)
          .changeCycle(CycleLabel("Run"))
          .play()

      case PirateState.MoveRight =>
        captain
          .moveTo(model.position)
          .changeCycle(CycleLabel("Run"))
          .play()

      case PirateState.Falling =>
        captain
          .moveTo(model.position)
          .changeCycle(CycleLabel("Fall"))
          .play()
    }

    val flashActive: Signal[Boolean] =
      Signal(_ < model.lastRespawn + Millis(2000))

    val flashing: Signal[Boolean] =
      Signal.Pulse(Millis(100))

    val signal = flashActive |*| flashing |> SignalFunction {
      case (false, _)    => base
      case (true, true)  => base.withAlpha(1)
      case (true, false) => base.withAlpha(0)
    }

    signal.at(gameTime.running)
  }

}
