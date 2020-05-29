package indigoexamples

import indigo._
import indigoextras.ui._

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object AudioExample extends IndigoDemo[Unit, Unit, Button] {

  val config: GameConfig = defaultGameConfig

  val assets: Set[AssetType] = Set(
    AssetType.Image(AssetName("graphics"), AssetPath("assets/graphics.png")),
    AssetType.Audio(AssetName("bounce"), AssetPath("assets/RetroGameJump.mp3")),
    AssetType.Audio(AssetName("music"), AssetPath("assets/march_of_steampunk.mp3"))
  )

  val fonts: Set[FontInfo] = Set()

  val animations: Set[Animation] = Set()

  val subSystems: Set[SubSystem] = Set()

  def setup(assetCollection: AssetCollection, dice: Dice, flags: Map[String, String]): Startup[StartupErrors, Unit] =
    Startup.Success(())

  def initialModel(startupData: Unit): Unit =
    ()

  def initialViewModel(startupData: Unit, model: Unit): Button =
    Button(
      buttonAssets = ButtonAssets(
        up = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 0, 16, 16),
        over = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 16, 16, 16),
        down = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 32, 16, 16)
      ),
      bounds = Rectangle(10, 10, 16, 16),
      depth = Depth(2)
    ).withUpAction {
      List(PlaySound(AssetName("bounce"), Volume.Max))
    }

  def updateModel(context: FrameContext, model: Unit): GlobalEvent => Outcome[Unit] = {
    case _ =>
      Outcome(model)
  }

  def updateViewModel(context: FrameContext, model: Unit, viewModel: Button): Outcome[Button] =
    viewModel.update(context.inputState.mouse)

  def present(context: FrameContext, model: Unit, viewModel: Button): SceneUpdateFragment =
    SceneUpdateFragment(viewModel.draw)
      .withAudio(
        SceneAudio(
          SceneAudioSource(BindingKey("My bg music"), PlaybackPattern.SingleTrackLoop(Track(AssetName("music"))))
        )
      )
}

final case class MyGameModel(button: Button, count: Int)
