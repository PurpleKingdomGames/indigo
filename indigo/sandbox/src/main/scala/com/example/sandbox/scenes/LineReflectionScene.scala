package com.example.sandbox.scenes

import com.example.sandbox.Fonts
import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo.*
import indigo.scenes.*
import indigo.syntax.*

object LineReflectionScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  type SceneModel     = Radians
  type SceneViewModel = SandboxViewModel

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: Lens[SandboxGameModel, Radians] =
    Lens(_.rotation, (m, r) => m.copy(rotation = r))

  def viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("LineReflectionScene")

  def subSystems: Set[SubSystem[SandboxGameModel]] =
    Set()

  def updateModel(
      context: SceneContext[SandboxStartupData],
      rotation: Radians
  ): GlobalEvent => Outcome[Radians] =
    case KeyboardEvent.KeyDown(Key.ARROW_LEFT) =>
      Outcome(rotation - Radians(0.05))

    case KeyboardEvent.KeyDown(Key.ARROW_RIGHT) =>
      Outcome(rotation + Radians(0.05))

    case _ =>
      Outcome(rotation)

  def updateViewModel(
      context: SceneContext[SandboxStartupData],
      rotation: Radians,
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

  def round2dp(v: Vector2): Vector2 =
    Vector2(
      round2dp(v.x),
      round2dp(v.y)
    )

  def round2dp(d: Double): Double =
    Math.round(d * 100.0) / 100.0

  def present(
      context: SceneContext[SandboxStartupData],
      rotation: Radians,
      viewModel: SandboxViewModel
  ): Outcome[SceneUpdateFragment] =

    def makeVertex(offset: Double, radius: Double): Vertex =
      Vertex(
        Math.sin(rotation.toDouble + offset) * radius,
        Math.cos(rotation.toDouble + offset) * radius
      )

    val vtxA = context.startUpData.viewportCenter.toVertex + makeVertex(0.0, 50.0)
    val vtxB = context.startUpData.viewportCenter.toVertex + makeVertex(Math.PI, 50.0)

    val surface = LineSegment(vtxA, vtxB)

    val incident = LineSegment(Vertex(10), Vertex(400))

    val reflectionData =
      surface.reflect(incident)

    val reflection =
      reflectionData.toBatch
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

    val textTemplate =
      Text("", Fonts.fontKey, SandboxAssets.fontMaterial)
        .moveTo(10, 10)

    val text =
      reflectionData match
        case None =>
          Batch(
            textTemplate.withText("Left + Right arrows to rotate").moveBy(0, 0)
          )

        case Some(rd) =>
          Batch(
            textTemplate.withText("Left + Right arrows to rotate").moveBy(0, 0),
            textTemplate.withText(s"Incident: ${round2dp(rd.incident)}").moveBy(0, 30),
            textTemplate.withText(s"Normal: ${round2dp(rd.normal)}").moveBy(0, 60),
            textTemplate.withText(s"Reflected: ${round2dp(rd.reflected)}").moveBy(0, 90),
            textTemplate.withText(s"I.N: ${round2dp(rd.incident.dot(rd.normal))}").moveBy(0, 120),
            textTemplate.withText(s"N.R: ${round2dp(rd.normal.dot(rd.reflected))}").moveBy(0, 150)
          )

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
        .addLayer(
          Layer(text)
            .withMagnification(1)
            .withCamera(
              Camera
                .Fixed(context.startUpData.viewportCenter * 2)
                .withZoom(Zoom.x05)
            )
        )
    )
