package indigoexamples

import indigo._
import indigogame._
import indigoexts.subsystems.fpscounter.FPSCounter

import indigoexamples.automata.LaunchPadAutomata
import indigoexamples.automata.RocketAutomata
import indigoexamples.automata.TrailAutomata
import indigoexamples.automata.FlareAutomata
import indigoexamples.model.{Projectiles, LaunchPad}
import indigoexts.geometry.Vertex
import indigoexts.subsystems.automata.AutomataEvent

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

  def setup(assetCollection: AssetCollection, flags: Map[String, String]): Startup[StartupErrors, Unit] =
    Startup.Success(())

  def initialModel(startupData: Unit): Unit =
    ()

  def update(gameTime: GameTime, model: Unit, inputState: InputState, dice: Dice): GlobalEvent => Outcome[Unit] = {
    case KeyboardEvent.KeyUp(Keys.SPACE) =>
      Outcome(model, launchFireworks(dice, toScreenSpace))

    case _ =>
      Outcome(model)
  }

  def initialViewModel(startupData: Unit): Unit => Unit =
    _ => ()

  def updateViewModel(gameTime: GameTime, model: Unit, viewModel: Unit, inputState: InputState, dice: Dice): Outcome[Unit] =
    Outcome(())

  def present(gameTime: GameTime, model: Unit, viewModel: Unit, inputState: InputState, boundaryLocator: BoundaryLocator): SceneUpdateFragment =
    SceneUpdateFragment.empty

}
