package indigoexamples

import indigo._
import indigoextras.ui._
import scala.scalajs.js.annotation._

/**
  * Presents an example of a radio button. The demo shows three options in a row that can be selected between.
  * On selecting different options, the background changes colour.
  */
@JSExportTopLevel("IndigoGame")
object RadioButtonExample extends IndigoDemo[Unit, Unit, MyGameModel, MyViewModel] {

  val eventFilters: EventFilters = EventFilters.Default

  val radioButtonGraphic: AssetName = AssetName("graphics")
  val backgroundGraphic: AssetName  = AssetName("background")

  def boot(flags: Map[String, String]): BootResult[Unit] =
    BootResult
      .noData(defaultGameConfig)
      .withAssets(
        AssetType.Image(radioButtonGraphic, AssetPath("assets/radio-example.png")),
        AssetType.Image(backgroundGraphic, AssetPath("assets/background.png"))
      )

  val buttonAssets: ButtonAssets =
    ButtonAssets(
      up = Graphic(0, 0, 16, 16, 2, Material.Textured(radioButtonGraphic)).withCrop(0, 0, 16, 16),
      over = Graphic(0, 0, 16, 16, 2, Material.Textured(radioButtonGraphic)).withCrop(16, 0, 16, 16),
      down = Graphic(0, 0, 16, 16, 2, Material.Textured(radioButtonGraphic)).withCrop(32, 0, 16, 16)
    )

  val background: Graphic = Graphic(0, 0, 66, 26, 3, Material.Textured(AssetName("background")))

  def setup(bootData: Unit, assetCollection: AssetCollection, dice: Dice): Startup[Unit] =
    Startup.Success(())

  def initialModel(startupData: Unit): MyGameModel =
    MyGameModel(0.0)

  def initialViewModel(startupData: Unit, model: MyGameModel): MyViewModel = {
    // Create three radio option buttons, each firing an event to tint the background differently
    val option1 = RadioButton(Point(5, 5)).withSelectedAction(() => List(MyButtonEvent(0.0)))
    val option2 = RadioButton(Point(25, 5)).withSelectedAction(() => List(MyButtonEvent(0.5)))
    val option3 = RadioButton(Point(45, 5)).withSelectedAction(() => List(MyButtonEvent(1.0)))

    // Group the radio buttons and present them using loaded graphics
    // Button option1 is selected initially
    MyViewModel(
      button = RadioButtonGroup(
        buttonAssets = buttonAssets,
        width = 16,
        height = 16,
        options = List(option1, option2, option3),
        depth = Depth(2),
        selected = Some(option1)
      ),
      background
    )
  }

  def updateModel(context: FrameContext[Unit], model: MyGameModel): GlobalEvent => Outcome[MyGameModel] = {
    case MyButtonEvent(newTint) =>
      Outcome(MyGameModel(newTint))

    case _ =>
      Outcome(model)
  }

  def updateViewModel(
      context: FrameContext[Unit],
      model: MyGameModel,
      viewModel: MyViewModel
  ): GlobalEvent => Outcome[MyViewModel] = {
    case FrameTick =>
      viewModel.button.update(context.inputState.mouse).map { btn =>
        viewModel.copy(button = btn)
      }

    case _ =>
      Outcome(viewModel)
  }

  def present(context: FrameContext[Unit], model: MyGameModel, viewModel: MyViewModel): SceneUpdateFragment =
    SceneUpdateFragment(viewModel.button.draw, viewModel.background.withTint(model.tint, 0.0, 0.0))
}

// The game model says how to tint the background
final case class MyGameModel(tint: Double)
// The view model contains the current state of the radio button group and the background graphic
final case class MyViewModel(button: RadioButtonGroup, background: Graphic)
// This event is fired when a new radio button option is selected
final case class MyButtonEvent(tint: Double) extends GlobalEvent
