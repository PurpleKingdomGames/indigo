package indigoexts.scenemanager

import indigo.{UpdatedModel, UpdatedViewModel}
import indigo.gameengine.GameTime
import indigo.gameengine.events.{FrameInputEvents, GlobalEvent}
import indigo.gameengine.scenegraph.SceneUpdateFragment
import indigo.shared.Eq
import indigoexts.lenses.Lens

import indigo.Eq._

trait Scene[GameModel, ViewModel, SceneModel, SceneViewModel] {

  val name: SceneName
  val sceneModelLens: Lens[GameModel, SceneModel]
  val sceneViewModelLens: Lens[ViewModel, SceneViewModel]

  def updateSceneModel(gameTime: GameTime, sceneModel: SceneModel): GlobalEvent => UpdatedModel[SceneModel]
  def updateSceneViewModel(gameTime: GameTime, sceneModel: SceneModel, sceneViewModel: SceneViewModel, frameInputEvents: FrameInputEvents): UpdatedViewModel[SceneViewModel]
  def updateSceneView(gameTime: GameTime, sceneModel: SceneModel, sceneViewModel: SceneViewModel, frameInputEvents: FrameInputEvents): SceneUpdateFragment

}
object Scene {

  def updateModel[GM, VM, SModel, SVModel](scene: Scene[GM, VM, SModel, SVModel], gameTime: GameTime, gameModel: GM): GlobalEvent => UpdatedModel[GM] =
    e => {
      val next = scene.updateSceneModel(gameTime, scene.sceneModelLens.get(gameModel))(e)
      UpdatedModel(
        scene.sceneModelLens.set(gameModel, next.model),
        next.globalEvents,
        next.inFrameEvents
      )
    }

  def updateViewModel[GM, VM, SModel, SVModel](scene: Scene[GM, VM, SModel, SVModel], gameTime: GameTime, model: GM, viewModel: VM, frameInputEvents: FrameInputEvents): UpdatedViewModel[VM] = {
    val next = scene.updateSceneViewModel(gameTime, scene.sceneModelLens.get(model), scene.sceneViewModelLens.get(viewModel), frameInputEvents)
    UpdatedViewModel(
      scene.sceneViewModelLens.set(
        viewModel,
        next.model
      ),
      next.globalEvents,
      next.inFrameEvents
    )
  }

  def updateView[GM, VM, SModel, SVModel](scene: Scene[GM, VM, SModel, SVModel], gameTime: GameTime, model: GM, viewModel: VM, frameInputEvents: FrameInputEvents): SceneUpdateFragment =
    scene.updateSceneView(gameTime, scene.sceneModelLens.get(model), scene.sceneViewModelLens.get(viewModel), frameInputEvents)

}

final case class SceneName(name: String) extends AnyVal {
  def ===(other: SceneName): Boolean =
    name === other.name
}
object SceneName {

  implicit val EqSceneName: Eq[SceneName] =
    Eq.create[SceneName] { (a, b) =>
      a === b
    }

}
