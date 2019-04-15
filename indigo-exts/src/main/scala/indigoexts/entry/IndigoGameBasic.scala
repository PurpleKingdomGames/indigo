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
      (sd: StartupData) => GameWithSubSystems(initialModel(sd), SubSystemsRegister(subSystems.toList)),
      (sd: StartupData) => (m: GameWithSubSystems[Model]) => initialViewModel(sd)(m.model),
      frameProcessor
    )
  }

  def main(args: Array[String]): Unit =
    indigoGame.start()

}

final class GameWithSubSystems[Model](val model: Model, val subSystemsRegister: SubSystemsRegister)
object GameWithSubSystems {
  import indigo.abstractions.syntax._

  def apply[Model](model: Model, subSystemsRegister: SubSystemsRegister): GameWithSubSystems[Model] =
    new GameWithSubSystems[Model](model, subSystemsRegister)

  def update[Model](
      modelUpdate: (GameTime, Model, Dice) => GlobalEvent => Outcome[Model]
  )(gameTime: GameTime, model: GameWithSubSystems[Model], dice: Dice): GlobalEvent => Outcome[GameWithSubSystems[Model]] =
    e =>
      (modelUpdate(gameTime, model.model, dice)(e), model.subSystemsRegister.update(gameTime, dice)(e))
        .map2((m, s) => GameWithSubSystems(m, s))

  def updateViewModel[Model, ViewModel](
      viewModelUpdate: (GameTime, Model, ViewModel, FrameInputEvents, Dice) => Outcome[ViewModel]
  )(gameTime: GameTime, model: GameWithSubSystems[Model], viewModel: ViewModel, frameInputEvents: FrameInputEvents, dice: Dice): Outcome[ViewModel] =
    viewModelUpdate(gameTime, model.model, viewModel, frameInputEvents, dice)

  def present[Model, ViewModel](
      viewPresent: (GameTime, Model, ViewModel, FrameInputEvents) => SceneUpdateFragment
  )(gameTime: GameTime, model: GameWithSubSystems[Model], viewModel: ViewModel, frameInputEvents: FrameInputEvents): SceneUpdateFragment =
    viewPresent(gameTime, model.model, viewModel, frameInputEvents) |+| model.subSystemsRegister.render(gameTime)
}
