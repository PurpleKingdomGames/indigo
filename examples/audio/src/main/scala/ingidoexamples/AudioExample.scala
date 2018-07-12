package ingidoexamples

import com.purplekingdomgames.indigoexts.entry._
import com.purplekingdomgames.indigo.gameengine.assets.AssetCollection
import com.purplekingdomgames.indigo.gameengine.{GameTime, StartupErrors, events}
import com.purplekingdomgames.indigo.gameengine.events.{FrameInputEvents, PlaySound}
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.{BindingKey, Depth, FontInfo, Rectangle}
import com.purplekingdomgames.indigoexts.entry.IndigoGameBasic
import com.purplekingdomgames.indigoexts.ui._
import com.purplekingdomgames.shared.{AssetType, AudioAsset, GameConfig, ImageAsset}

object AudioExample extends IndigoGameBasic[Unit, MyGameModel, Unit] {

  val config: GameConfig = defaultGameConfig

  val assets: Set[AssetType] = Set(
    ImageAsset("graphics", "assets/graphics.png"),
    AudioAsset("bounce", "assets/RetroGameJump.mp3"),
    AudioAsset("music", "assets/march_of_steampunk.mp3")
  )

  val fonts: Set[FontInfo] = Set()

  val animations: Set[Animations] = Set()

  def setup(assetCollection: AssetCollection): Either[StartupErrors, Unit] =
    Right(())

  def initialModel(startupData: Unit): MyGameModel =
    MyGameModel(
      button = Button(ButtonState.Up).withUpAction { () =>
        Option(PlaySound("bounce", Volume.Max))
      },
      count = 0
    )

  def update(gameTime: GameTime, model: MyGameModel): events.GameEvent => MyGameModel = {
    case e: ButtonEvent =>
      model.copy(
        button = model.button.update(e)
      )

    case _ =>
      model
  }

  def initialViewModel: MyGameModel => Unit = _ => ()

  def updateViewModel(gameTime: GameTime, model: MyGameModel, viewModel: Unit, frameInputEvents: FrameInputEvents): Unit =
    ()

  def present(gameTime: GameTime, model: MyGameModel, viewModel: Unit, frameInputEvents: FrameInputEvents): SceneUpdateFragment =
    model.button
      .draw(
        bounds = Rectangle(10, 10, 16, 16),
        depth = Depth(2),
        frameInputEvents = frameInputEvents,
        buttonAssets = ButtonAssets(
          up = Graphic(0, 0, 16, 16, 2, "graphics").withCrop(32, 0, 16, 16),
          over = Graphic(0, 0, 16, 16, 2, "graphics").withCrop(32, 16, 16, 16),
          down = Graphic(0, 0, 16, 16, 2, "graphics").withCrop(32, 32, 16, 16)
        )
      )
      .toSceneUpdateFragment
      .withAudio(
        SceneAudio(
          SceneAudioSource(BindingKey("My bg music"), PlaybackPattern.SingleTrackLoop(Track("music")))
        )
      )
}

case class MyGameModel(button: Button, count: Int)
