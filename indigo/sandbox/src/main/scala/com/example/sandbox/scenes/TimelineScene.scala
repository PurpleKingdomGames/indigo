package com.example.sandbox.scenes

import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo.*
import indigo.scenes.*
import indigo.syntax.*
import indigoextras.animation.TimeSlot
import indigoextras.animation.Timeline

object TimelineScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  type SceneModel     = Unit
  type SceneViewModel = Unit

  def eventFilters: EventFilters =
    EventFilters.Permissive

  def modelLens: Lens[SandboxGameModel, Unit] =
    Lens.unit

  def viewModelLens: Lens[SandboxViewModel, Unit] =
    Lens.unit

  def name: SceneName =
    SceneName("timeline")

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

  val crate = Graphic(64, 64, SandboxAssets.cratesMaterial)
    .modifyMaterial(_.withLighting(LightingModel.Unlit))
    .withCrop(0, 0, 32, 32)

  // No frills timeline animation
  val timeline =
    Timeline(
      TimeSlot(
        2.seconds,
        9.seconds,
        (g: Graphic[Material.Bitmap]) =>
          Signal
            .EaseInOut(
              5.seconds
            ) // This doesn't do what you think, warping time affects the distance you can travel, hence the 500 below
            .map(d => Seconds(d))
            .map { t =>
              Signal
                .Lerp(Point(0), Point(500), 5.seconds)
                .map(pt => g.moveTo(pt))
                .at(t)
            }
      )
    )

  def present(
      context: FrameContext[SandboxStartupData],
      model: Unit,
      viewModel: Unit
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        Layer(
          Batch.fromOption(
            timeline.at(context.running)(crate)
          )
        )
      )
    )
