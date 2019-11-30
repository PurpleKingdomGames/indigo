package indigoexamples

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
import indigojs.delegates.StartUpDelegate

//Remove! Remove?
import indigo.platform.assets.AssetCollection
import indigo.shared.Startup
import indigo.shared.events.GlobalEvent
import indigo.shared.Outcome
import indigo.shared.dice.Dice
import indigo.shared.time.GameTime
import indigo.shared.events.FrameInputEvents
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.config.GameConfig
import indigo.shared.AssetType
import indigo.shared.datatypes.FontInfo
import indigo.shared.animation.Animation

@JSExportTopLevel("Indigo")
object IndigoJS {

  // GameEngine
  type StartupData      = js.Object
  type StartupError     = js.Array[String]
  type GameModel        = js.Object
  type ViewModel        = js.Object
  type Initialise       = js.Function1[AssetCollection, StartUpDelegate]
  type InitialModel     = js.Function1[StartupData, GameModel]
  type InitialViewModel = js.Function2[StartupData, GameModel, ViewModel]

  // StandardFrameProcessor
  type ModelUpdate     = (GameTime, GameModel, Dice) => GlobalEvent => Outcome[GameModel]
  type ViewModelUpdate = (GameTime, GameModel, ViewModel, FrameInputEvents, Dice) => Outcome[ViewModel]
  type ViewUpdate      = (GameTime, GameModel, ViewModel, FrameInputEvents) => SceneUpdateFragment

  private def fakeModelUpdate: ModelUpdate         = (_, m, _) => _ => Outcome(m)
  private def fakeViewModelUpdate: ViewModelUpdate = (_, _, vm, _, _) => Outcome(vm)
  private def fakeViewUpdate: ViewUpdate           = (_, _, _, _) => SceneUpdateFragment.empty

  private def indigoGame(
      config: GameConfig,
      assets: Set[AssetType],
      fonts: Set[FontInfo],
      animations: Set[Animation],
      initialise: AssetCollection => Startup[StartupError, StartupData],
      initialModel: StartupData => GameModel,
      initialViewModel: StartupData => GameModel => ViewModel
  ): GameEngine[StartupData, StartupError, GameModel, ViewModel] = {

    val frameProcessor: StandardFrameProcessor[GameModel, ViewModel] =
      StandardFrameProcessor(
        fakeModelUpdate,
        fakeViewModelUpdate,
        fakeViewUpdate
      )

    new GameEngine[StartupData, StartupError, GameModel, ViewModel](
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

  private def convertInitialise(f: Initialise): AssetCollection => Startup[StartupError, StartupData] =
    (ac: AssetCollection) => f(ac).toInternal

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  @JSExport
  def init(
      config: GameConfigDelegate,
      assets: js.Array[AssetTypeDelegate],
      fonts: js.Array[FontInfoDelegate],
      animations: js.Array[AnimationDelegate],
      initialise: Initialise,
      initialModel: InitialModel,
      initialViewModel: InitialViewModel
  ): Unit =
    indigoGame(
      config = config.toInternal,
      assets = assets.map(_.toInternal).toSet,
      fonts = fonts.map(_.toInternal).toSet,
      animations = animations.map(_.toInternal).toSet,
      initialise = convertInitialise(initialise),
      initialModel = initialModel,
      initialViewModel = initialViewModel.curried
    ).start()

}
