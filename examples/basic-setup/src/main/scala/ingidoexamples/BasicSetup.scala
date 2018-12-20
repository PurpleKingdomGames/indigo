package ingidoexamples

import indigo._
import indigoexts.entrypoint._

object BasicSetup extends IndigoGameBasic[MyStartUpData, MyGameModel, MyViewModel] {

  val config: GameConfig = defaultGameConfig

  val assets: Set[AssetType] = Set()

  val fonts: Set[FontInfo] = Set()

  val animations: Set[Animations] = Set()

  val subSystems: Set[SubSystem] = Set()

  def setup(assetCollection: AssetCollection): Startup[StartupErrors, MyStartUpData] =
    Startup.Success(MyStartUpData())

  def initialModel(startupData: MyStartUpData): MyGameModel =
    MyGameModel()

  def update(gameTime: GameTime, model: MyGameModel): GlobalEvent => UpdatedModel[MyGameModel] = _ => UpdatedModel(model)

  def initialViewModel(startupData: MyStartUpData): MyGameModel => MyViewModel = _ => MyViewModel()

  def updateViewModel(gameTime: GameTime, model: MyGameModel, viewModel: MyViewModel, frameInputEvents: FrameInputEvents): UpdatedViewModel[MyViewModel] =
    UpdatedViewModel(viewModel)

  def present(gameTime: GameTime, model: MyGameModel, viewModel: MyViewModel, frameInputEvents: FrameInputEvents): SceneUpdateFragment =
    noRender
}

// What does your game need to start? E.g. Parsing a level description file
final case class MyStartUpData()

// Your game model is anything you like!
final case class MyGameModel()

// Your view model is also ...anything you like!
final case class MyViewModel()
