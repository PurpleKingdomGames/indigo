package com.example.sandbox

import indigo._
import indigoextras.jobs._

import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("IndigoGame")
object JobsExample extends IndigoDemo[Unit, Unit, Unit, Unit] {

  val eventFilters: EventFilters = EventFilters.Default

  def boot(flags: Map[String, String]): BootResult[Unit] =
    BootResult
      .noData(GameConfig.default)
      .withAssets(AssetType.Image(AssetName("dots"), AssetPath("assets/dots.png")))
      .withSubSystems(JobMarket.subSystem)

  def setup(bootData: Unit, assetCollection: AssetCollection, dice: Dice): Startup[Unit] =
    Startup.Success(())

  def initialModel(startupData: Unit): Unit =
    ()

  def initialViewModel(startupData: Unit, model: Unit): Unit =
    ()

  def updateModel(context: FrameContext[Unit], model: Unit): GlobalEvent => Outcome[Unit] =
    _ => Outcome(model)

  def updateViewModel(context: FrameContext[Unit], model: Unit, viewModel: Unit): GlobalEvent => Outcome[Unit] =
    _ => Outcome(viewModel)

  def present(context: FrameContext[Unit], model: Unit, viewModel: Unit): SceneUpdateFragment =
    SceneUpdateFragment.empty
}

// Bob is our NPC 'Actor'
final case class Bob()
object Bob {

  implicit val bobWorker: Worker[Bob, Unit] =
    new Worker[Bob, Unit] {

      def isJobComplete(actor: Bob): Job => Boolean =
        ???

      def onJobComplete(actor: Bob, context: Unit): Job => Outcome[List[Job]] =
        ???

      def workOnJob(gameTime: GameTime, actor: Bob, context: Unit): Job => (Job, Bob) =
        ???

      def generateJobs: () => List[Job] =
        ???

      def canTakeJob(actor: Bob): Job => Boolean =
        ???

    }

}
