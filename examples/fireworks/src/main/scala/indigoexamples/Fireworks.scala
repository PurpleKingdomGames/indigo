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
object Fireworks extends IndigoDemo[Unit, Unit, Unit] {

  val targetFPS: Int = 60

  val config: GameConfig =
    defaultGameConfig
      .withFrameRate(targetFPS)
      .withMagnification(3)
      .withViewport(GameViewport.at720p)

  val assets: Set[AssetType] =
    Assets.assets

  val fonts: Set[FontInfo] =
    Set(FontDetails.fontInfo)

  val animations: Set[Animation] =
    Set()

  val toScreenSpace: Vertex => Point =
    Projectiles.toScreenSpace(config.viewport.giveDimensions(3))

  def launchFireworks(dice: Dice, toScreenSpace: Vertex => Point): List[AutomataEvent.Spawn] =
    List.fill(dice.roll(5) + 5)(
      LaunchPadAutomata.spawnEvent(
        LaunchPad.generateLaunchPad(dice),
        toScreenSpace
      )
    )

  val subSystems: Set[SubSystem] =
    Set(
      FPSCounter.subSystem(FontDetails.fontKey, Point(5, 5), targetFPS),
      LaunchPadAutomata.automata,
      RocketAutomata.automata(toScreenSpace),
      TrailAutomata.automata,
      FlareAutomata.automata(toScreenSpace)
    )

  def setup(assetCollection: AssetCollection, dice: Dice, flags: Map[String, String]): Startup[StartupErrors, Unit] =
    Startup.Success(())

  def initialModel(startupData: Unit): Unit =
    ()

  def initialViewModel(startupData: Unit, model: Unit): Unit =
    ()

  def updateModel(context: FrameContext, model: Unit): GlobalEvent => Outcome[Unit] = {
    case KeyboardEvent.KeyUp(Keys.SPACE) =>
      Outcome(model, launchFireworks(context.dice, toScreenSpace))

    case _ =>
      Outcome(model)
  }

  def updateViewModel(context: FrameContext, model: Unit, viewModel: Unit): Outcome[Unit] =
    Outcome(())

  def present(context: FrameContext, model: Unit, viewModel: Unit): SceneUpdateFragment =
    SceneUpdateFragment.empty

}
