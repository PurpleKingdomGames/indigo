package indigoexamples

import indigo._
import indigoextras.ui._

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object AudioExample extends IndigoDemo[Unit, Unit, Unit, Button] {

  val eventFilters: EventFilters = EventFilters.Default

  def boot(flags: Map[String, String]): Outcome[BootResult[Unit]] =
    Outcome {
      BootResult
        .noData(defaultGameConfig)
        .withAssets(
          AssetType.Image(AssetName("graphics"), AssetPath("assets/graphics.png")),
          AssetType.Audio(AssetName("bounce"), AssetPath("assets/RetroGameJump.mp3")),
          AssetType.Audio(AssetName("music"), AssetPath("assets/march_of_steampunk.mp3"))
        )
    }

  def setup(bootData: Unit, assetCollection: AssetCollection, dice: Dice): Outcome[Startup[Unit]] =
    Outcome(Startup.Success(()))

  def initialModel(startupData: Unit): Outcome[Unit] =
    Outcome(())

  def initialViewModel(startupData: Unit, model: Unit): Outcome[Button] =
    Outcome(
      Button(
        buttonAssets = ButtonAssets(
          up = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 0, 16, 16),
          over = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 16, 16, 16),
          down = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 32, 16, 16)
        ),
        bounds = Rectangle(10, 10, 16, 16),
        depth = Depth(2)
      ).withUpActions(PlaySound(AssetName("bounce"), Volume.Max))
    )

  def updateModel(context: FrameContext[Unit], model: Unit): GlobalEvent => Outcome[Unit] = {
    case _ =>
      Outcome(model)
  }

  def updateViewModel(context: FrameContext[Unit], model: Unit, viewModel: Button): GlobalEvent => Outcome[Button] = {
    case FrameTick =>
      viewModel.update(context.inputState.mouse)

    case _ =>
      Outcome(viewModel)
  }

  def present(context: FrameContext[Unit], model: Unit, viewModel: Button): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(viewModel.draw)
        .withAudio(
          SceneAudio(
            SceneAudioSource(BindingKey("My bg music"), PlaybackPattern.SingleTrackLoop(Track(AssetName("music"))))
          )
        )
    )
}

final case class MyGameModel(button: Button, count: Int)
