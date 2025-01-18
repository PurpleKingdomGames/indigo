package com.example.sandbox.scenes

import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo.*
import indigo.scenes.*

object BoundingCircleScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("Bounding Circle Scene")

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

    val vtxA = context.startUpData.viewportCenter.toVertex + makeVertex(0.2, 0.0, 50.0)
    val vtxB = context.startUpData.viewportCenter.toVertex + makeVertex(0.1, 0.5, 55.0)
    val vtxC = context.startUpData.viewportCenter.toVertex + makeVertex(0.3, 1.5, 45.0)

    val circle = BoundingCircle.fromThreeVertices(vtxA, vtxB, vtxC).getOrElse(BoundingCircle(0, 0, 10))

    Outcome(
      SceneUpdateFragment.empty
        .addLayer(
          // Our bounding circle
          Shape.Circle(
            circle.position.toPoint,
            circle.radius.toInt,
            Fill.None,
            Stroke(1, RGBA.White)
          ),
          vertexCirce.moveTo(vtxA.toPoint),
          vertexCirce.moveTo(vtxB.toPoint),
          vertexCirce.moveTo(vtxC.toPoint)
        )
    )
