package com.example.jobs

import indigo._
import indigoextras.geometry.Vertex
import indigoextras.jobs.JobMarket
import indigoextras.jobs.JobMarketEvent

import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("IndigoGame")
object JobsExample extends IndigoDemo[Unit, StartupData, Model, Unit] {

  val eventFilters: EventFilters = EventFilters.Permissive

  def boot(flags: Map[String, String]): Outcome[BootResult[Unit]] =
    Outcome(
      BootResult
        .noData(
          GameConfig.default
            .withViewport(400, 400)
            .withClearColor(RGBA(0.0, 0.2, 0.0, 1.0))
            .withMagnification(2)
        )
        .withAssets(Assets.assets)
        .withFonts(Assets.fontInfo)
        .withSubSystems(JobMarket.subSystem)
    )

  def setup(bootData: Unit, assetCollection: AssetCollection, dice: Dice): Outcome[Startup[StartupData]] = {
    val treeData = (1 to (dice.roll(3) + 3)).toList.map { i =>
      TreeData(
        i,
        Vertex(
          Math.sin(dice.rollDouble * Radians.TAU.toDouble),
          Math.cos(dice.rollDouble * Radians.TAU.toDouble)
        ),
        dice.rollDouble
      )
    }

    Outcome(Startup.Success(StartupData(treeData)))
  }

  def initialModel(startupData: StartupData): Outcome[Model] =
    Outcome(Model.initialModel(startupData))

  def initialViewModel(startupData: StartupData, model: Model): Outcome[Unit] =
    Outcome(())

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

  def present(context: FrameContext[StartupData], model: Model, viewModel: Unit): Outcome[SceneUpdateFragment] =
    Outcome(View.present(model))
}

final case class StartupData(trees: List[TreeData])

final case class TreeData(index: Int, position: Vertex, growthRate: Double)
