package com.example.sandbox.scenes

import com.example.sandbox.Log
import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo._
import indigo.scenes._
import indigoextras.geometry.Polygon
import indigoextras.geometry.Vertex
import indigoextras.ui.HitArea

import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer

object CratesScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  val spawnCount: Int = 600

  type SceneModel     = Unit
  type SceneViewModel = Unit

  def eventFilters: EventFilters =
    EventFilters.Permissive

  def modelLens: indigo.scenes.Lens[SandboxGameModel, Unit] =
    Lens.unit

  def viewModelLens: Lens[SandboxViewModel, Unit] =
    Lens.unit

  def name: SceneName =
    SceneName("crates")

  def subSystems: Set[SubSystem] =
    Set()

  def updateModel(
      context: FrameContext[SandboxStartupData],
      model: Unit
  ): GlobalEvent => Outcome[Unit] =
    _ => Outcome(model)

  def updateViewModel(
      context: FrameContext[SandboxStartupData],
      model: Unit,
      viewModel: Unit
  ): GlobalEvent => Outcome[Unit] =
    _ => Outcome(viewModel)

  val graphic = Graphic(64, 64, SandboxAssets.cratesMaterial)

  val cloneId: CloneId = CloneId("crates")

  val cloneBlanks: List[CloneBlank] =
    List(CloneBlank(cloneId, graphic.withCrop(0, 0, 32, 32).withRef(16, 16)).static)

  val lights =
    List(
      PointLight.default.moveTo(Point(100, 100)).withFalloff(Falloff.smoothLinear),
      PointLight.default.moveTo(Point(150, 80)).withFalloff(Falloff.smoothLinear).withColor(RGBA.Yellow),
      AmbientLight(RGBA.Blue.mix(RGBA.White, 0.3).withAmount(0.5))
    )

  def present(
      context: FrameContext[SandboxStartupData],
      model: Unit,
      viewModel: Unit
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        Layer(
          graphic.moveTo(16, 16),
          CloneTiles(
            cloneId,
            Array(
              CloneTileData(80, 120, Radians.zero, 1.0, 1.0, 0, 0, 32, 32),
              CloneTileData(120, 120, Radians.zero, 1.0, 1.0, 0, 32, 16, 16),
              CloneTileData(160, 120, Radians.zero, 1.0, 1.0, 0, 48, 32, 16),
              CloneTileData(200, 120, Radians.zero, 1.0, 1.0, 32, 0, 32, 48)
            )
          )
        )
      ).addCloneBlanks(cloneBlanks)
        .addLights(lights)
    )
