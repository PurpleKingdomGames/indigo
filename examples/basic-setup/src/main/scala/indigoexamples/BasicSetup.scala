package indigoexamples

import indigo._

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object BasicSetup extends IndigoDemo[MyStartUpData, MyGameModel, MyViewModel] {

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

  def setup(assetCollection: AssetCollection, dice: Dice, flags: Map[String, String]): Startup[StartupErrors, MyStartUpData] =
    Startup.Success(MyStartUpData())

  def initialModel(startupData: MyStartUpData): MyGameModel =
    MyGameModel()

  def initialViewModel(startupData: MyStartUpData, model: MyGameModel): MyViewModel = MyViewModel()

  def updateModel(context: FrameContext, model: MyGameModel): GlobalEvent => Outcome[MyGameModel] = _ => Outcome(model)

  def updateViewModel(context: FrameContext, model: MyGameModel, viewModel: MyViewModel): Outcome[MyViewModel] =
    Outcome(viewModel)

  def present(context: FrameContext, model: MyGameModel, viewModel: MyViewModel): SceneUpdateFragment =
    noRender
}

// What does your game need to start? E.g. Parsing a level description file
final case class MyStartUpData()

// Your game model is anything you like!
final case class MyGameModel()

// Your view model is also ...anything you like!
final case class MyViewModel()
