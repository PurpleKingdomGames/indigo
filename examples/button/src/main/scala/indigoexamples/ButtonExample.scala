package indigoexamples

import indigo._
import indigogame._
import indigoexts.ui._

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object ButtonExample extends IndigoDemo[Unit, MyGameModel, MyViewModel] {

  val buttonAssets: ButtonAssets =
    ButtonAssets(
      up = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 0, 16, 16),
      over = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 16, 16, 16),
      down = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 32, 16, 16)
    )

  val config: GameConfig = defaultGameConfig

  val assets: Set[AssetType] = Set(AssetType.Image(AssetName("graphics"), AssetPath("assets/graphics.png")))

  val fonts: Set[FontInfo] = Set()

  val animations: Set[Animation] = Set()

  val subSystems: Set[SubSystem] = Set()

  def setup(assetCollection: AssetCollection, flags: Map[String, String]): Startup[StartupErrors, Unit] =
    Startup.Success(())

  def initialModel(startupData: Unit): MyGameModel =
    MyGameModel(count = 0)

  def update(gameTime: GameTime, model: MyGameModel, inputState: InputState, dice: Dice): GlobalEvent => Outcome[MyGameModel] = {
    case MyButtonEvent =>
      val next = model.copy(count = model.count + 1)
      println("Count: " + next.count.toString)
      Outcome(next)

    case _ =>
      Outcome(model)
  }

  def initialViewModel(startupData: Unit): MyGameModel => MyViewModel =
    _ =>
      MyViewModel(
        button = Button(
          buttonAssets = buttonAssets,
          bounds = Rectangle(10, 10, 16, 16),
          depth = Depth(2)
        ).withUpAction {
          List(MyButtonEvent)
        }
      )

  def updateViewModel(gameTime: GameTime, model: MyGameModel, viewModel: MyViewModel, inputState: InputState, dice: Dice): Outcome[MyViewModel] =
    viewModel.button.update(inputState.mouse).map { btn =>
      viewModel.copy(button = btn)
    }

  def present(gameTime: GameTime, model: MyGameModel, viewModel: MyViewModel, inputState: InputState, boundaryLocator: BoundaryLocator): SceneUpdateFragment =
    SceneUpdateFragment(viewModel.button.draw)
}

final case class MyGameModel(count: Int)
final case class MyViewModel(button: Button)
case object MyButtonEvent extends GlobalEvent
