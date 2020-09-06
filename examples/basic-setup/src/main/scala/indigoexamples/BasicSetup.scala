package indigoexamples

import indigo._

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object BasicSetup extends IndigoDemo[Unit, MyStartUpData, MyGameModel, MyViewModel] {

  val eventFilters: EventFilters = EventFilters.Default

  def boot(flags: Map[String, String]): BootResult[Unit] =
    BootResult.noData(
      defaultGameConfig
        .withClearColor(ClearColor.fromHexString("0xAA191E"))
    )

  def setup(bootData: Unit, assetCollection: AssetCollection, dice: Dice): Startup[MyStartUpData] =
    Startup.Success(MyStartUpData())

  def initialModel(startupData: MyStartUpData): MyGameModel =
    MyGameModel()

  def initialViewModel(startupData: MyStartUpData, model: MyGameModel): MyViewModel = MyViewModel()

  def updateModel(context: FrameContext[MyStartUpData], model: MyGameModel): GlobalEvent => Outcome[MyGameModel] = _ => Outcome(model)

  def updateViewModel(context: FrameContext[MyStartUpData], model: MyGameModel, viewModel: MyViewModel): GlobalEvent => Outcome[MyViewModel] =
    _ => Outcome(viewModel)

  def present(context: FrameContext[MyStartUpData], model: MyGameModel, viewModel: MyViewModel): SceneUpdateFragment =
    noRender
}

// What does your game need to start? E.g. Parsing a level description file
final case class MyStartUpData()

// Your game model is anything you like!
final case class MyGameModel()

// Your view model is also ...anything you like!
final case class MyViewModel()
