package pirate.scenes.level

import indigo.*
import indigo.scenes.*
import indigo.physics.*

import pirate.scenes.level.subsystems.CloudsAutomata
import pirate.scenes.level.subsystems.CloudsSubSystem
import pirate.scenes.level.LevelView
import pirate.core.{StartupData, Model, ViewModel}
import pirate.scenes.level.model.Platform
import pirate.scenes.level.model.LevelModel
import pirate.scenes.level.viewmodel.LevelViewModel
import pirate.scenes.level.viewmodel.PirateViewState
import pirate.scenes.level.model.Pirate
import pirate.scenes.level.model.PirateRespawn

final case class LevelScene(screenWidth: Int) extends Scene[StartupData, Model, ViewModel]:
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
      context: SceneContext[StartupData],
      model: LevelModel
  ): GlobalEvent => Outcome[LevelModel] =
    case PirateRespawn(at) =>
      model match
        case LevelModel.NotReady =>
          Outcome(model)

        case LevelModel.Ready(pirate, platform, world) =>
          Outcome(
            LevelModel.Ready(
              pirate,
              platform,
              world.modifyByTag("pirate")(_.moveTo(at).withVelocity(Vector2.zero))
            )
          )

    case FrameTick if model.notReady =>
      (model, context.startUpData.levelDataStore) match
        case (LevelModel.NotReady, Some(levelDataStore)) =>
          val pirate   = Pirate.initial
          val platform = Platform.fromTerrainMap(levelDataStore.terrainMap)

          Outcome(
            LevelModel.Ready(
              pirate,
              platform,
              World
                .empty[String]
                .withResistance(Resistance(0.01))
                .withForces(Vector2(0, 30))
                .withColliders(platform.navMesh)
                .addColliders(Collider.Box("pirate", Pirate.initialBounds).withRestitution(Restitution(0)))
            )
          )

        case _ =>
          Outcome(model)

    case FrameTick =>
      model.update(context.gameTime, context.inputState)

    case _ =>
      Outcome(model)

  def updateViewModel(
      context: SceneContext[StartupData],
      model: LevelModel,
      viewModel: LevelViewModel
  ): GlobalEvent => Outcome[LevelViewModel] =
    case FrameTick if viewModel.notReady =>
      (viewModel, context.startUpData.levelDataStore) match
        case (LevelViewModel.NotReady, Some(levelDataStore)) =>
          val changeSpace: Vertex => Vertex =
            _ * levelDataStore.tileSize.toVertex

          Outcome(LevelViewModel.Ready(changeSpace, PirateViewState.initial))

        case _ =>
          Outcome(viewModel)

    case FrameTick =>
      model match
        case LevelModel.NotReady =>
          Outcome(viewModel)

        case LevelModel.Ready(pirate, _, _) =>
          viewModel.update(context.gameTime, pirate)

    case _ =>
      Outcome(viewModel)

  def present(
      context: SceneContext[StartupData],
      model: LevelModel,
      viewModel: SceneViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      (model, viewModel) match
        case (m @ LevelModel.Ready(_, _, _), vm @ LevelViewModel.Ready(_, _)) =>
          LevelView.draw(context.gameTime, m, vm, context.startUpData.captain, context.startUpData.levelDataStore)

        case _ =>
          SceneUpdateFragment.empty
    )

object LevelScene:
  val name: SceneName =
    SceneName("demo")
