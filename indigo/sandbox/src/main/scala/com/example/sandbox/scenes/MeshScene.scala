package com.example.sandbox.scenes

import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo.*
import indigo.scenes.*
import indigoextras.mesh.*

object MeshScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("mesh scene")

  def subSystems: Set[SubSystem[SandboxGameModel]] =
    Set()

  def updateModel(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel
  ): GlobalEvent => Outcome[SandboxGameModel] =
    _ => Outcome(model)

  def updateViewModel(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): GlobalEvent => Outcome[SandboxViewModel] =
    _ => Outcome(viewModel)

  def present(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): Outcome[SceneUpdateFragment] =

    Outcome(
      SceneUpdateFragment(
        drawMesh(
          model.meshData.points,
          model.meshData.superTriangle,
          model.meshData.mesh.toLineSegments
        )
      )
    )

  def drawMesh(
      points: Batch[Point],
      superTriangle: Triangle,
      lines: Batch[LineSegment]
  ): Batch[SceneNode] =
    val pts: Batch[Shape.Circle] =
      points.map { pt =>
        Shape.Circle(
          pt,
          5,
          Fill.Color(RGBA.Green)
        )
      }

    val st = superTriangle.toLineSegments.map { ls =>
      Shape.Line(ls.start.toPoint, ls.end.toPoint, Stroke(1, RGBA.Magenta))
    }

    val ml = lines.map { ls =>
      Shape.Line(ls.start.toPoint, ls.end.toPoint, Stroke(1, RGBA.Cyan))
    }

    pts ++ st ++ ml
