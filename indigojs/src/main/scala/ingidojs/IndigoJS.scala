package ingidoexamples

import indigo._
import indigo.gameengine.GameEngine
import indigo.gameengine.StandardFrameProcessor

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

import scala.scalajs.js.annotation._

@JSExportTopLevel("Indigo")
object IndigoJS {

  /*
    config: GameConfig,
    configAsync: Future[Option[GameConfig]],
    assets: Set[AssetType],
    assetsAsync: Future[Set[AssetType]],
    fonts: Set[FontInfo],
    animations: Set[Animation],
    initialise: AssetCollection => Startup[StartupError, StartupData],
    initialModel: StartupData => GameModel,
    initialViewModel: StartupData => GameModel => ViewModel,
    frameProccessor: FrameProcessor[GameModel, ViewModel]
   */

  // GameEngine
  type StartupData      = Unit
  type StartupError     = StartupErrors
  type GameModel        = Unit
  type ViewModel        = Unit
  type Initialise       = AssetCollection => Startup[StartupError, StartupData]
  type InitialModel     = StartupData => GameModel
  type InitialViewModel = StartupData => GameModel => ViewModel

  private def fakeInitialise: Initialise             = _ => Startup.Success(())
  private def fakeInitialModel: InitialModel         = _ => ()
  private def fakeInitialViewModel: InitialViewModel = _ => _ => ()

  // StandardFrameProcessor
  type ModelUpdate     = (GameTime, GameModel, Dice) => GlobalEvent => Outcome[GameModel]
  type ViewModelUpdate = (GameTime, GameModel, ViewModel, FrameInputEvents, Dice) => Outcome[ViewModel]
  type ViewUpdate      = (GameTime, GameModel, ViewModel, FrameInputEvents) => SceneUpdateFragment

  private def fakeModelUpdate: ModelUpdate         = (_, _, _) => _ => Outcome(())
  private def fakeViewModelUpdate: ViewModelUpdate = (_, _, _, _, _) => Outcome(())
  private def fakeViewUpdate: ViewUpdate           = (_, _, _, _) => SceneUpdateFragment.empty

  private def indigoGame(config: GameConfig): GameEngine[StartupData, StartupErrors, GameModel, ViewModel] = {

    val frameProcessor: StandardFrameProcessor[GameModel, ViewModel] =
      StandardFrameProcessor(
        fakeModelUpdate,
        fakeViewModelUpdate,
        fakeViewUpdate
      )

    new GameEngine[StartupData, StartupErrors, GameModel, ViewModel](
      config,
      Future(None),
      Set(),
      Future(Set()),
      Set(),
      Set(),
      fakeInitialise,
      fakeInitialModel,
      fakeInitialViewModel,
      frameProcessor
    )
  }

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  @JSExport
  def init(width: Int, height: Int, clearColor: ClearColorJS): Unit =
    indigoGame(
      GameConfig.default
        .withViewport(width, height)
        .withClearColor(clearColor.toClearColor)
    ).start()

}

@JSExportTopLevel("ClearColor")
final class ClearColorJS(r: Double, g: Double, b: Double, a: Double) {
  def toClearColor: ClearColor =
    ClearColor(r, g, b, a)
}

/*
package indigoexts.entry

import indigo._
import indigo.gameengine.GameEngine
import indigo.gameengine.StandardFrameProcessor
import indigoexts.subsystems.SubSystem
import indigoexts.subsystems.SubSystemsRegister

import scala.concurrent.Future

// Using Scala.js, so this is just to make the compiler happy.
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * A trait representing a minimal set of functions to get your game running
 * @tparam StartupData The class type representing your successful startup data
 * @tparam Model The class type representing your game's model
 * @tparam ViewModel The class type representing your game's view model
 */
trait IndigoGameBasic[StartupData, Model, ViewModel] {

  val config: GameConfig

  val assets: Set[AssetType]

  val fonts: Set[FontInfo]

  val animations: Set[Animation]

  val subSystems: Set[SubSystem]

  def setup(assetCollection: AssetCollection): Startup[StartupErrors, StartupData]

  def initialModel(startupData: StartupData): Model

  def update(gameTime: GameTime, model: Model, dice: Dice): GlobalEvent => Outcome[Model]

  def initialViewModel(startupData: StartupData): Model => ViewModel

  def updateViewModel(gameTime: GameTime, model: Model, viewModel: ViewModel, frameInputEvents: FrameInputEvents, dice: Dice): Outcome[ViewModel]

  def present(gameTime: GameTime, model: Model, viewModel: ViewModel, frameInputEvents: FrameInputEvents): SceneUpdateFragment

  private def indigoGame: GameEngine[StartupData, StartupErrors, GameWithSubSystems[Model], ViewModel] = {

    val frameProcessor: StandardFrameProcessor[GameWithSubSystems[Model], ViewModel] =
      StandardFrameProcessor(
        GameWithSubSystems.update(update),
        GameWithSubSystems.updateViewModel(updateViewModel),
        (gameTime: GameTime, model: GameWithSubSystems[Model], viewModel: ViewModel, frameInputEvents: FrameInputEvents) =>
          GameWithSubSystems.present(present)(gameTime, model, viewModel, frameInputEvents)
      )

    new GameEngine[StartupData, StartupErrors, GameWithSubSystems[Model], ViewModel](
      config,
      Future(None),
      assets,
      Future(Set()),
      fonts,
      animations,
      (ac: AssetCollection) => setup(ac),
      (sd: StartupData) => GameWithSubSystems(initialModel(sd), SubSystemsRegister().add(subSystems.toList)),
      (sd: StartupData) => (m: GameWithSubSystems[Model]) => initialViewModel(sd)(m.model),
      frameProcessor
    )
  }

  def main(args: Array[String]): Unit =
    indigoGame.start()

}
 */
