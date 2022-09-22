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

  val crate: Graphic[Material.ImageEffects] = 
    Graphic(64, 64, SandboxAssets.cratesMaterial)
      .modifyMaterial(_.toImageEffects.withLighting(LightingModel.Unlit))
      .withCrop(0, 0, 32, 32)

  // No frills timeline animation
  import indigo.shared.temporal.SignalFunction as SF

  def move(g: Graphic[Material.ImageEffects]): SignalFunction[Point, Graphic[Material.ImageEffects]] =
    SignalFunction(pt => g.moveTo(pt))

  val modifier1: Graphic[Material.ImageEffects] => SignalFunction[Seconds, Graphic[Material.ImageEffects]] =
    SF.easeInOut(5.seconds) >>> SF.lerp(Point(0), Point(100)) >>> move(_)

  val modifier2: Graphic[Material.ImageEffects] => SignalFunction[Seconds, Graphic[Material.ImageEffects]] =
    SF.easeInOut(3.seconds) >>> SF.lerp(Point(100), Point(100, 0)) >>> move(_)

  val modifier3: Graphic[Material.ImageEffects] => SignalFunction[Seconds, Graphic[Material.ImageEffects]] =
    g => SF.sin >>> SignalFunction(d => (d + 1) / 2) >>> SignalFunction(d => g.modifyMaterial(_.withAlpha(d)))

  val timeline =
    Timeline(
      TimeSlot(2.seconds, 7.seconds, modifier1),
      TimeSlot(7.seconds, 10.seconds, modifier2),
      TimeSlot(2.seconds, 10.seconds, modifier3)
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
