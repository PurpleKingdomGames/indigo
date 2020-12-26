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

  val eventFilters: EventFilters = EventFilters.Permissive

  val radioButtonGraphic: AssetName = AssetName("graphics")
  val backgroundGraphic: AssetName  = AssetName("background")

  def boot(flags: Map[String, String]): Outcome[BootResult[Unit]] =
    Outcome(
      BootResult
        .noData(defaultGameConfig)
        .withAssets(
          AssetType.Image(radioButtonGraphic, AssetPath("assets/radio-example.png")),
          AssetType.Image(backgroundGraphic, AssetPath("assets/background.png"))
        )
    )

  val buttonAssets: ButtonAssets =
    ButtonAssets(
      up = Graphic(0, 0, 16, 16, 2, Material.Textured(radioButtonGraphic)).withCrop(0, 0, 16, 16),
      over = Graphic(0, 0, 16, 16, 2, Material.Textured(radioButtonGraphic)).withCrop(16, 0, 16, 16),
      down = Graphic(0, 0, 16, 16, 2, Material.Textured(radioButtonGraphic)).withCrop(32, 0, 16, 16)
    )

  val background: Graphic =
    Graphic(0, 0, 66, 26, 3, Material.Textured(AssetName("background")))

  def setup(bootData: Unit, assetCollection: AssetCollection, dice: Dice): Outcome[Startup[Unit]] =
    Outcome(Startup.Success(()))

  def initialModel(startupData: Unit): Outcome[MyGameModel] =
    Outcome(MyGameModel(RGBA.Black))

  def initialViewModel(startupData: Unit, model: MyGameModel): Outcome[MyViewModel] =
    // Create three radio option buttons, each firing an event to tint the background differently
    // Group the radio buttons and present them using loaded graphics
    // Button option1 is selected initially
    Outcome(
      MyViewModel(
        RadioButtonGroup(buttonAssets, 16, 16)
          .withRadioButtons(
            RadioButton(Point(5, 5))
              .withSelectedActions(MyRadioButtonEvent(RGBA.Red))
              .selected,
            RadioButton(Point(25, 5)).withSelectedActions(MyRadioButtonEvent(RGBA.Green)),
            RadioButton(Point(45, 5)).withSelectedActions(MyRadioButtonEvent(RGBA.Blue))
          ),
        background
      )
    )

  def updateModel(context: FrameContext[Unit], model: MyGameModel): GlobalEvent => Outcome[MyGameModel] = {
    case MyRadioButtonEvent(newTint) =>
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
      viewModel.radioButtons.update(context.inputState.mouse).map { radioBtns =>
        viewModel.copy(radioButtons = radioBtns)
      }

    case _ =>
      Outcome(viewModel)
  }

  def present(context: FrameContext[Unit], model: MyGameModel, viewModel: MyViewModel): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(viewModel.radioButtons.draw, viewModel.background.withTint(model.tint))
    )
}

// The game model says how to tint the background
final case class MyGameModel(tint: RGBA)
// The view model contains the current state of the radio button group and the background graphic
final case class MyViewModel(radioButtons: RadioButtonGroup, background: Graphic)
// This event is fired when a new radio button option is selected
final case class MyRadioButtonEvent(tint: RGBA) extends GlobalEvent
