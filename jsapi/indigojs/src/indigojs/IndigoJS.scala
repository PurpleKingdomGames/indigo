package indigojs

import indigo.gameengine.GameEngine
import indigo.entry.StandardFrameProcessor

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
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.datatypes.FontInfo
import indigo.shared.animation.Animation
import indigojs.delegates.OutcomeDelegate
import indigojs.delegates.SceneUpdateFragmentDelegate
import indigojs.delegates.FrameContextDelegate
import indigojs.delegates.DiceDelegate
import indigojs.delegates.AssetCollectionDelegate
import indigojs.delegates.GlobalEventDelegate
import indigo.shared.FrameContext

@JSExportTopLevel("Indigo")
object IndigoJS {

  type StartupData      = js.Object
  type StartupError     = js.Array[String]
  type GameModel        = js.Object
  type ViewModel        = js.Object
  type Initialise       = js.Function2[AssetCollectionDelegate, DiceDelegate, StartUpDelegate]
  type InitialModel     = js.Function1[StartupData, GameModel]
  type InitialViewModel = js.Function2[StartupData, GameModel, ViewModel]
  type ModelUpdate      = js.Function2[FrameContextDelegate[StartupData], GameModel, js.Function1[GlobalEventDelegate, OutcomeDelegate]]
  type ViewModelUpdate  = js.Function3[FrameContextDelegate[StartupData], GameModel, ViewModel, OutcomeDelegate]
  type ViewUpdate       = js.Function3[FrameContextDelegate[StartupData], GameModel, ViewModel, SceneUpdateFragmentDelegate]

  private def indigoGame(
      fonts: Set[FontInfo],
      animations: Set[Animation],
      initialise: AssetCollection => Dice => Startup[StartupError, StartupData],
      initialModel: StartupData => GameModel,
      initialViewModel: StartupData => GameModel => ViewModel,
      modelUpdate: (FrameContext[StartupData], GameModel) => GlobalEvent => Outcome[GameModel],
      viewModelUpdate: (FrameContext[StartupData], GameModel, ViewModel) => Outcome[ViewModel],
      viewUpdate: (FrameContext[StartupData], GameModel, ViewModel) => SceneUpdateFragment
  ): GameEngine[StartupData, StartupError, GameModel, ViewModel] = {

    val frameProcessor: StandardFrameProcessor[StartupData, GameModel, ViewModel] =
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

  private def convertInitialise(f: Initialise): AssetCollection => Dice => Startup[StartupError, StartupData] =
    (ac: AssetCollection) => (d: Dice) => f(new AssetCollectionDelegate(ac), new DiceDelegate(d)).toInternal

  private def convertUpdateModel(f: ModelUpdate): (FrameContext[StartupData], GameModel) => GlobalEvent => Outcome[GameModel] =
    (fc, gm) =>
      e =>
        f(
          new FrameContextDelegate(fc),
          gm
        )(GlobalEventDelegate.fromGlobalEvent(e)).toInternal

  private def convertUpdateViewModel(f: ViewModelUpdate): (FrameContext[StartupData], GameModel, ViewModel) => Outcome[ViewModel] =
    (fc, gm, vm) =>
      f(
        new FrameContextDelegate(fc),
        gm,
        vm
      ).toInternal

  private def convertUpdateView(f: ViewUpdate): (FrameContext[StartupData], GameModel, ViewModel) => SceneUpdateFragment =
    (fc, gm, vm) =>
      f(
        new FrameContextDelegate(fc),
        gm,
        vm
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
    )

}
