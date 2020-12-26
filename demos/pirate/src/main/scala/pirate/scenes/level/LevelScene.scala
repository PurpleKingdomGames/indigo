package pirate.scenes.level

import indigo._
import indigo.scenes._

import pirate.scenes.level.subsystems.CloudsAutomata
import pirate.scenes.level.subsystems.CloudsSubSystem
import pirate.scenes.level.LevelView
import pirate.core.{StartupData, Model, ViewModel}
import pirate.scenes.level.model.Platform
import pirate.scenes.level.model.LevelModel
import pirate.scenes.level.viewmodel.LevelViewModel
import pirate.scenes.level.viewmodel.PirateViewState
import pirate.scenes.level.model.Pirate
import indigoextras.geometry.Vertex

final case class LevelScene(screenWidth: Int) extends Scene[StartupData, Model, ViewModel] {
  type SceneModel     = LevelModel
  type SceneViewModel = LevelViewModel

  val name: SceneName = LevelScene.name
  val modelLens: Lens[Model, LevelModel] =
    Lens(
      _.gameScene,
      (m, sm) => m.copy(gameScene = sm)
    )

  val viewModelLens: Lens[ViewModel, LevelViewModel] =
    Lens(
      _.level,
      (vm, svm) => vm.copy(level = svm)
    )

  val eventFilters: EventFilters =
    EventFilters.Restricted

  val subSystems: Set[SubSystem] =
    Set(
      CloudsAutomata.automata,
      CloudsSubSystem(screenWidth)
    )

  def updateModel(
      context: FrameContext[StartupData],
      model: LevelModel
  ): GlobalEvent => Outcome[LevelModel] = {
    case FrameTick if model.notReady =>
      (model, context.startUpData.levelDataStore) match {
        case (LevelModel.NotReady, Some(levelDataStore)) =>
          Outcome(
            LevelModel.Ready(
              Pirate.initial,
              Platform.fromTerrainMap(levelDataStore.terrainMap)
            )
          )

        case _ =>
          Outcome(model)
      }

    case FrameTick =>
      model.update(context.gameTime, context.inputState)

    case _ =>
      Outcome(model)
  }

  def updateViewModel(
      context: FrameContext[StartupData],
      model: LevelModel,
      viewModel: LevelViewModel
  ): GlobalEvent => Outcome[LevelViewModel] = {
    case FrameTick if viewModel.notReady =>
      (viewModel, context.startUpData.levelDataStore) match {
        case (LevelViewModel.NotReady, Some(levelDataStore)) =>
          val changeSpace: Vertex => Point =
            v => (v * Vertex.fromPoint(levelDataStore.tileSize)).toPoint

          Outcome(LevelViewModel.Ready(changeSpace, PirateViewState.initial))

        case _ =>
          Outcome(viewModel)
      }

    case FrameTick =>
      model match {
        case LevelModel.NotReady =>
          Outcome(viewModel)

        case LevelModel.Ready(pirate, _) =>
          viewModel.update(context.gameTime, pirate)
      }

    case _ =>
      Outcome(viewModel)
  }

  def present(
      context: FrameContext[StartupData],
      model: LevelModel,
      viewModel: SceneViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      (model, viewModel) match {
        case (m @ LevelModel.Ready(_, _), vm @ LevelViewModel.Ready(_, _)) =>
          LevelView.draw(context.gameTime, m, vm, context.startUpData.captain, context.startUpData.levelDataStore)

        case _ =>
          SceneUpdateFragment.empty
      }
    )
}

object LevelScene {
  val name: SceneName = SceneName("demo")
}
