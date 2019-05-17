package ingidoexamples

import indigo._
import indigoexts.entrypoint._
import indigoexts.subsystems.fpscounter.FPSCounter

import ingidoexamples.model.FireworksModel
import ingidoexamples.automata.FireworksAutomata
import ingidoexamples.automata.CrossAutomaton

object Fireworks extends IndigoGameBasic[Unit, FireworksModel, Unit] {

  val config: GameConfig = defaultGameConfig.withMagnification(2)

  val assets: Set[AssetType] = Assets.assets

  val fonts: Set[FontInfo] =
    Set(
      FontStuff.fontInfo
    )

  val animations: Set[Animation] = Set()

  val subSystems: Set[SubSystem] =
    Set(
      FPSCounter.subSystem(FontStuff.fontKey, Point(5, 5)),
      FireworksAutomata.subSystem
    )

  def setup(assetCollection: AssetCollection): Startup[StartupErrors, Unit] =
    Startup.Success(())

  def initialModel(startupData: Unit): FireworksModel =
    FireworksModel()

  def update(gameTime: GameTime, model: FireworksModel, dice: Dice): GlobalEvent => Outcome[FireworksModel] = {
    case e: MouseEvent.Click =>
      Outcome(model)
        .addGlobalEvents(CrossAutomaton.spawnAt(e.position))

    case _ =>
      Outcome(model)
  }

  def initialViewModel(startupData: Unit): FireworksModel => Unit = _ => ()

  def updateViewModel(gameTime: GameTime, model: FireworksModel, viewModel: Unit, frameInputEvents: FrameInputEvents, dice: Dice): Outcome[Unit] =
    Outcome(())

  def present(gameTime: GameTime, model: FireworksModel, viewModel: Unit, frameInputEvents: FrameInputEvents): SceneUpdateFragment =
    SceneUpdateFragment.empty

}
