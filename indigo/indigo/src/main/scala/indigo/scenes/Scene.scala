package indigo.scenes

import indigo.shared.Outcome
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneUpdateFragment

import indigo.shared.subsystems.SubSystem
import indigo.shared.FrameContext
import indigo.shared.events.EventFilters

trait Scene[StartUpData, GameModel, ViewModel] derives CanEqual {
  type SceneModel
  type SceneViewModel

  def name: SceneName
  def modelLens: Lens[GameModel, SceneModel]
  def viewModelLens: Lens[ViewModel, SceneViewModel]
  def eventFilters: EventFilters
  def subSystems: Set[SubSystem]

  def updateModel(context: FrameContext[StartUpData], model: SceneModel): GlobalEvent => Outcome[SceneModel]
  def updateViewModel(
      context: FrameContext[StartUpData],
      model: SceneModel,
      viewModel: SceneViewModel
  ): GlobalEvent => Outcome[SceneViewModel]
  def present(
      context: FrameContext[StartUpData],
      model: SceneModel,
      viewModel: SceneViewModel
  ): Outcome[SceneUpdateFragment]
}
object Scene {

  def updateModel[SD, GM, VM](
      scene: Scene[SD, GM, VM],
      context: FrameContext[SD],
      gameModel: GM
  ): GlobalEvent => Outcome[GM] =
    e =>
      scene
        .updateModel(context, scene.modelLens.get(gameModel))(e)
        .map(scene.modelLens.set(gameModel, _))

  def updateViewModel[SD, GM, VM](
      scene: Scene[SD, GM, VM],
      context: FrameContext[SD],
      model: GM,
      viewModel: VM
  ): GlobalEvent => Outcome[VM] =
    e =>
      scene
        .updateViewModel(context, scene.modelLens.get(model), scene.viewModelLens.get(viewModel))(e)
        .map(scene.viewModelLens.set(viewModel, _))

  def updateView[SD, GM, VM](
      scene: Scene[SD, GM, VM],
      context: FrameContext[SD],
      model: GM,
      viewModel: VM
  ): Outcome[SceneUpdateFragment] =
    scene.present(context, scene.modelLens.get(model), scene.viewModelLens.get(viewModel))

}

opaque type SceneName = String
object SceneName:
  def apply(sceneName: String): SceneName = sceneName
  given CanEqual[SceneName, SceneName] = CanEqual.derived
  given CanEqual[Option[SceneName], Option[SceneName]] = CanEqual.derived
