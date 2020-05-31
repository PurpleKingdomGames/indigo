package indigoexamples

import indigo._
import indigoextras.ui._

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object AutomataExample extends IndigoDemo[Unit, ViewportSize, Unit, ViewModel] {

  def parseFlags(flags: Map[String, String]): Unit = ()

  def config(flagData: Unit): GameConfig = defaultGameConfig

  def assets(flagData: Unit): Set[AssetType] = Set(
    AssetType.Image(AssetName("graphics"), AssetPath("assets/graphics.png")),
    AssetType.Image(FontStuff.fontName, AssetPath("assets/boxy_font.png"))
  )

  val fonts: Set[FontInfo] = Set(FontStuff.fontInfo)

  val animations: Set[Animation] = Set()

  val subSystems: Set[SubSystem] =
    Set(Score.automataSubSystem(FontStuff.fontKey))

  def setup(flagData: Unit, gameConfig: GameConfig, assetCollection: AssetCollection, dice: Dice): Startup[StartupErrors, ViewportSize] =
    Startup.Success(ViewportSize(gameConfig.viewport.width, gameConfig.viewport.height))

  def initialModel(startupData: ViewportSize): Unit =
    ()

  def initialViewModel(startupData: ViewportSize, model: Unit): ViewModel =
    ViewModel(
      Button(
        buttonAssets = ButtonAssets(
          up = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 0, 16, 16),
          over = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 16, 16, 16),
          down = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 32, 16, 16)
        ),
        bounds = Rectangle(10, 10, 16, 16),
        depth = Depth(2)
      ),
      startupData
    )

  def updateModel(context: FrameContext, model: Unit): GlobalEvent => Outcome[Unit] = {
    case _ =>
      Outcome(model)
  }

  def updateViewModel(context: FrameContext, model: Unit, viewModel: ViewModel): Outcome[ViewModel] =
    viewModel.button
      .update(context.inputState.mouse)
      .map { btn =>
        btn.withUpAction {
          List(Score.spawnEvent(Score.generateLocation(viewModel.viewportSize, context.dice), context.dice))
        }
      }
      .map(viewModel.withButton)

  def present(context: FrameContext, model: Unit, viewModel: ViewModel): SceneUpdateFragment =
    SceneUpdateFragment(
      viewModel.button.draw,
      Text("click to win!", 30, 10, 1, FontStuff.fontKey)
    )

}

final case class ViewportSize(width: Int, height: Int)
final case class ViewModel(button: Button, viewportSize: ViewportSize) {
  def withButton(btn: Button): ViewModel =
    this.copy(button = btn)
}
