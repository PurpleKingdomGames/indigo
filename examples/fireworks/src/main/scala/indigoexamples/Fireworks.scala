package indigoexamples

import indigo._
import indigoextras.subsystems.FPSCounter

import indigoexamples.automata.LaunchPadAutomata
import indigoexamples.automata.RocketAutomata
import indigoexamples.automata.TrailAutomata
import indigoexamples.automata.FlareAutomata
import indigoexamples.model.{Projectiles, LaunchPad}
import indigoextras.geometry.Vertex
import indigoextras.subsystems.AutomataEvent

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object Fireworks extends IndigoDemo[Vertex => Point, FireworksStartupData, FireworksModel, Unit] {

  val targetFPS: Int     = 60
  val magnification: Int = 3

  def boot(flags: Map[String, String]): BootResult[Vertex => Point] = {
    val config =
      defaultGameConfig
        .withFrameRate(targetFPS)
        .withMagnification(magnification)
        .withViewport(GameViewport.at720p)

    val toScreenSpace: Vertex => Point =
      Projectiles.toScreenSpace(config.viewport.giveDimensions(magnification))

    BootResult(
      config,
      toScreenSpace
    ).withAssets(Assets.assets)
      .withFonts(FontDetails.fontInfo)
      .withSubSystems(
        FPSCounter(FontDetails.fontKey, Point(5, 5), targetFPS),
        LaunchPadAutomata.automata,
        RocketAutomata.automata(toScreenSpace),
        TrailAutomata.automata,
        FlareAutomata.automata(toScreenSpace)
      )
  }

  def launchFireworks(dice: Dice, toScreenSpace: Vertex => Point): List[AutomataEvent.Spawn] =
    List.fill(dice.roll(5) + 5)(
      LaunchPadAutomata.spawnEvent(
        LaunchPad.generateLaunchPad(dice),
        toScreenSpace
      )
    )

  def setup(toScreenSpace: Vertex => Point, assetCollection: AssetCollection, dice: Dice): Startup[StartupErrors, FireworksStartupData] =
    Startup.Success(
      FireworksStartupData(toScreenSpace)
    )

  def initialModel(startupData: FireworksStartupData): FireworksModel =
    FireworksModel(startupData.toScreenSpace)

  def initialViewModel(startupData: FireworksStartupData, model: FireworksModel): Unit =
    ()

  def updateModel(context: FrameContext[FireworksStartupData], model: FireworksModel): GlobalEvent => Outcome[FireworksModel] = {
    case KeyboardEvent.KeyUp(Keys.SPACE) =>
      Outcome(model, launchFireworks(context.dice, model.toScreenSpace))

    case _ =>
      Outcome(model)
  }

  def updateViewModel(context: FrameContext[FireworksStartupData], model: FireworksModel, viewModel: Unit): Outcome[Unit] =
    Outcome(())

  def present(context: FrameContext[FireworksStartupData], model: FireworksModel, viewModel: Unit): SceneUpdateFragment =
    SceneUpdateFragment.empty

}

final case class FireworksStartupData(toScreenSpace: Vertex => Point)
final case class FireworksModel(toScreenSpace: Vertex => Point)
