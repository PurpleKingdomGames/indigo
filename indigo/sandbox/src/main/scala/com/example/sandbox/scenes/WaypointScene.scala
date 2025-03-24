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
import indigoextras.waypoints.WaypointPathPosition

object WaypointScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  type SceneModel     = Unit
  type SceneViewModel = Unit

  def eventFilters: EventFilters =
    EventFilters.Permissive

  def modelLens: Lens[SandboxGameModel, SceneModel] =
    Lens.unit

  def viewModelLens: Lens[SandboxViewModel, Unit] =
    Lens.unit

  def name: SceneName =
    SceneName("waypoints")

  def subSystems: Set[SubSystem[SandboxGameModel]] =
    Set()

  def updateModel(
      context: SceneContext[SandboxStartupData],
      model: SceneModel
  ): GlobalEvent => Outcome[SceneModel] =
    _ => Outcome(model)

  def updateViewModel(
      context: SceneContext[SandboxStartupData],
      model: SceneModel,
      viewModel: Unit
  ): GlobalEvent => Outcome[Unit] =
    _ => Outcome(viewModel)

  def dude(color: RGB): Graphic[Material.ImageEffects] =
    Graphic(32, 32, SandboxAssets.dudeMaterial)
      .modifyMaterial(_.disableLighting.withTint(color))
      .withCrop(0, 0, 32, 32)
      .withRef(16, 16)

  val moveAndRotate
      : Graphic[Material.ImageEffects] => SignalFunction[WaypointPathPosition, Graphic[Material.ImageEffects]] = g =>
    SignalFunction(wpp => g.moveTo(wpp.position.toPoint).rotateTo(wpp.direction))

  val pentagramWaypoints = (0 until 5).toBatch.map: i =>
    val angle = ((Radians.PI * 4) / 5) * i
    val x     = Math.cos(angle.toDouble) * 60 + 137.5
    val y     = Math.sin(angle.toDouble) * 60 + 100
    Vertex(x, y)

  val decagonWaypoints = (0 until 10).toBatch.map: i =>
    val angle = (Radians.PI / 5) * i
    val x     = Math.cos(angle.toDouble) * 60 + 137.5
    val y     = Math.sin(angle.toDouble) * 60 + 100
    Vertex(x, y)

  def traverseWaypoints(waypoints: Batch[Vertex], loop: Boolean): SignalFunction[Double, WaypointPathPosition] =
    val path = WaypointPath(waypoints, 0.0, loop)
    SignalFunction(over => path.calculatePosition(over))

  val traversePentagram: SignalFunction[Double, WaypointPathPosition] = traverseWaypoints(pentagramWaypoints, true)
  val traverseDecagon: SignalFunction[Double, WaypointPathPosition]   = traverseWaypoints(decagonWaypoints, true)

  def mult(amount: Double): SignalFunction[Double, Double] =
    SignalFunction(_ * amount)

  val delayDuration     = 5.seconds
  val animationDuration = 24.seconds

  val tl1: Double => Timeline[Graphic[Material.ImageEffects]] = loops =>
    timeline(
      layer(
        startAfter(delayDuration),
        animate(animationDuration) {
          easeIn >>> mult(loops) >>> traverseDecagon >>> moveAndRotate(_)
        }
      )
    )

  val tl2: Double => Timeline[Graphic[Material.ImageEffects]] = loops =>
    timeline(
      layer(
        startAfter(delayDuration),
        animate(animationDuration) {
          easeOut >>> mult(loops) >>> traverseDecagon >>> moveAndRotate(_)
        }
      )
    )

  val tl3: Double => Timeline[Graphic[Material.ImageEffects]] = loops =>
    timeline(
      layer(
        startAfter(delayDuration),
        animate(animationDuration) {
          easeInOut >>> mult(loops) >>> traverseDecagon >>> moveAndRotate(_)
        }
      )
    )

  val tl4: Double => Timeline[Graphic[Material.ImageEffects]] = loops =>
    timeline(
      layer(
        startAfter(delayDuration),
        animate(animationDuration) {
          lerp >>> mult(loops) >>> traversePentagram >>> moveAndRotate(_)
        }
      )
    )

  def present(
      context: SceneContext[SandboxStartupData],
      model: SceneModel,
      viewModel: Unit
  ): Outcome[SceneUpdateFragment] =

    val waypointGraphics = decagonWaypoints.map: w =>
      Shape.Circle(
        w.toPoint,
        4,
        Fill.Color(RGBA.Indigo),
        Stroke(1, RGBA.White)
      )

    val pentagramPathGraphics = pentagramWaypoints
      .zip(pentagramWaypoints.tail :+ pentagramWaypoints.head)
      .map: (w1, w2) =>
        Shape.Line(
          w1.toPoint,
          w2.toPoint,
          Stroke(1, RGBA.White)
        )

    val decagonPathGraphics = decagonWaypoints
      .zip(decagonWaypoints.tail :+ decagonWaypoints.head)
      .map: (w1, w2) =>
        Shape.Line(
          w1.toPoint,
          w2.toPoint,
          Stroke(1, RGBA.Black)
        )

    Outcome(
      SceneUpdateFragment(
        pentagramPathGraphics ++
          decagonPathGraphics ++
          waypointGraphics ++
          tl1(8.0).atOrLast(context.frame.time.running)(dude(RGB.Coral)).toBatch ++
          tl2(8.0).atOrLast(context.frame.time.running)(dude(RGB.Plum)).toBatch ++
          tl3(8.0).atOrLast(context.frame.time.running)(dude(RGB.SeaGreen)).toBatch ++
          tl4(8.0).atOrLast(context.frame.time.running)(dude(RGB.Crimson)).toBatch ++
          tl4(-8.0).atOrLast(context.frame.time.running)(dude(RGB.Thistle)).toBatch
      )
    )
