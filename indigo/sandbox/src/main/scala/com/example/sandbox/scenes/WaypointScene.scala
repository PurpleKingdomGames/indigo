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

  val moveAndRotateShape: Shape[?] => SignalFunction[WaypointPathPosition, Shape[?]] = g =>
    SignalFunction(wpp => g.moveTo(wpp.position.toPoint))

  val pentagramWaypoints = (0 until 5).toBatch.map: i =>
    val angle = ((Radians.PI * 4) / 5) * i
    val x     = Math.cos(angle.toDouble) * 60 + 200
    val y     = Math.sin(angle.toDouble) * 60 + 100
    Vertex(x, y)

  val decagonWaypoints = (0 until 10).toBatch.map: i =>
    val angle = (Radians.PI / 5) * i
    val x     = Math.cos(angle.toDouble) * 60 + 200
    val y     = Math.sin(angle.toDouble) * 60 + 100
    Vertex(x, y)

  val simplePathWaypoints = Batch(Vertex(20, 20), Vertex(100, 70), Vertex(20, 120), Vertex(100, 170))

  def traverseWaypoints(path: WaypointPath): SignalFunction[Double, WaypointPathPosition] =
    SignalFunction(over => path.calculatePosition(over))

  val traversePentagram: SignalFunction[Double, WaypointPathPosition] =
    traverseWaypoints(WaypointPath(pentagramWaypoints).withLooping(true))
  val traverseDecagon: SignalFunction[Double, WaypointPathPosition] =
    traverseWaypoints(WaypointPath(decagonWaypoints).withLooping(true))

  val directPath       = WaypointPath(simplePathWaypoints)
  val mediumRadiusPath = WaypointPath(simplePathWaypoints).withProximityRadius(10.0)
  val largeRadiusPath  = WaypointPath(simplePathWaypoints).withProximityRadius(20.0)
  val traverseSimplePathDirect: SignalFunction[Double, WaypointPathPosition] = traverseWaypoints(directPath)
  val traverseSimplePathMedium: SignalFunction[Double, WaypointPathPosition] = traverseWaypoints(mediumRadiusPath)
  val traverseSimplePathLarge: SignalFunction[Double, WaypointPathPosition]  = traverseWaypoints(largeRadiusPath)

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

  val tl5: Timeline[Shape[?]] =
    timeline(
      layer(
        startAfter(delayDuration),
        animate(animationDuration) {
          lerp >>> traverseSimplePathDirect >>> moveAndRotateShape(_)
        }
      )
    )

  val tl6: Timeline[Shape[?]] =
    timeline(
      layer(
        startAfter(delayDuration),
        animate(animationDuration) {
          lerp >>> traverseSimplePathMedium >>> moveAndRotateShape(_)
        }
      )
    )

  val tl7: Timeline[Shape[?]] =
    timeline(
      layer(
        startAfter(delayDuration),
        animate(animationDuration) {
          lerp >>> traverseSimplePathLarge >>> moveAndRotateShape(_)
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

    val simplePathWaypointGraphics = simplePathWaypoints.map: w =>
      Shape.Circle(
        w.toPoint,
        3,
        Fill.Color(RGBA.Green),
        Stroke.None
      )

    val directPathGraphics = directPath.calculatedWaypoints
      .dropRight(1)
      .zip(directPath.calculatedWaypoints.tail)
      .map: (w1, w2) =>
        Shape.Line(
          w1.toPoint,
          w2.toPoint,
          Stroke(1, RGBA.White)
        )

    val mediumRadiusPathGraphics = mediumRadiusPath.calculatedWaypoints
      .dropRight(1)
      .zip(mediumRadiusPath.calculatedWaypoints.tail)
      .map: (w1, w2) =>
        Shape.Line(
          w1.toPoint,
          w2.toPoint,
          Stroke(1, RGBA.DarkBlue)
        )

    val largeRadiusPathGraphics = largeRadiusPath.calculatedWaypoints
      .dropRight(1)
      .zip(largeRadiusPath.calculatedWaypoints.tail)
      .map: (w1, w2) =>
        Shape.Line(
          w1.toPoint,
          w2.toPoint,
          Stroke(1, RGBA.Crimson)
        )

    val smallCircle = Shape
      .Circle(
        Point.zero,
        4,
        Fill.None,
        Stroke(1, RGBA.White)
      )

    val mediumCircle = Shape
      .Circle(
        Point.zero,
        10,
        Fill.None,
        Stroke(1, RGBA.DarkBlue)
      )

    val largeCircle = Shape
      .Circle(
        Point.zero,
        20,
        Fill.None,
        Stroke(1, RGBA.Crimson)
      )

    Outcome(
      SceneUpdateFragment(
        pentagramPathGraphics ++
          decagonPathGraphics ++
          waypointGraphics ++
          directPathGraphics ++
          mediumRadiusPathGraphics ++
          largeRadiusPathGraphics ++
          simplePathWaypointGraphics ++
          tl1(8.0).atOrLast(context.frame.time.running)(dude(RGB.Coral)).toBatch ++
          tl2(8.0).atOrLast(context.frame.time.running)(dude(RGB.Plum)).toBatch ++
          tl3(8.0).atOrLast(context.frame.time.running)(dude(RGB.SeaGreen)).toBatch ++
          tl4(8.0).atOrLast(context.frame.time.running)(dude(RGB.Crimson)).toBatch ++
          tl4(-8.0).atOrLast(context.frame.time.running)(dude(RGB.Thistle)).toBatch ++
          Batch(tl5.atOrElse(context.frame.time.running)(smallCircle)) ++
          Batch(tl6.atOrElse(context.frame.time.running)(mediumCircle)) ++
          Batch(tl7.atOrElse(context.frame.time.running)(largeCircle))
      )
    )
