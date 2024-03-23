package com.example.sandbox.scenes

import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo.*
import indigo.scenes.*
import indigo.syntax.*

object CameraWithCloneTilesScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  type SceneModel     = Unit
  type SceneViewModel = Unit

  def eventFilters: EventFilters =
    EventFilters.Permissive

  def modelLens: Lens[SandboxGameModel, Unit] =
    Lens.unit

  def viewModelLens: Lens[SandboxViewModel, Unit] =
    Lens.unit

  def name: SceneName =
    SceneName("crates with camera")

  def subSystems: Set[SubSystem[SandboxGameModel]] =
    Set()

  def updateModel(
      context: SceneContext[SandboxStartupData],
      model: Unit
  ): GlobalEvent => Outcome[Unit] =
    _ => Outcome(model)

  def updateViewModel(
      context: SceneContext[SandboxStartupData],
      model: Unit,
      viewModel: Unit
  ): GlobalEvent => Outcome[Unit] =
    _ => Outcome(viewModel)

  val graphic = Graphic(64, 64, SandboxAssets.cratesMaterial.withLighting(LightingModel.Unlit))

  val cloneId: CloneId = CloneId("crates")

  val cloneBlanks: Batch[CloneBlank] =
    Batch(CloneBlank(cloneId, graphic).static)

  val crates =
    (0 to 5).flatMap { row =>
      (0 to 5).map { col =>
        CloneTileData(col * 32, row * 32, Radians.zero, 1.0, 1.0, 0, 0, 32, 32)
      }
    }.toBatch

  def present(
      context: SceneContext[SandboxStartupData],
      model: Unit,
      viewModel: Unit
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        Layer(
          CloneTiles(cloneId, crates)
        ).withMagnification(2)
          .withCamera(Camera.LookAt(Point(96)))
      ).addCloneBlanks(cloneBlanks)
    )
