package com.example.sandbox.scenes

import com.example.sandbox.Fonts
import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo.*
import indigo.scenes.*
import org.scalajs.dom

import scala.annotation.nowarn

@nowarn("msg=unused")
object PointersScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:
  // disabling default browser touch actions
  val style = dom.document.createElement("style")
  style.innerHTML = "canvas { touch-action: none }"
  dom.document.head.appendChild(style)

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

  def subSystems: Set[SubSystem[SandboxGameModel]] =
    Set()

  def updateModel(
      context: SceneContext[SandboxStartupData],
      model: PointersModel
  ): GlobalEvent => Outcome[PointersModel] =
    case e: PointerEvent.Down =>
      Outcome(
        model.copy(isPainting = true).append(e.position)
      )
    case e: (PointerEvent.Up | PointerEvent.Cancel | PointerEvent.Leave) =>
      Outcome(
        model.copy(isPainting = false)
      )
    case e: PointerEvent.Move =>
      Outcome(
        if model.isPainting then model.append(e.position) else model
      )
    case _ => Outcome(model)

  def updateViewModel(
      context: SceneContext[SandboxStartupData],
      model: PointersModel,
      viewModel: Unit
  ): GlobalEvent => Outcome[Unit] =
    _ => Outcome(viewModel)

  val textMaterial = SandboxAssets.fontMaterial.toBitmap

  val globalMagnificationLevel = 2

  def present(
      context: SceneContext[SandboxStartupData],
      model: PointersModel,
      viewModel: Unit
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        Layer(
          Batch(
            Text("Touch the screen", Fonts.fontKey, textMaterial)
              .moveTo(10, 10)
          ) ++ model.paint.map { point =>
            Shape.Circle(
              // Related: https://github.com/PurpleKingdomGames/indigo/issues/113
              center = point * globalMagnificationLevel,
              radius = 20,
              fill = Fill.Color(RGBA.Tomato),
              stroke = Stroke.None
            )
          }
        ).withMagnification(1)
      )
    )

final case class PointersModel(
    paint: Batch[Point],
    isPainting: Boolean
):
  def append(point: Point): PointersModel = copy(paint = paint :+ point)

object PointersModel:
  val empty: PointersModel = PointersModel(
    paint = Batch.empty[Point],
    isPainting = false
  )
