package indigoexamples

import indigo._
import indigoexts.entrypoint._
import indigoexts.ui._

object AudioExample extends IndigoGameBasic[Unit, MyGameModel, Unit] {

  val config: GameConfig = defaultGameConfig

  val assets: Set[AssetType] = Set(
    AssetType.Image(AssetName("graphics"), AssetPath("assets/graphics.png")),
    AssetType.Audio(AssetName("bounce"), AssetPath("assets/RetroGameJump.mp3")),
    AssetType.Audio(AssetName("music"), AssetPath("assets/march_of_steampunk.mp3"))
  )

  val fonts: Set[FontInfo] = Set()

  val animations: Set[Animation] = Set()

  val subSystems: Set[SubSystem] = Set()

  def setup(assetCollection: AssetCollection): Startup[StartupErrors, Unit] =
    Startup.Success(())

  def initialModel(startupData: Unit): MyGameModel =
    MyGameModel(
      button = Button(ButtonState.Up).withUpAction { () =>
        List(PlaySound(AssetName("bounce"), Volume.Max))
      },
      count = 0
    )

  def update(gameTime: GameTime, model: MyGameModel, inputState: InputState, dice: Dice): GlobalEvent => Outcome[MyGameModel] = {
    case e: ButtonEvent =>
      Outcome(
        model.copy(
          button = model.button.update(e)
        )
      )

    case _ =>
      Outcome(model)
  }

  def initialViewModel(startupData: Unit): MyGameModel => Unit = _ => ()

  def updateViewModel(gameTime: GameTime, model: MyGameModel, viewModel: Unit, inputState: InputState, dice: Dice): Outcome[Unit] =
    Outcome(())

  def present(gameTime: GameTime, model: MyGameModel, viewModel: Unit, inputState: InputState): SceneUpdateFragment =
    model.button
      .draw(
        bounds = Rectangle(10, 10, 16, 16),
        depth = Depth(2),
        inputState = inputState,
        buttonAssets = ButtonAssets(
          up = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 0, 16, 16),
          over = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 16, 16, 16),
          down = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 32, 16, 16)
        )
      )
      .toSceneUpdateFragment
      .withAudio(
        SceneAudio(
          SceneAudioSource(BindingKey("My bg music"), PlaybackPattern.SingleTrackLoop(Track(AssetName("music"))))
        )
      )
}

final case class MyGameModel(button: Button, count: Int)
