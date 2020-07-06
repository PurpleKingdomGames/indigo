package indigo.scenes

import indigo.shared.Outcome
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.EqualTo
import indigo.shared.subsystems.SubSystem
import indigo.shared.FrameContext

trait Scene[StartUpData, GameModel, ViewModel] {
  type SceneModel
  type SceneViewModel

  val name: SceneName
  val sceneModelLens: Lens[GameModel, SceneModel]
  val sceneViewModelLens: Lens[ViewModel, SceneViewModel]

  val sceneSubSystems: Set[SubSystem]

  def updateSceneModel(context: FrameContext[StartUpData], sceneModel: SceneModel): GlobalEvent => Outcome[SceneModel]
  def updateSceneViewModel(context: FrameContext[StartUpData], sceneModel: SceneModel, sceneViewModel: SceneViewModel): GlobalEvent => Outcome[SceneViewModel]
  def updateSceneView(context: FrameContext[StartUpData], sceneModel: SceneModel, sceneViewModel: SceneViewModel): SceneUpdateFragment

}
object Scene {

  def updateModel[SD, GM, VM](scene: Scene[SD, GM, VM], context: FrameContext[SD], gameModel: GM): GlobalEvent => Outcome[GM] =
    e =>
      scene
        .updateSceneModel(context, scene.sceneModelLens.get(gameModel))(e)
        .mapState(scene.sceneModelLens.set(gameModel, _))

  def updateViewModel[SD, GM, VM](scene: Scene[SD, GM, VM], context: FrameContext[SD], model: GM, viewModel: VM): GlobalEvent => Outcome[VM] =
    e =>
      scene
        .updateSceneViewModel(context, scene.sceneModelLens.get(model), scene.sceneViewModelLens.get(viewModel))(e)
        .mapState(scene.sceneViewModelLens.set(viewModel, _))

  def updateView[SD, GM, VM](scene: Scene[SD, GM, VM], context: FrameContext[SD], model: GM, viewModel: VM): SceneUpdateFragment =
    scene.updateSceneView(context, scene.sceneModelLens.get(model), scene.sceneViewModelLens.get(viewModel))

}

final class SceneName(val name: String) extends AnyVal
object SceneName {

  def apply(name: String): SceneName =
    new SceneName(name)

  implicit val EqSceneName: EqualTo[SceneName] = {
    val eq = implicitly[EqualTo[String]]
    EqualTo.create { (a, b) =>
      eq.equal(a.name, b.name)
    }
  }

}
