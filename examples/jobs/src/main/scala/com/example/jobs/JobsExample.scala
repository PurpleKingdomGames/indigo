package com.example.jobs

import indigo._
import indigoextras.jobs.JobMarket
import indigoextras.geometry.Vertex

import scala.scalajs.js.annotation.JSExportTopLevel
import indigoextras.jobs.JobMarketEvent

@JSExportTopLevel("IndigoGame")
object JobsExample extends IndigoDemo[Unit, StartupData, Model, Unit] {

  val eventFilters: EventFilters = EventFilters.Default

  def boot(flags: Map[String, String]): BootResult[Unit] =
    BootResult
      .noData(
        GameConfig.default
          .withViewport(400, 400)
          .withClearColor(ClearColor.fromRGB(0.0, 0.2, 0.0))
          .withMagnification(2)
      )
      .withAssets(Assets.assets)
      .withFonts(Assets.fontInfo)
      .withSubSystems(JobMarket.subSystem)

  def setup(bootData: Unit, assetCollection: AssetCollection, dice: Dice): Startup[StartupData] = {
    val treeData = (1 to (dice.roll(3) + 3)).toList.map { i =>
      TreeData(
        i,
        Vertex(
          Math.sin(dice.rollDouble * Radians.TAU.value),
          Math.cos(dice.rollDouble * Radians.TAU.value)
        ),
        dice.rollDouble
      )
    }

    Startup.Success(StartupData(treeData))
  }

  def initialModel(startupData: StartupData): Model =
    Model.initialModel(startupData)

  def initialViewModel(startupData: StartupData, model: Model): Unit =
    ()

  def updateModel(context: FrameContext[StartupData], model: Model): GlobalEvent => Outcome[Model] = {
    case e @ FrameTick =>
      model.update(context.gameTime, context.dice)(e)

    case e: JobMarketEvent =>
      model.update(context.gameTime, context.dice)(e)

    case e @ DropWood(_) =>
      model.update(context.gameTime, context.dice)(e)

    case RemoveTree(index) =>
      model.removeTreeWithIndex(index)

    case RemoveWood(id) =>
      model.removeWoodWithId(id)

    case _ =>
      Outcome(model)
  }

  def updateViewModel(context: FrameContext[StartupData], model: Model, viewModel: Unit): GlobalEvent => Outcome[Unit] =
    _ => Outcome(viewModel)

  def present(context: FrameContext[StartupData], model: Model, viewModel: Unit): SceneUpdateFragment =
    View.present(model)
}

final case class StartupData(trees: List[TreeData])

final case class TreeData(index: Int, position: Vertex, growthRate: Double)
