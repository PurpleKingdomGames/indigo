'use strict';
class View {

    // STEP 2
    static drawBackground() {
        return SceneUpdateFragmentHelper.empty
            .addGameLayerNodes([Graphic(
                Rectangle(0, 0, 640, 360),
                50,
                0,
                1,
                1,
                Assets.Static.backgroundRef,
                new Point(0, 0),
                Rectangle(0, 0, 640, 360),
                EffectsHelper.None
            )])
    }

    // STEP 3
    static sceneAudio() {
        return SceneUpdateFragmentHelper.empty
            .withAudio(
                SceneAudio(
                    SceneAudioSource(
                        Assets.Sounds.shanty,
                        SingleTrackLoop(
                            Track(Assets.Sounds.shanty)
                        )
                    )
                )
            )
        ;
    }

    // STEP 4
    static drawWater(viewModel) {
        SceneUpdateFragmentHelper.empty
            .addGameLayerNodes(
                viewModel.waterReflections.play(),
                viewModel.waterReflections.moveBy(150, 30).play(),
                viewModel.waterReflections.moveBy(-100, 60).play()
            )
        ;
    }

    // STEP 7
    static drawForeground(viewModel, screenDimensions) {
        return SceneUpdateFragmentHelper.empty
            .addGameLayerNodes(
                Text("The Cursed Pirate\n@davidjamessmith", 0, 0, 5, Assets.Fonts.fontKey)
                    .alignRight
                    .withAlpha(0.5)
                    .moveTo(screenDimensions.right - 5, screenDimensions.bottom - 30),
                viewModel.flag.play(),
                viewModel.helm.play(),
                Assets.Trees.tallTrunkGraphic.moveTo(420, 220),
                Assets.Trees.leftLeaningTrunkGraphic.moveTo(100, 270),
                Assets.Trees.rightLeaningTrunkGraphic.moveTo(25, 150),
                viewModel.backTallPalm.moveTo(420, 210).changeCycle(CycleLabel("P Back")).play(),
                viewModel.palm.moveTo(397, 188).play(),
                viewModel.palm.moveTo(77, 235).play(),
                viewModel.palm.moveTo(37, 104).play(),
                Assets.Static.chestGraphic.moveTo(380, 271),
                Assets.Static.levelGraphic
            )
        ;
    }

    // STEP 8
    static drawPirate(model, captain) {
        return SceneUpdateFragmentHelper.empty
            .addGameLayerNodes(updatedCaptain(model, captain))
        ;
    }

    // STEP 9
    static drawPirateWithRespawn(gameTime, model, captain) {
        return SceneUpdateFragmentHelper.empty
            .addGameLayerNodes(respawnEffect(gameTime, model, updatedCaptain(model, captain)))
        ;
    }

    static respawnEffect(gameTime, model, captain) {
        const flashActive =
            Signal(_ < model.lastRespawn + 2000)
        ;

        const flashOnOff =
            Signal.Pulse(100)
        ;

        const combinedSignals =
            flashActive.merge(flashOnOff)
        ;

        const captainWithAlpha =
            new SignalFunction(function(a, b) {
                if (!a)
                    return captain;
                else if (a && b)
                    return captain.withAlpha(1);
                else
                    return captain.withAlpha(0);
            })
        ;

        const signal = combinedSignals.merge(captainWithAlpha)
        signal.at(gameTime.running)
    }

    static updatedCaptain(model, captain) {
        switch (model.pirateState) {
            case PirateState.Idle:
                if (model.facingRight)
                    return captain
                        .moveTo(model.position)
                        .changeCycle(CycleLabel("Idle"))
                        .play()
                    ;
                else
                    captain
                        .moveTo(model.position)
                        .flipHorizontal(true)
                        .moveBy(-20, 0)
                        .changeCycle(CycleLabel("Idle"))
                        .play()
                    ;

            case PirateState.MoveLeft:
                return captain
                    .moveTo(model.position)
                    .flipHorizontal(true)
                    .moveBy(-20, 0)
                    .changeCycle(CycleLabel("Run"))
                    .play()
                ;

            case PirateState.MoveRight:
                return captain
                    .moveTo(model.position)
                    .changeCycle(CycleLabel("Run"))
                    .play()
                ;

            case PirateState.Falling:
                if (model.facingRight)
                    return captain
                        .moveTo(model.position)
                        .changeCycle(CycleLabel("Fall"))
                        .play()
                    ;
                else
                    return captain
                        .moveTo(model.position)
                        .flipHorizontal(true)
                        .moveBy(-20, 0)
                        .changeCycle(CycleLabel("Fall"))
                        .play()
                    ;
        }
    }
}
