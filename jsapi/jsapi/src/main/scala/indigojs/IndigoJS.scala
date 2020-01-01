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
import indigojs.delegates.OutcomeDelegate
import indigojs.delegates.SceneUpdateFragmentDelegate
import indigojs.delegates.GameTimeDelegate
import indigojs.delegates.DiceDelegate
import indigojs.delegates.FrameInputEventsDelegate

@JSExportTopLevel("Indigo")
object IndigoJS {

  type StartupData      = js.Object
  type StartupError     = js.Array[String]
  type GameModel        = js.Object
  type ViewModel        = js.Object
  type Initialise       = js.Function1[AssetCollection, StartUpDelegate]
  type InitialModel     = js.Function1[StartupData, GameModel]
  type InitialViewModel = js.Function2[StartupData, GameModel, ViewModel]
  type ModelUpdate      = js.Function3[GameTimeDelegate, GameModel, DiceDelegate, js.Function1[GlobalEvent, OutcomeDelegate]]
  type ViewModelUpdate  = js.Function5[GameTimeDelegate, GameModel, ViewModel, FrameInputEventsDelegate, DiceDelegate, OutcomeDelegate]
  type ViewUpdate       = js.Function4[GameTimeDelegate, GameModel, ViewModel, FrameInputEventsDelegate, SceneUpdateFragmentDelegate]

  private def indigoGame(
      config: GameConfig,
      assets: Set[AssetType],
      fonts: Set[FontInfo],
      animations: Set[Animation],
      initialise: AssetCollection => Startup[StartupError, StartupData],
      initialModel: StartupData => GameModel,
      initialViewModel: StartupData => GameModel => ViewModel,
      modelUpdate: (GameTime, GameModel, Dice) => GlobalEvent => Outcome[GameModel],
      viewModelUpdate: (GameTime, GameModel, ViewModel, FrameInputEvents, Dice) => Outcome[ViewModel],
      viewUpdate: (GameTime, GameModel, ViewModel, FrameInputEvents) => SceneUpdateFragment
  ): GameEngine[StartupData, StartupError, GameModel, ViewModel] = {

    val frameProcessor: StandardFrameProcessor[GameModel, ViewModel] =
      StandardFrameProcessor(modelUpdate, viewModelUpdate, viewUpdate)

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

  private def convertUpdateModel(f: ModelUpdate): (GameTime, GameModel, Dice) => GlobalEvent => Outcome[GameModel] =
    (gt, gm, d) =>
      e =>
        f(
          new GameTimeDelegate(gt),
          gm,
          new DiceDelegate(d)
        )(e).toInternal

  private def convertUpdateViewModel(f: ViewModelUpdate): (GameTime, GameModel, ViewModel, FrameInputEvents, Dice) => Outcome[ViewModel] =
    (gt, gm, vm, es, d) =>
      f(
        new GameTimeDelegate(gt),
        gm,
        vm,
        new FrameInputEventsDelegate(es),
        new DiceDelegate(d)
      ).toInternal

  private def convertUpdateView(f: ViewUpdate): (GameTime, GameModel, ViewModel, FrameInputEvents) => SceneUpdateFragment =
    (gt, gm, vm, es) =>
      f(
        new GameTimeDelegate(gt),
        gm,
        vm,
        new FrameInputEventsDelegate(es)
      ).toInternal

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  @JSExport
  def init(
      config: GameConfigDelegate,
      assets: js.Array[AssetTypeDelegate],
      fonts: js.Array[FontInfoDelegate],
      animations: js.Array[AnimationDelegate],
      initialise: Initialise,
      initialModel: InitialModel,
      initialViewModel: InitialViewModel,
      updateModel: ModelUpdate,
      updateViewModel: ViewModelUpdate,
      present: ViewUpdate
  ): Unit =
    indigoGame(
      config = config.toInternal,
      assets = assets.map(_.toInternal).toSet,
      fonts = fonts.map(_.toInternal).toSet,
      animations = animations.map(_.toInternal).toSet,
      initialise = convertInitialise(initialise),
      initialModel = initialModel,
      initialViewModel = initialViewModel.curried,
      modelUpdate = convertUpdateModel(updateModel),
      viewModelUpdate = convertUpdateViewModel(updateViewModel),
      viewUpdate = convertUpdateView(present)
    ).start()

}
