package indigoexts.scenemanager

import indigo.gameengine.Outcome
import indigo.time.GameTime
import indigo.gameengine.events.{FrameInputEvents, GlobalEvent}
import indigo.gameengine.scenegraph.SceneUpdateFragment
import indigo.shared.EqualTo
import indigoexts.lenses.Lens

import indigo.shared.EqualTo._

trait Scene[GameModel, ViewModel] {
  type SceneModel
  type SceneViewModel

  val name: SceneName
  val sceneModelLens: Lens[GameModel, SceneModel]
  val sceneViewModelLens: Lens[ViewModel, SceneViewModel]

  def updateSceneModel(gameTime: GameTime, sceneModel: SceneModel): GlobalEvent => Outcome[SceneModel]
  def updateSceneViewModel(gameTime: GameTime, sceneModel: SceneModel, sceneViewModel: SceneViewModel, frameInputEvents: FrameInputEvents): Outcome[SceneViewModel]
  def updateSceneView(gameTime: GameTime, sceneModel: SceneModel, sceneViewModel: SceneViewModel, frameInputEvents: FrameInputEvents): SceneUpdateFragment

}
object Scene {

  def updateModel[GM, VM](scene: Scene[GM, VM], gameTime: GameTime, gameModel: GM): GlobalEvent => Outcome[GM] =
    e =>
      scene
        .updateSceneModel(gameTime, scene.sceneModelLens.get(gameModel))(e)
        .mapState(scene.sceneModelLens.set(gameModel, _))

  def updateViewModel[GM, VM](scene: Scene[GM, VM], gameTime: GameTime, model: GM, viewModel: VM, frameInputEvents: FrameInputEvents): Outcome[VM] =
    scene
      .updateSceneViewModel(gameTime, scene.sceneModelLens.get(model), scene.sceneViewModelLens.get(viewModel), frameInputEvents)
      .mapState(scene.sceneViewModelLens.set(viewModel, _))

  def updateView[GM, VM](scene: Scene[GM, VM], gameTime: GameTime, model: GM, viewModel: VM, frameInputEvents: FrameInputEvents): SceneUpdateFragment =
    scene.updateSceneView(gameTime, scene.sceneModelLens.get(model), scene.sceneViewModelLens.get(viewModel), frameInputEvents)

}

final class SceneName(val name: String) extends AnyVal
object SceneName {

  def apply(name: String): SceneName =
    new SceneName(name)

  implicit val EqSceneName: EqualTo[SceneName] =
    EqualTo.create { (a, b) =>
      a === b
    }

}
