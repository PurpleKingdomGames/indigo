package pirate.core

import pirate.scenes.loading.LoadingModel
import pirate.scenes.level.model.LevelModel

// A simple master model class to hold the sub-models for
// each scene. Slightly over-egged, LoadingModel only holds
// LoadingState and so is a bit superfluous, but it's here
// for clarity.
final case class Model(
    loadingScene: LoadingModel,
    gameScene: LevelModel
)
object Model:

  // It's a good idea to have as far requirements as possible on the
  // initial versions of the model and view model.
  def initial: Model =
    Model(
      LoadingModel.initial,
      LevelModel.NotReady
    )
