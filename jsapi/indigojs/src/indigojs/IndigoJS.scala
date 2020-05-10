package indigojs

import indigo.gameengine.GameEngine
import indigogame.entry.StandardFrameProcessor

import scala.concurrent.Future

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js.annotation._
import scala.scalajs.js

import indigojs.delegates.AssetTypeDelegate
import indigojs.delegates.config.GameConfigDelegate
import indigojs.delegates.FontInfoDelegate
import indigojs.delegates.AnimationDelegate
import indigojs.delegates.StartUpDelegate

import indigo.platform.assets.AssetCollection

import indigo.shared.Startup
import indigo.shared.events.GlobalEvent
import indigo.shared.Outcome
import indigo.shared.dice.Dice
import indigo.shared.time.GameTime
import indigo.shared.events.InputState
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.datatypes.FontInfo
import indigo.shared.animation.Animation
import indigojs.delegates.OutcomeDelegate
import indigojs.delegates.SceneUpdateFragmentDelegate
import indigojs.delegates.GameTimeDelegate
import indigojs.delegates.DiceDelegate
import indigojs.delegates.InputStateDelegate
import indigojs.delegates.AssetCollectionDelegate
import indigojs.delegates.GlobalEventDelegate

@JSExportTopLevel("Indigo")
object IndigoJS {

  type StartupData      = js.Object
  type StartupError     = js.Array[String]
  type GameModel        = js.Object
  type ViewModel        = js.Object
  type Initialise       = js.Function2[AssetCollectionDelegate, Map[String, String], StartUpDelegate]
  type InitialModel     = js.Function1[StartupData, GameModel]
  type InitialViewModel = js.Function2[StartupData, GameModel, ViewModel]
  type ModelUpdate      = js.Function4[GameTimeDelegate, GameModel, InputStateDelegate, DiceDelegate, js.Function1[GlobalEventDelegate, OutcomeDelegate]]
  type ViewModelUpdate  = js.Function5[GameTimeDelegate, GameModel, ViewModel, InputStateDelegate, DiceDelegate, OutcomeDelegate]
  type ViewUpdate       = js.Function4[GameTimeDelegate, GameModel, ViewModel, InputStateDelegate, SceneUpdateFragmentDelegate]

  private def indigoGame(
      fonts: Set[FontInfo],
      animations: Set[Animation],
      initialise: AssetCollection => Map[String, String] => Startup[StartupError, StartupData],
      initialModel: StartupData => GameModel,
      initialViewModel: StartupData => GameModel => ViewModel,
      modelUpdate: (GameTime, GameModel, InputState, Dice) => GlobalEvent => Outcome[GameModel],
      viewModelUpdate: (GameTime, GameModel, ViewModel, InputState, Dice) => Outcome[ViewModel],
      viewUpdate: (GameTime, GameModel, ViewModel, InputState) => SceneUpdateFragment
  ): GameEngine[StartupData, StartupError, GameModel, ViewModel] = {

    val frameProcessor: StandardFrameProcessor[GameModel, ViewModel] =
      new StandardFrameProcessor(modelUpdate, viewModelUpdate, viewUpdate)

    new GameEngine[StartupData, StartupError, GameModel, ViewModel](
      fonts,
      animations,
      initialise,
      initialModel,
      initialViewModel,
      frameProcessor
    )
  }

  private def convertInitialise(f: Initialise): AssetCollection => Map[String, String] => Startup[StartupError, StartupData] =
    (ac: AssetCollection) => (flags: Map[String, String]) => f(new AssetCollectionDelegate(ac), flags).toInternal

  private def convertUpdateModel(f: ModelUpdate): (GameTime, GameModel, InputState, Dice) => GlobalEvent => Outcome[GameModel] =
    (gt, gm, is, d) =>
      e =>
        f(
          new GameTimeDelegate(gt),
          gm,
          new InputStateDelegate(is),
          new DiceDelegate(d)
        )(GlobalEventDelegate.fromGlobalEvent(e)).toInternal

  private def convertUpdateViewModel(f: ViewModelUpdate): (GameTime, GameModel, ViewModel, InputState, Dice) => Outcome[ViewModel] =
    (gt, gm, vm, is, d) =>
      f(
        new GameTimeDelegate(gt),
        gm,
        vm,
        new InputStateDelegate(is),
        new DiceDelegate(d)
      ).toInternal

  private def convertUpdateView(f: ViewUpdate): (GameTime, GameModel, ViewModel, InputState) => SceneUpdateFragment =
    (gt, gm, vm, is) =>
      f(
        new GameTimeDelegate(gt),
        gm,
        vm,
        new InputStateDelegate(is)
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
      fonts = fonts.map(_.toInternal).toSet,
      animations = animations.map(_.toInternal).toSet,
      initialise = convertInitialise(initialise),
      initialModel = initialModel,
      initialViewModel = initialViewModel.curried,
      modelUpdate = convertUpdateModel(updateModel),
      viewModelUpdate = convertUpdateViewModel(updateViewModel),
      viewUpdate = convertUpdateView(present)
    ).start(
      config.toInternal,
      Future(None),
      assets.map(_.toInternal).toSet,
      Future(Set())
    )(Map[String, String]())

}
