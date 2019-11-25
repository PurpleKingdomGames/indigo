package indigoexamples

import indigo._
import indigoexts.entrypoint._
import indigoexts.uicomponents._
import indigoexts.subsystems.fpscounter.FPSCounter

import indigoexamples.model.FireworksModel
import indigoexamples.automata.LaunchPadAutomata
import indigoexamples.automata.RocketAutomata
import indigoexamples.automata.TrailAutomata
import indigoexamples.automata.FlareAutomata
import indigoexamples.model.Projectiles
import indigoexts.geometry.Vertex

object Fireworks extends IndigoGameBasic[Unit, FireworksModel, Unit] {

  val config: GameConfig =
    defaultGameConfig
      .withFrameRate(30)
      .withMagnification(2)
      .withViewport(GameViewport.at720p)

  val assets: Set[AssetType] =
    Assets.assets

  val fonts: Set[FontInfo] =
    Set(
      FontStuff.fontInfo
    )

  val animations: Set[Animation] =
    Set()

  val toScreenSpace: Vertex => Point =
    Projectiles.toScreenSpace(config.viewport.giveDimensions(3))

  val subSystems: Set[SubSystem] =
    Set(
      FPSCounter.subSystem(FontStuff.fontKey, Point(5, 40)),
      LaunchPadAutomata.automata,
      RocketAutomata.automata(toScreenSpace),
      TrailAutomata.automata,
      FlareAutomata.automata(toScreenSpace)
    )

  def setup(assetCollection: AssetCollection): Startup[StartupErrors, Unit] =
    Startup.Success(())

  def initialModel(startupData: Unit): FireworksModel =
    FireworksModel.initialModel

  def update(gameTime: GameTime, model: FireworksModel, dice: Dice): GlobalEvent => Outcome[FireworksModel] = {
    case FrameTick =>
      model.update(
        dice,
        toScreenSpace
      )

    case e: ButtonEvent =>
      Outcome(
        model.copy(
          launchButton = model.launchButton.update(e)
        )
      )

    case _ =>
      Outcome(model)
  }

  def initialViewModel(startupData: Unit): FireworksModel => Unit =
    _ => ()

  def updateViewModel(gameTime: GameTime, model: FireworksModel, viewModel: Unit, frameInputEvents: FrameInputEvents, dice: Dice): Outcome[Unit] =
    Outcome(())

  def present(gameTime: GameTime, model: FireworksModel, viewModel: Unit, frameInputEvents: FrameInputEvents): SceneUpdateFragment =
    LaunchButton.present(model.launchButton, frameInputEvents) |+| SceneUpdateFragment.empty.withGameLayerMagnification(3)

}
