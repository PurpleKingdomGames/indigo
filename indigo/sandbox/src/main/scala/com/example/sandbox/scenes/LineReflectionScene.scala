package com.example.sandbox.scenes

import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo.*
import indigo.scenes.*
import indigo.syntax.*

object LineReflectionScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("LineReflectionScene")

  def subSystems: Set[SubSystem] =
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

  val vertexCirce =
    Shape.Circle(
      Point(0),
      5,
      Fill.Color(RGBA.Green),
      Stroke.None
    )

  def present(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): Outcome[SceneUpdateFragment] =

    def makeVertex(timeMultiplier: Double, timeOffset: Double, radius: Double): Vertex =
      Vertex(
        Math.sin(Radians.fromSeconds((context.sceneRunning * timeMultiplier) + timeOffset).toDouble) * radius,
        Math.cos(Radians.fromSeconds((context.sceneRunning * timeMultiplier) + timeOffset).toDouble) * radius
      )

    val vtxA = context.startUpData.viewportCenter.toVertex + makeVertex(0.1, 0.0, 50.0)
    val vtxB = context.startUpData.viewportCenter.toVertex + makeVertex(0.1, 0.5, 50.0)

    val surface = LineSegment(vtxA, vtxB)

    val incident = LineSegment(Vertex(10), Vertex(400))

    val reflection =
      surface
        .reflect(incident)
        .toBatch
        .flatMap { rd =>
          val normalAtIntersect = LineSegment(rd.at, rd.at + (rd.normal.toVertex * 30))
          val reflection        = LineSegment(rd.at, rd.at + (rd.reflected.toVertex * 200))

          Batch(
            // Normal at intersection point
            Shape.Line(
              normalAtIntersect.start.toPoint,
              normalAtIntersect.end.toPoint,
              Stroke(1, RGBA.Yellow)
            ),
            // Reflection
            Shape.Line(
              reflection.start.toPoint,
              reflection.end.toPoint,
              Stroke(1, RGBA.Magenta)
            )
          )
        }

    val nrml = LineSegment(surface.center, surface.center + (surface.normal.toVertex * 30))

    Outcome(
      SceneUpdateFragment.empty
        .addLayer(
          Batch(
            // Surface
            Shape.Line(
              surface.start.toPoint,
              surface.end.toPoint,
              Stroke(1, RGBA.White)
            ),
            // Normal
            Shape.Line(
              nrml.start.toPoint,
              nrml.end.toPoint,
              Stroke(1, RGBA.Green)
            ),
            // Incident
            Shape.Line(
              incident.start.toPoint,
              incident.end.toPoint,
              Stroke(1, RGBA.Cyan)
            )
          ) ++
            reflection
        )
    )
