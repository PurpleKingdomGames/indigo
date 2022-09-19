package com.example.sandbox.scenes

import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo.*
import indigo.scenes.*
import indigo.shared.temporal.SignalFunction as SF
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

  def toPoint(from: Point, to: Point): SignalFunction[Double, Point] =
    SignalFunction { amount =>
      def linear(p0: Vector2, p1: Vector2): Vector2 =
        Vector2(
          (1 - amount) * p0.x + amount * p1.x,
          (1 - amount) * p0.y + amount * p1.y
        )

      val interp = linear(from.toVector, to.toVector).toPoint

      Point(
        x = if (from.x == to.x) from.x else interp.x,
        y = if (from.y == to.y) from.y else interp.y
      )
    }

  def move(g: Graphic[Material.Bitmap]): SignalFunction[Point, Graphic[Material.Bitmap]] =
    SignalFunction { pt =>
      g.moveTo(pt)
    }

  // No frills timeline animation
  val timeline =
    Timeline(
      TimeSlot(
        2.seconds,
        7.seconds,
        (g: Graphic[Material.Bitmap]) =>
          SF.easeInOut(5.seconds) >>> toPoint(Point(0), Point(100)) >>> move(g)
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
