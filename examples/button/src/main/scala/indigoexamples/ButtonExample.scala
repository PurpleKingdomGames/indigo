package indigoexamples

import indigo._
import indigoextras.ui._

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object ButtonExample extends IndigoDemo[Unit, Unit, MyGameModel, MyViewModel] {

  val eventFilters: EventFilters = EventFilters.Default

  def boot(flags: Map[String, String]): BootResult[Unit] =
    BootResult
      .noData(defaultGameConfig)
      .withAssets(AssetType.Image(AssetName("graphics"), AssetPath("assets/graphics.png")))

  val buttonAssets: ButtonAssets =
    ButtonAssets(
      up = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 0, 16, 16),
      over = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 16, 16, 16),
      down = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 32, 16, 16)
    )

  def setup(bootData: Unit, assetCollection: AssetCollection, dice: Dice): Startup[Unit] =
    Startup.Success(())

  def initialModel(startupData: Unit): MyGameModel =
    MyGameModel(count = 0)

  def initialViewModel(startupData: Unit, model: MyGameModel): MyViewModel =
    MyViewModel(
      button = Button(
        buttonAssets = buttonAssets,
        bounds = Rectangle(10, 10, 16, 16),
        depth = Depth(2)
      ).withUpAction {
        List(MyButtonEvent)
      }
    )

  def updateModel(context: FrameContext[Unit], model: MyGameModel): GlobalEvent => Outcome[MyGameModel] = {
    case MyButtonEvent =>
      val next = model.copy(count = model.count + 1)
      println("Count: " + next.count.toString)
      Outcome(next)

    case _ =>
      Outcome(model)
  }

  def updateViewModel(context: FrameContext[Unit], model: MyGameModel, viewModel: MyViewModel): GlobalEvent => Outcome[MyViewModel] = {
    case FrameTick =>
      viewModel.button.update(context.inputState.mouse).map { btn =>
        viewModel.copy(button = btn)
      }

    case _ =>
      Outcome(viewModel)
  }

  def present(context: FrameContext[Unit], model: MyGameModel, viewModel: MyViewModel): SceneUpdateFragment =
    SceneUpdateFragment(viewModel.button.draw)
}

final case class MyGameModel(count: Int)
final case class MyViewModel(button: Button)
case object MyButtonEvent extends GlobalEvent
