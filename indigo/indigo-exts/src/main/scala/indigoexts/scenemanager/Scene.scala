package indigoexts.scenemanager

import indigo.shared.Outcome
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.EqualTo
import indigoexts.lenses.Lens
import indigoexts.subsystems.SubSystem
import indigo.shared.FrameContext

trait Scene[GameModel, ViewModel] {
  type SceneModel
  type SceneViewModel

  val name: SceneName
  val sceneModelLens: Lens[GameModel, SceneModel]
  val sceneViewModelLens: Lens[ViewModel, SceneViewModel]

  val sceneSubSystems: Set[SubSystem]

  def updateSceneModel(context: FrameContext, sceneModel: SceneModel): GlobalEvent => Outcome[SceneModel]
  def updateSceneViewModel(context: FrameContext, sceneModel: SceneModel, sceneViewModel: SceneViewModel): Outcome[SceneViewModel]
  def updateSceneView(context: FrameContext, sceneModel: SceneModel, sceneViewModel: SceneViewModel): SceneUpdateFragment

}
object Scene {

  def updateModel[GM, VM](scene: Scene[GM, VM], context: FrameContext, gameModel: GM): GlobalEvent => Outcome[GM] =
    e =>
      scene
        .updateSceneModel(context, scene.sceneModelLens.get(gameModel))(e)
        .mapState(scene.sceneModelLens.set(gameModel, _))

  def updateViewModel[GM, VM](scene: Scene[GM, VM], context: FrameContext, model: GM, viewModel: VM): Outcome[VM] =
    scene
      .updateSceneViewModel(context, scene.sceneModelLens.get(model), scene.sceneViewModelLens.get(viewModel))
      .mapState(scene.sceneViewModelLens.set(viewModel, _))

  def updateView[GM, VM](scene: Scene[GM, VM], context: FrameContext, model: GM, viewModel: VM): SceneUpdateFragment =
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
