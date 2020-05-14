package indigoexamples

import indigo._
import indigogame._
import indigoexts.ui._

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object AutomataExample extends IndigoDemo[Unit, Unit, Button] {

  val config: GameConfig = defaultGameConfig

  val assets: Set[AssetType] = Set(
    AssetType.Image(AssetName("graphics"), AssetPath("assets/graphics.png")),
    AssetType.Image(FontStuff.fontName, AssetPath("assets/boxy_font.png"))
  )

  val fonts: Set[FontInfo] = Set(FontStuff.fontInfo)

  val animations: Set[Animation] = Set()

  val subSystems: Set[SubSystem] =
    Set(Score.automataSubSystem(FontStuff.fontKey))

  def setup(assetCollection: AssetCollection, flags: Map[String, String]): Startup[StartupErrors, Unit] =
    Startup.Success(())

  def initialModel(startupData: Unit): Unit =
    ()

  def update(gameTime: GameTime, model: Unit, inputState: InputState, dice: Dice): GlobalEvent => Outcome[Unit] = {
    case _ =>
      Outcome(model)
  }

  def initialViewModel(startupData: Unit): Unit => Button =
    _ =>
      Button(
        buttonAssets = ButtonAssets(
          up = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 0, 16, 16),
          over = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 16, 16, 16),
          down = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 32, 16, 16)
        ),
        bounds = Rectangle(10, 10, 16, 16),
        depth = Depth(2)
      )

  def updateViewModel(gameTime: GameTime, model: Unit, viewModel: Button, inputState: InputState, dice: Dice): Outcome[Button] =
    viewModel.update(inputState.mouse).map { btn =>
      btn.withUpAction {
        List(Score.spawnEvent(Score.generateLocation(config, dice), dice))
      }
    }

  def present(gameTime: GameTime, model: Unit, viewModel: Button, inputState: InputState, boundaryLocator: BoundaryLocator): SceneUpdateFragment =
    SceneUpdateFragment(
      viewModel.draw,
      Text("click to win!", 30, 10, 1, FontStuff.fontKey)
    )

}
