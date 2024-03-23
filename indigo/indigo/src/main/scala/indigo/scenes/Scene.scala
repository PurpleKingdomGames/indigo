package indigo.scenes

import indigo.*
import indigo.shared.Outcome
import indigo.shared.collections.Batch
import indigo.shared.events.EventFilters
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.subsystems.SubSystem

/** Describes the functions that a valid scene must implement.
  */
trait Scene[StartUpData, GameModel, ViewModel] derives CanEqual {
  type SceneModel
  type SceneViewModel

  def name: SceneName
  def modelLens: Lens[GameModel, SceneModel]
  def viewModelLens: Lens[ViewModel, SceneViewModel]
  def eventFilters: EventFilters
  def subSystems: Set[SubSystem[GameModel]]

  def updateModel(context: SceneContext[StartUpData], model: SceneModel): GlobalEvent => Outcome[SceneModel]
  def updateViewModel(
      context: SceneContext[StartUpData],
      model: SceneModel,
      viewModel: SceneViewModel
  ): GlobalEvent => Outcome[SceneViewModel]
  def present(
      context: SceneContext[StartUpData],
      model: SceneModel,
      viewModel: SceneViewModel
  ): Outcome[SceneUpdateFragment]
}
object Scene {

  def updateModel[SD, GM, VM](
      scene: Scene[SD, GM, VM],
      context: SceneContext[SD],
      gameModel: GM
  ): GlobalEvent => Outcome[GM] =
    e =>
      scene
        .updateModel(context, scene.modelLens.get(gameModel))(e)
        .map(scene.modelLens.set(gameModel, _))

  def updateViewModel[SD, GM, VM](
      scene: Scene[SD, GM, VM],
      context: SceneContext[SD],
      model: GM,
      viewModel: VM
  ): GlobalEvent => Outcome[VM] =
    e =>
      scene
        .updateViewModel(context, scene.modelLens.get(model), scene.viewModelLens.get(viewModel))(e)
        .map(scene.viewModelLens.set(viewModel, _))

  def updateView[SD, GM, VM](
      scene: Scene[SD, GM, VM],
      context: SceneContext[SD],
      model: GM,
      viewModel: VM
  ): Outcome[SceneUpdateFragment] =
    scene.present(context, scene.modelLens.get(model), scene.viewModelLens.get(viewModel))

  def empty[SD, GM, VM]: Scene[SD, GM, VM] =
    new Scene[SD, GM, VM] {
      type SceneModel     = Unit
      type SceneViewModel = Unit

      val sceneFragment =
        Outcome(SceneUpdateFragment(Batch.empty[Layer]))

      val modelOutcome = Outcome(())

      val name: SceneName =
        SceneName("empty-scene")

      val modelLens: Lens[GM, Unit] =
        Lens.unit

      val viewModelLens: Lens[VM, Unit] =
        Lens.unit

      val eventFilters: EventFilters =
        EventFilters.BlockAll

      val subSystems: Set[SubSystem[GM]] =
        Set()

      def updateModel(
          context: SceneContext[SD],
          model: Unit
      ): GlobalEvent => Outcome[Unit] =
        _ => modelOutcome

      def updateViewModel(
          context: SceneContext[SD],
          model: Unit,
          viewModel: Unit
      ): GlobalEvent => Outcome[Unit] =
        _ => modelOutcome

      def present(
          context: SceneContext[SD],
          model: Unit,
          viewModel: Unit
      ): Outcome[SceneUpdateFragment] = sceneFragment
    }
}

opaque type SceneName = String
object SceneName:
  inline def apply(sceneName: String): SceneName        = sceneName
  extension (sn: SceneName) inline def toString: String = sn
  given CanEqual[SceneName, SceneName]                  = CanEqual.derived
  given CanEqual[Option[SceneName], Option[SceneName]]  = CanEqual.derived
