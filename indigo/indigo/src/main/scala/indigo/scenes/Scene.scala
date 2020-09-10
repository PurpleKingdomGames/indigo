package indigo.scenes

import indigo.shared.Outcome
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.EqualTo
import indigo.shared.subsystems.SubSystem
import indigo.shared.FrameContext
import indigo.shared.events.EventFilters

trait Scene[StartUpData, GameModel, ViewModel] {
  type SceneModel
  type SceneViewModel

  def name: SceneName
  def modelLens: Lens[GameModel, SceneModel]
  def viewModelLens: Lens[ViewModel, SceneViewModel]
  def eventFilters: EventFilters
  def subSystems: Set[SubSystem]

  def updateModel(context: FrameContext[StartUpData], model: SceneModel): GlobalEvent => Outcome[SceneModel]
  def updateViewModel(context: FrameContext[StartUpData], model: SceneModel, viewModel: SceneViewModel): GlobalEvent => Outcome[SceneViewModel]
  def present(context: FrameContext[StartUpData], model: SceneModel, viewModel: SceneViewModel): SceneUpdateFragment
}
object Scene {

  def updateModel[SD, GM, VM](scene: Scene[SD, GM, VM], context: FrameContext[SD], gameModel: GM): GlobalEvent => Outcome[GM] =
    e =>
      scene
        .updateModel(context, scene.modelLens.get(gameModel))(e)
        .mapState(scene.modelLens.set(gameModel, _))

  def updateViewModel[SD, GM, VM](scene: Scene[SD, GM, VM], context: FrameContext[SD], model: GM, viewModel: VM): GlobalEvent => Outcome[VM] =
    e =>
      scene
        .updateViewModel(context, scene.modelLens.get(model), scene.viewModelLens.get(viewModel))(e)
        .mapState(scene.viewModelLens.set(viewModel, _))

  def updateView[SD, GM, VM](scene: Scene[SD, GM, VM], context: FrameContext[SD], model: GM, viewModel: VM): SceneUpdateFragment =
    scene.present(context, scene.modelLens.get(model), scene.viewModelLens.get(viewModel))

}

final case class SceneName(name: String) extends AnyVal
object SceneName {

  implicit val EqSceneName: EqualTo[SceneName] = {
    val eq = implicitly[EqualTo[String]]
    EqualTo.create { (a, b) =>
      eq.equal(a.name, b.name)
    }
  }

}
