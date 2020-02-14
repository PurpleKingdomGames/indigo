
class PirateDemo extends IndigoGameBasic {
    config =
        GameConfig.default
            .withViewport(GameViewport.at720p)
            .withMagnification(2)
    ;

    assets =
        Assets.assets
    ;

    fonts =
        Set(Assets.Fonts.fontInfo)
    ;

    /*animations =
        Set(
            Assets.Clouds.cloudsAnimation1,
            Assets.Clouds.cloudsAnimation2,
            Assets.Clouds.cloudsAnimation3
        )
    ;

    subSystems =
        Set(
            CloudsAutomata.automata,                            // STEP 6
            CloudsSubSystem.init(config.screenDimensions.width) // STEP 5
        )
    ;

    setup = function(assetCollection) {
        return InitialLoad.setup(assetCollection)
    }*/

    initialModel = function(startupData) {
        return Model.initialModel(config.screenDimensions)
    }

    update = function(gameTime, model, inputState, dice) {
        return function(event) {
            Model.update(gameTime, model, inputState, config.screenDimensions)
        }
    }

    initialViewModel = function (startupData, gameModel) {
        return ViewModel.initialViewModel(startupData, config.screenDimensions)
    }

    updateViewModel = function(gameTime, model, viewModel, inputState, dice) {
        return Outcome(viewModel)
    }

    present = function(gameTime, model, viewModel, inputState) {
            return [
                View.drawBackground,
                //View.sceneAudio,
                //View.drawWater(viewModel),
                //View.drawForeground(viewModel, config.screenDimensions),
                //View.drawPirateWithRespawn(gameTime, model, viewModel.captain) // STEP 9
            ]
        // View.drawBackground |+|
        //   View.sceneAudio |+|
        //   View.drawWater(viewModel) |+|
        //   View.drawForeground(viewModel, config.screenDimensions) |+|
        //   View.drawPirate(model, viewModel.captain) // STEP 8
        // View.drawBackground |+| View.sceneAudio |+| View.drawWater(viewModel) |+|
        //   View.drawForeground(viewModel, config.screenDimensions) // STEP 7
        // View.drawBackground |+| View.sceneAudio |+| View.drawWater(viewModel) // STEP 4
        // View.drawBackground |+| View.sceneAudio // STEP 3
        // View.drawBackground // STEP 2
        // noRender // STEP 1
    }
}