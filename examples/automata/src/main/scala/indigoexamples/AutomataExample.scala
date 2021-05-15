package indigoexamples

import indigo._
import indigoextras.ui._

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object AutomataExample extends IndigoDemo[Size, Size, Unit, ViewModel] {

  val eventFilters: EventFilters = EventFilters.Permissive

  def boot(flags: Map[String, String]): Outcome[BootResult[Size]] =
    Outcome {
      val config = defaultGameConfig

      BootResult(
        config,
        config.viewport.size
      ).withAssets(
        AssetType.Image(AssetName("graphics"), AssetPath("assets/graphics.png")),
        AssetType.Image(FontStuff.fontName, AssetPath("assets/boxy_font.png"))
      ).withFonts(FontStuff.fontInfo)
        .withSubSystems(Score.automataSubSystem(FontStuff.fontKey))
    }

  def setup(bootData: Size, assetCollection: AssetCollection, dice: Dice): Outcome[Startup[Size]] =
    Outcome(Startup.Success(bootData))

  def initialModel(startupData: Size): Outcome[Unit] =
    Outcome(())

  def initialViewModel(startupData: Size, model: Unit): Outcome[ViewModel] =
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

  def updateModel(context: FrameContext[Size], model: Unit): GlobalEvent => Outcome[Unit] = { case _ =>
    Outcome(model)
  }

  def updateViewModel(context: FrameContext[Size], model: Unit, viewModel: ViewModel): GlobalEvent => Outcome[ViewModel] = {
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

  def present(context: FrameContext[Size], model: Unit, viewModel: ViewModel): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        viewModel.button.draw,
        Text("click to win!", 30, 10, 1, FontStuff.fontKey, Material.Bitmap(FontStuff.fontName))
      )
    )

}

final case class ViewModel(button: Button, viewportSize: Size) {
  def withButton(btn: Button): ViewModel =
    this.copy(button = btn)
}
