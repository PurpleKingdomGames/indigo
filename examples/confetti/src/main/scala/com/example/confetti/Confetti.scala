package com.example.confetti

import indigo._

import scala.scalajs.js.annotation.JSExportTopLevel
import indigoextras.subsystems.FPSCounter

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
        .withSubSystems(FPSCounter(Point.zero, 60))
    )

  def setup(bootData: Unit, assetCollection: AssetCollection, dice: Dice): Outcome[Startup[Unit]] =
    Outcome(Startup.Success(()))

  def initialModel(startupData: Unit): Outcome[Model] =
    Outcome(Model.empty)

  def initialViewModel(startupData: Unit, model: Model): Outcome[Unit] =
    Outcome(())

  def updateModel(context: FrameContext[Unit], model: Model): GlobalEvent => Outcome[Model] =
    case FrameTick if context.mouse.leftMouseIsDown =>
      Outcome(
        model
          .spawn(context, 15)
          .update
      )

    case FrameTick =>
      Outcome(model.update)

    case _ =>
      Outcome(model)

  def updateViewModel(context: FrameContext[Unit], model: Model, viewModel: Unit): GlobalEvent => Outcome[Unit] =
    _ => Outcome(viewModel)

  val dots: List[Graphic[Material.Bitmap]] =
    List(
      Assets.redDot,
      Assets.greenDot,
      Assets.blueDot,
      Assets.yellowDot
    )

  val count: TextBox =
    TextBox("", 640, 20).alignCenter
      .withFontSize(Pixels(12))
      .withColor(RGBA.White)

  def present(context: FrameContext[Unit], model: Model, viewModel: Unit): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        model.particles.map { p =>
          dots(p.color).moveTo(p.x, p.y).scaleBy(p.scale, p.scale)
        } ++ List(
          count.withText(s"count: ${model.particles.length}")
        )
      )
    )

opaque type Model = (Int, List[Particle])
object Model:
  def empty: Model = (0, Nil)

  extension (m: Model)
    def color: Int                = m._1
    def particles: List[Particle] = m._2

    def spawn(context: FrameContext[Unit], count: Int): Model =
      (
        m._1,
        (0 until count).toList.map { _ =>
          Particle(
            context.mouse.position.x,
            context.mouse.position.y,
            context.dice.rollDouble * 2.0 - 1.0,
            context.dice.rollDouble * 2.0,
            m._1,
            context.dice.rollDouble * 0.5 + 0.5
          )
        } ++ m._2
      )

    def update: Model =
      (
        (m._1 + 1) % 4,
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

final case class Particle(x: Int, y: Int, fx: Double, fy: Double, color: Int, scale: Double)
