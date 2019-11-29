package indigoexamples

import indigo._
import indigo.gameengine.GameEngine
import indigo.gameengine.StandardFrameProcessor

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

import scala.scalajs.js.annotation._
import scala.scalajs.js

import indigojs.delegates.AssetTypeDelegate
import indigojs.delegates.GameConfigDelegate
import indigojs.delegates.FontInfoDelegate
import indigojs.delegates.AnimationDelegate

@JSExportTopLevel("Indigo")
object IndigoJS {

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

  private def indigoGame(
      config: GameConfig,
      assets: Set[AssetType],
      fonts: Set[FontInfo],
      animations: Set[Animation],
      initialise: AssetCollection => Startup[StartupError, StartupData],
      initialModel: StartupData => GameModel,
      initialViewModel: StartupData => GameModel => ViewModel
  ): GameEngine[StartupData, StartupErrors, GameModel, ViewModel] = {

    val frameProcessor: StandardFrameProcessor[GameModel, ViewModel] =
      StandardFrameProcessor(
        fakeModelUpdate,
        fakeViewModelUpdate,
        fakeViewUpdate
      )

    new GameEngine[StartupData, StartupErrors, GameModel, ViewModel](
      config,
      Future(None),
      assets,
      Future(Set()),
      fonts,
      animations,
      initialise,
      initialModel,
      initialViewModel,
      frameProcessor
    )
  }

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  @JSExport
  def init(
      config: GameConfigDelegate,
      assets: js.Array[AssetTypeDelegate],
      fonts: js.Array[FontInfoDelegate],
      animations: js.Array[AnimationDelegate]
  ): Unit =
    indigoGame(
      config = config.toInternal,
      assets = assets.map(_.toInternal).toSet,
      fonts = fonts.map(_.toInternal).toSet,
      animations = animations.map(_.toInternal).toSet,
      initialise = fakeInitialise,
      initialModel = fakeInitialModel,
      initialViewModel = fakeInitialViewModel
    ).start()

}
