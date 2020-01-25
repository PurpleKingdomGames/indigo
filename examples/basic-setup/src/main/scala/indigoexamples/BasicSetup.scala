package indigoexamples

import indigo._
import indigoexts.entrypoint._

object BasicSetup extends IndigoGameBasic[MyStartUpData, MyGameModel, MyViewModel] {

  val config: GameConfig =
    defaultGameConfig
      .withClearColor(ClearColor.fromHexString("0xAA191E"))

  val assets: Set[AssetType] =
    Set()

  val fonts: Set[FontInfo] =
    Set()

  val animations: Set[Animation] =
    Set()

  val subSystems: Set[SubSystem] =
    Set()

  def setup(assetCollection: AssetCollection): Startup[StartupErrors, MyStartUpData] =
    Startup.Success(MyStartUpData())

  def initialModel(startupData: MyStartUpData): MyGameModel =
    MyGameModel()

  def update(gameTime: GameTime, model: MyGameModel, inputState: InputState, dice: Dice): GlobalEvent => Outcome[MyGameModel] = _ => Outcome(model)

  def initialViewModel(startupData: MyStartUpData): MyGameModel => MyViewModel = _ => MyViewModel()

  def updateViewModel(gameTime: GameTime, model: MyGameModel, viewModel: MyViewModel, inputState: InputState, dice: Dice): Outcome[MyViewModel] =
    Outcome(viewModel)

  def present(gameTime: GameTime, model: MyGameModel, viewModel: MyViewModel, inputState: InputState): SceneUpdateFragment =
    noRender
}

// What does your game need to start? E.g. Parsing a level description file
final case class MyStartUpData()

// Your game model is anything you like!
final case class MyGameModel()

// Your view model is also ...anything you like!
final case class MyViewModel()
