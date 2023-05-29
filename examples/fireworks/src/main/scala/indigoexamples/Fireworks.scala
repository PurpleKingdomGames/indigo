package indigoexamples

import indigo.*
import indigoexamples.automata.FlareAutomata
import indigoexamples.automata.LaunchPadAutomata
import indigoexamples.automata.RocketAutomata
import indigoexamples.automata.TrailAutomata
import indigoexamples.model.LaunchPad
import indigoexamples.model.Projectiles
import indigoextras.subsystems.AutomataEvent
import indigoextras.subsystems.FPSCounter

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object Fireworks extends IndigoDemo[Vertex => Point, FireworksStartupData, Unit, Unit] {

  val magnification: Int = 3

  /** Fairly severe. The model only gets one event and the view model is never run.
    */
  val eventFilters: EventFilters =
    EventFilters(
      {
        case e: KeyboardEvent.KeyUp =>
          Some(e)

        case _ =>
          None
      },
      _ => None
    )

  def boot(flags: Map[String, String]): Outcome[BootResult[Vertex => Point]] =
    Outcome {
      val config =
        defaultGameConfig
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
          FPSCounter(Point(5, 5)),
          LaunchPadAutomata.automata,
          RocketAutomata.automata(toScreenSpace),
          TrailAutomata.automata,
          FlareAutomata.automata(toScreenSpace)
        )
    }

  def launchFireworks(dice: Dice, toScreenSpace: Vertex => Point): Batch[AutomataEvent.Spawn] =
    Batch.fromList(
      List.fill(dice.roll(5) + 5)(
        LaunchPadAutomata.spawnEvent(
          LaunchPad.generateLaunchPad(dice),
          toScreenSpace
        )
      )
    )

  def setup(toScreenSpace: Vertex => Point, assetCollection: AssetCollection, dice: Dice): Outcome[Startup[FireworksStartupData]] =
    Outcome(
      Startup.Success(
        FireworksStartupData(toScreenSpace)
      )
    )

  def initialModel(startupData: FireworksStartupData): Outcome[Unit] =
    Outcome(())

  def initialViewModel(startupData: FireworksStartupData, model: Unit): Outcome[Unit] =
    Outcome(())

  def updateModel(context: FrameContext[FireworksStartupData], model: Unit): GlobalEvent => Outcome[Unit] = {
    case KeyboardEvent.KeyUp(Key.SPACE) =>
      Outcome(model, launchFireworks(context.dice, context.startUpData.toScreenSpace))

    case _ =>
      Outcome(model)
  }

  def updateViewModel(context: FrameContext[FireworksStartupData], model: Unit, viewModel: Unit): GlobalEvent => Outcome[Unit] =
    _ => Outcome(())

  def present(context: FrameContext[FireworksStartupData], model: Unit, viewModel: Unit): Outcome[SceneUpdateFragment] =
    Outcome(SceneUpdateFragment.empty)

}

final case class FireworksStartupData(toScreenSpace: Vertex => Point)
