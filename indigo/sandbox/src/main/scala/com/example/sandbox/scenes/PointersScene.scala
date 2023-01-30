package com.example.sandbox.scenes

import com.example.sandbox.Fonts
import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo.*
import indigo.scenes.*
import indigo.shared.events.PointerEvent

object PointersScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  type SceneModel     = PointersModel
  type SceneViewModel = Unit

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: Lens[SandboxGameModel, PointersModel] =
    Lens(_.pointers, (m, p) => m.copy(pointers = p))

  def viewModelLens: Lens[SandboxViewModel, Unit] =
    Lens.unit

  def name: SceneName =
    SceneName("pointers")

  def subSystems: Set[SubSystem] =
    Set()

  def updateModel(
      context: SceneContext[SandboxStartupData],
      model: PointersModel
  ): GlobalEvent => Outcome[PointersModel] =
    case e: PointerEvent.PointerDown =>
      println(e)
      Outcome(
        model.copy(isPainting = true).append(e.position)
      )
    case e: (PointerEvent.PointerUp | PointerEvent.PointerCancel) =>
      println(e)
      Outcome(
        model.copy(isPainting = false)
      )
    case e: PointerEvent.PointerMove =>
      println(e)
      Outcome(
        if model.isPainting then model.append(e.position) else model
      )
    // case e: PointerEvent =>
    //   println(e)
    //   Outcome(model)
    case _ => Outcome(model)

  def updateViewModel(
      context: SceneContext[SandboxStartupData],
      model: PointersModel,
      viewModel: Unit
  ): GlobalEvent => Outcome[Unit] =
    _ => Outcome(viewModel)

  val textMaterial = SandboxAssets.fontMaterial.toBitmap

  def present(
      context: SceneContext[SandboxStartupData],
      model: PointersModel,
      viewModel: Unit
  ): Outcome[SceneUpdateFragment] =

    val paint = model.paint.map { point =>
      Shape.Circle(
        center = point,
        radius = 20,
        fill = Fill.Color(RGBA.Tomato),
        stroke = Stroke.None
      )
    }

    // println("paint" -> paint)

    Outcome(
      SceneUpdateFragment(
        Layer(
          Batch(
            Text("Touch the screen", Fonts.fontKey, textMaterial)
              .moveTo(10, 10)
          ) ++ paint
        )
        // .withMagnification(1)
      )
    )

case class PointersModel(
    paint: Batch[Point],
    isPainting: Boolean
):
  def append(point: Point): PointersModel = copy(paint = paint :+ point)

object PointersModel:
  val empty: PointersModel = PointersModel(
    paint = Batch.empty[Point],
    isPainting = false
  )
