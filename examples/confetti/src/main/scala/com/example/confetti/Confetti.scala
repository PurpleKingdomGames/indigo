package com.example.confetti

import indigo._
import indigoextras.subsystems.FPSCounter

import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("IndigoGame")
object Confetti extends IndigoDemo[Unit, Unit, Model, Unit]:

  val eventFilters: EventFilters = EventFilters.Permissive

  def boot(flags: Map[String, String]): Outcome[BootResult[Unit]] =
    Outcome(
      BootResult
        .noData(
          GameConfig.default
            .withViewport(640, 480)
            .withClearColor(RGBA(0.0, 0.2, 0.0, 1.0))
            .withMagnification(1)
            .withFrameRate(60)
        )
        .withAssets(Assets.assets)
        .withSubSystems(FPSCounter(Point.zero, FPS.`60`))
    )

  def setup(bootData: Unit, assetCollection: AssetCollection, dice: Dice): Outcome[Startup[Unit]] =
    Outcome(Startup.Success(()))

  def initialModel(startupData: Unit): Outcome[Model] =
    Outcome(Model.empty)

  def initialViewModel(startupData: Unit, model: Model): Outcome[Unit] =
    Outcome(())

  def updateModel(context: FrameContext[Unit], model: Model): GlobalEvent => Outcome[Model] = event =>
    val isLeftDown  = context.mouse.isLeftDown
    val isRightDown = context.mouse.isRightDown

    event match
      case FrameTick if isLeftDown || isRightDown =>
        (isLeftDown, isRightDown) match
          case (true, true) =>
            val lmb = model.spawn(context, 8, MouseButton.LeftMouseButton)
            val rmb = lmb.spawn(context, 7, MouseButton.RightMouseButton)
            Outcome(rmb.update)
          case (true, false) =>
            Outcome(model.spawn(context, 15, MouseButton.LeftMouseButton).update)
          case (false, true) =>
            Outcome(model.spawn(context, 15, MouseButton.RightMouseButton).update)
          case (false, false) => // Not reachable
            Outcome(model.update)
      case FrameTick =>
        Outcome(model.update)
      case _ =>
        Outcome(model)

  def updateViewModel(context: FrameContext[Unit], model: Model, viewModel: Unit): GlobalEvent => Outcome[Unit] =
    _ => Outcome(viewModel)

  val lmbDots: List[Graphic[Material.Bitmap]] =
    List(
      Assets.redDot,
      Assets.greenDot
    )

  val rmbDots: List[Graphic[Material.Bitmap]] =
    List(
      Assets.blueDot,
      Assets.yellowDot
    )

  val count: TextBox =
    TextBox("", 640, 20).alignCenter
      .withFontSize(Pixels(12))
      .withColor(RGBA.White)

  val helpText: TextBox =
    TextBox("Click left, right or both anywhere!", 640, 20).alignRight
      .withFontSize(Pixels(12))
      .withColor(RGBA.White)

  def present(context: FrameContext[Unit], model: Model, viewModel: Unit): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        model.particles.map { p =>
          val dots = p.source match
            case MouseButton.LeftMouseButton  => lmbDots
            case MouseButton.RightMouseButton => rmbDots

          dots(p.color).moveTo(p.x, p.y).scaleBy(p.scale, p.scale)
        } ++ List(
          count.withText(s"count: ${model.particles.length}"),
          helpText
        )
      )
    )

opaque type Model = (Int, List[Particle])
type LmbOrRmb     = MouseButton.LeftMouseButton.type | MouseButton.RightMouseButton.type

object Model:
  def empty: Model = (0, Nil)

  extension (m: Model)
    def color: Int                = m._1
    def particles: List[Particle] = m._2

    def spawn(context: FrameContext[Unit], count: Int, source: LmbOrRmb): Model =
      (
        m._1,
        (0 until count).toList.map { _ =>
          Particle(
            context.mouse.position.x,
            context.mouse.position.y,
            context.dice.rollDouble * 2.0 - 1.0,
            context.dice.rollDouble * 2.0,
            m._1,
            context.dice.rollDouble * 0.5 + 0.5,
            source
          )
        } ++ m._2
      )

    def update: Model =
      (
        (m._1 + 1) % 2,
        m._2
          .filter(p => p.y < 500)
          .map { p =>
            val newFy = p.fy - 0.1
            val newFx = p.fx * 0.95
            p.copy(
              x = (p.x + (15 * newFx)).toInt,
              y = (p.y - (5 * newFy)).toInt,
              fx = newFx,
              fy = newFy
            )
          }
      )

final case class Particle(x: Int, y: Int, fx: Double, fy: Double, color: Int, scale: Double, source: LmbOrRmb)
