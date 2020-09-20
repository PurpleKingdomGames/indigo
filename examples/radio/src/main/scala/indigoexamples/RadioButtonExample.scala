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

  def boot(flags: Map[String, String]): BootResult[Unit] =
    BootResult
      .noData(defaultGameConfig)
      .withAssets(
        AssetType.Image(AssetName("graphics"), AssetPath("assets/radio-example.png")),
        AssetType.Image(AssetName("background"), AssetPath("assets/background.png"))
      )

  val buttonAssets: ButtonAssets =
    ButtonAssets(
      up = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(0, 0, 16, 16),
      over = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(16, 0, 16, 16),
      down = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 0, 16, 16)
    )

  val background: Graphic = Graphic(0, 0, 66, 26, 3, Material.Textured(AssetName("background")))

  def setup(bootData: Unit, assetCollection: AssetCollection, dice: Dice): Startup[Unit] =
    Startup.Success(())

  def initialModel(startupData: Unit): MyGameModel =
    MyGameModel(0.0)

  def initialViewModel(startupData: Unit, model: MyGameModel): MyViewModel =
    MyViewModel(
      button = RadioButton(
        buttonAssets = buttonAssets,
        positions = List(Point(5, 5), Point(25, 5), Point(45, 5)),
        width = 16,
        height = 16,
        depth = Depth(2),
        selected = Some(0)
      ).withSelectedAction { index =>
        List(MyButtonEvent(index))
      },
      background
    )

  def updateModel(context: FrameContext[Unit], model: MyGameModel): GlobalEvent => Outcome[MyGameModel] = {
    case MyButtonEvent(selection) =>
      val newTint = selection match {
        case 1 => 0.5
        case 2 => 1.0
        case _ => 0.0
      }
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

final case class MyGameModel(tint: Double)
final case class MyViewModel(button: RadioButton, background: Graphic)
final case class MyButtonEvent(selection: Int) extends GlobalEvent
