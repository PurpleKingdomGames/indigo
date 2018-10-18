package indigoexts.scenemanager

import indigo.{UpdatedModel, UpdatedViewModel}
import indigo.gameengine.GameTime
import indigo.gameengine.events.{FrameInputEvents, GameEvent}
import indigo.gameengine.scenegraph.SceneUpdateFragment
import indigoexts.lenses.Lens

trait Scene[GameModel, ViewModel, SceneModel, SceneViewModel] {

  val name: SceneName

  val sceneModelLens: Lens[GameModel, SceneModel]
  val sceneViewModelLens: Lens[ViewModel, SceneViewModel]

  def updateSceneModel(gameTime: GameTime, sceneModel: SceneModel): GameEvent => UpdatedModel[SceneModel]

  def updateSceneViewModel(gameTime: GameTime, sceneModel: SceneModel, sceneViewModel: SceneViewModel, frameInputEvents: FrameInputEvents): UpdatedViewModel[SceneViewModel]

  def updateSceneView(gameTime: GameTime, sceneModel: SceneModel, sceneViewModel: SceneViewModel, frameInputEvents: FrameInputEvents): SceneUpdateFragment

  def updateModelDelegate(gameTime: GameTime, gameModel: GameModel): GameEvent => UpdatedModel[GameModel] =
    e => {
      val next = updateSceneModel(gameTime, sceneModelLens.get(gameModel))(e)
      UpdatedModel(
        sceneModelLens.set(gameModel, next.model),
        next.events
      )
    }

  def updateViewModelDelegate(gameTime: GameTime, model: GameModel, viewModel: ViewModel, frameInputEvents: FrameInputEvents): UpdatedViewModel[ViewModel] = {
    val next = updateSceneViewModel(gameTime, sceneModelLens.get(model), sceneViewModelLens.get(viewModel), frameInputEvents)
    UpdatedViewModel(
      sceneViewModelLens.set(
        viewModel,
        next.model
      ),
      next.events
    )
  }

  def updateViewDelegate(gameTime: GameTime, model: GameModel, viewModel: ViewModel, frameInputEvents: FrameInputEvents): SceneUpdateFragment =
    updateSceneView(gameTime, sceneModelLens.get(model), sceneViewModelLens.get(viewModel), frameInputEvents)

}

case class SceneName(name: String) extends AnyVal {
  def ===(other: SceneName): Boolean =
    name == other.name
}
