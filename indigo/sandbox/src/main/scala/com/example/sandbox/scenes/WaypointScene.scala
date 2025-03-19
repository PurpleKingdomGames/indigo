package com.example.sandbox.scenes

import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo.*
import indigo.scenes.*
import indigo.syntax.*
import indigo.syntax.animations.*
import indigoextras.waypoints.WaypointPath

object WaypointScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  type SceneModel     = WaypointModel
  type SceneViewModel = Unit

  def eventFilters: EventFilters =
    EventFilters.Permissive

  def modelLens: Lens[SandboxGameModel, WaypointModel] =
    Lens(_.waypointModel, (m, w) => m.copy(waypointModel = w))

  def viewModelLens: Lens[SandboxViewModel, Unit] =
    Lens.unit

  def name: SceneName =
    SceneName("waypoints")

  def subSystems: Set[SubSystem[SandboxGameModel]] =
    Set()

  def updateModel(
      context: SceneContext[SandboxStartupData],
      model: WaypointModel
  ): GlobalEvent => Outcome[WaypointModel] =
    _ => Outcome(model)

  def updateViewModel(
      context: SceneContext[SandboxStartupData],
      model: WaypointModel,
      viewModel: Unit
  ): GlobalEvent => Outcome[Unit] =
    _ => Outcome(viewModel)

  def dude(color: RGB): Graphic[Material.ImageEffects] =
    Graphic(32, 32, SandboxAssets.dudeMaterial)
      .modifyMaterial(_.disableLighting.withTint(color))
      .withCrop(0, 0, 32, 32)
      .withRef(16, 16)

  val moveAndRotate
      : Graphic[Material.ImageEffects] => SignalFunction[(Vector2, Radians), Graphic[Material.ImageEffects]] = g =>
    SignalFunction((v, r) => g.moveTo(v.toPoint).rotateTo(r))

  def traversePath(path: WaypointPath): SignalFunction[Double, (Vector2, Radians)] =
    SignalFunction(over => path.calculatePosition(over))

  def mult(amount: Double): SignalFunction[Double, Double] =
    SignalFunction(_ * amount)

  val tlEased: (Seconds, WaypointPath, Double) => Timeline[Graphic[Material.ImageEffects]] = (delay, path, loops) =>
    timeline(
      layer(
        startAfter(delay),
        animate(24.seconds) {
          wrap(4.seconds) >>> easeInOut(4.seconds) >>> mult(loops) >>> traversePath(path) >>> moveAndRotate(_)
        }
      )
    )

  val tlLinear: (Seconds, WaypointPath, Double) => Timeline[Graphic[Material.ImageEffects]] = (delay, path, loops) =>
    timeline(
      layer(
        startAfter(delay),
        animate(24.seconds) {
          wrap(4.seconds) >>> lerp(4.seconds) >>> mult(loops) >>> traversePath(path) >>> moveAndRotate(_)
        }
      )
    )

  def present(
      context: SceneContext[SandboxStartupData],
      model: WaypointModel,
      viewModel: Unit
  ): Outcome[SceneUpdateFragment] =

    val waypoints = model.waypointPath.waypoints

    val waypointGraphics = waypoints.map: w =>
      Shape.Circle(
        w.toPoint,
        4,
        Fill.Color(RGBA.Indigo),
        Stroke(1, RGBA.White)
      )

    val pathGraphics = waypoints
      .zip(waypoints.tail.appended(waypoints.head))
      .map: (w1, w2) =>
        Shape.Line(
          w1.toPoint,
          w2.toPoint,
          Stroke(1, RGBA.White)
        )

    Outcome(
      SceneUpdateFragment(
        pathGraphics.toBatch ++
          waypointGraphics.toBatch ++
          tlEased(1.seconds, model.waypointPath, 2.0).atOrLast(context.frame.time.running)(dude(RGB.Green)).toBatch ++
          tlLinear(1.seconds, model.waypointPath, 1.0).atOrLast(context.frame.time.running)(dude(RGB.Red)).toBatch ++
          tlLinear(1.seconds, model.waypointPath, -1.0).atOrLast(context.frame.time.running)(dude(RGB.Blue)).toBatch
      )
    )

final case class WaypointModel(waypointPath: WaypointPath)

object WaypointModel:
  def apply(): WaypointModel =
    val waypoints = List(
      Vector2(30, 30),
      Vector2(150, 60),
      Vector2(200, 130),
      Vector2(120, 150),
      Vector2(60, 80)
    )
    val path = WaypointPath(waypoints, 0.0, true)
    WaypointModel(path)
