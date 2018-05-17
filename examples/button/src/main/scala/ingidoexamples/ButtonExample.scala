package ingidoexamples

import com.purplekingdomgames.indigo._
import com.purplekingdomgames.indigo.gameengine.assets.AssetCollection
import com.purplekingdomgames.indigo.gameengine.{GameTime, StartupErrors, events}
import com.purplekingdomgames.indigo.gameengine.events.ViewEvent
import com.purplekingdomgames.indigo.gameengine.scenegraph.{Animations, Graphic, SceneUpdateFragment}
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.{Depth, FontInfo, Rectangle}
import com.purplekingdomgames.indigoexts.ui._
import com.purplekingdomgames.shared.{AssetType, GameConfig, ImageAsset}

object ButtonExample extends IndigoGameBasic[Unit, MyGameModel] {

  val config: GameConfig = defaultGameConfig

  // We'll need some graphics.
  val assets: Set[AssetType] = Set(ImageAsset("graphics", "assets/graphics.png"))

  val fonts: Set[FontInfo] = Set()

  val animations: Set[Animations] = Set()

  def setup(assetCollection: AssetCollection): Either[StartupErrors, Unit] =
    Right(())

  // Let's setup our button's initial state
  def initialModel(startupData: Unit): MyGameModel =
    MyGameModel(
      button = Button(ButtonState.Up).withUpAction { () =>
        Option(MyButtonEvent) // On mouse release will emit this event.
      },
      count = 0
    )

  // Match on event type, forward ButtonEvents to all buttons! (they'll work out if it's for the right button)
  def update(gameTime: GameTime, model: MyGameModel): events.GameEvent => MyGameModel = {
    case e: ButtonEvent =>
      model.copy(
        button = model.button.update(e)
      )

    case MyButtonEvent => // Our event is caught, updates the model and writes to the console.
      val next = model.copy(count = model.count + 1)
      println("Count: " + next.count)
      next

    case _ =>
      model
  }

  def present(gameTime: GameTime, model: MyGameModel, frameInputEvents: events.FrameInputEvents): SceneUpdateFragment = {
    val button: ButtonViewUpdate = model.button.draw(
      bounds = Rectangle(10, 10, 16, 16), // Where should the button be on the screen?
      depth = Depth(2), // At what depth?
      frameInputEvents = frameInputEvents, // delegate events
      buttonAssets = ButtonAssets( // We could cache the graphics much earlier
        up = Graphic(0, 0, 16, 16, 2, "graphics").withCrop(32, 0, 16, 16),
        over = Graphic(0, 0, 16, 16, 2, "graphics").withCrop(32, 16, 16, 16),
        down = Graphic(0, 0, 16, 16, 2, "graphics").withCrop(32, 32, 16, 16)
      )
    )

    button.toSceneUpdateFragment
  }
}

// We need a button in our model
case class MyGameModel(button: Button, count: Int)
case object MyButtonEvent extends ViewEvent
