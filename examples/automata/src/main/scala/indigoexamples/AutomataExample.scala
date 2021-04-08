package indigoexamples

import indigo._
import indigoextras.ui._

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object AutomataExample extends IndigoDemo[Point, Point, Unit, ViewModel] {

  val eventFilters: EventFilters = EventFilters.Permissive

  def boot(flags: Map[String, String]): Outcome[BootResult[Point]] =
    Outcome {
      val config = defaultGameConfig

      BootResult(
        config,
        config.viewport.size
      ).withAssets(
          AssetType.Image(AssetName("graphics"), AssetPath("assets/graphics.png")),
          AssetType.Image(FontStuff.fontName, AssetPath("assets/boxy_font.png"))
        )
        .withFonts(FontStuff.fontInfo)
        .withSubSystems(Score.automataSubSystem(FontStuff.fontKey))
    }

  def setup(bootData: Point, assetCollection: AssetCollection, dice: Dice): Outcome[Startup[Point]] =
    Outcome(Startup.Success(bootData))

  def initialModel(startupData: Point): Outcome[Unit] =
    Outcome(())

  def initialViewModel(startupData: Point, model: Unit): Outcome[ViewModel] =
    Outcome(
      ViewModel(
        Button(
          buttonAssets = ButtonAssets(
            up = Graphic(0, 0, 16, 16, 2, Material.Bitmap(AssetName("graphics"))).withCrop(32, 0, 16, 16),
            over = Graphic(0, 0, 16, 16, 2, Material.Bitmap(AssetName("graphics"))).withCrop(32, 16, 16, 16),
            down = Graphic(0, 0, 16, 16, 2, Material.Bitmap(AssetName("graphics"))).withCrop(32, 32, 16, 16)
          ),
          bounds = Rectangle(10, 10, 16, 16),
          depth = Depth(2)
        ),
        startupData
      )
    )

  def updateModel(context: FrameContext[Point], model: Unit): GlobalEvent => Outcome[Unit] = {
    case _ =>
      Outcome(model)
  }

  def updateViewModel(context: FrameContext[Point], model: Unit, viewModel: ViewModel): GlobalEvent => Outcome[ViewModel] = {
    case FrameTick =>
      viewModel.button
        .update(context.inputState.mouse)
        .map { btn =>
          btn.withUpActions(Score.spawnEvent(Score.generateLocation(viewModel.viewportSize, context.dice), context.dice))
        }
        .map(viewModel.withButton)

    case _ =>
      Outcome(viewModel)
  }

  def present(context: FrameContext[Point], model: Unit, viewModel: ViewModel): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        viewModel.button.draw,
        Text("click to win!", 30, 10, 1, FontStuff.fontKey, Material.Bitmap(FontStuff.fontName))
      )
    )

}

final case class ViewModel(button: Button, viewportSize: Point) {
  def withButton(btn: Button): ViewModel =
    this.copy(button = btn)
}
