package indigoexamples

import indigo._
import indigoexts.entrypoint._
import indigoexts.ui._

object ButtonExample extends IndigoGameBasic[Unit, MyGameModel, Unit] {

  val config: GameConfig = defaultGameConfig

  // We'll need some graphics.
  val assets: Set[AssetType] = Set(AssetType.Image("graphics", "assets/graphics.png"))

  val fonts: Set[FontInfo] = Set()

  val animations: Set[Animation] = Set()

  val subSystems: Set[SubSystem] = Set()

  def setup(assetCollection: AssetCollection): Startup[StartupErrors, Unit] =
    Startup.Success(())

  // Let's setup our button's initial state
  def initialModel(startupData: Unit): MyGameModel =
    MyGameModel(
      button = Button(ButtonState.Up).withUpAction { () =>
        List(MyButtonEvent) // On mouse release will emit this event.
      },
      count = 0
    )

  // Match on event type, forward ButtonEvents to all buttons! (they'll work out if it's for the right button)
  def update(gameTime: GameTime, model: MyGameModel, inputState: InputState, dice: Dice): GlobalEvent => Outcome[MyGameModel] = {
    case e: ButtonEvent =>
      Outcome(
        model.copy(
          button = model.button.update(e)
        )
      )

    case MyButtonEvent => // Our event is caught, updates the model and writes to the console.
      val next = model.copy(count = model.count + 1)
      println("Count: " + next.count.toString)
      Outcome(next)

    case _ =>
      Outcome(model)
  }

  def initialViewModel(startupData: Unit): MyGameModel => Unit = _ => ()

  def updateViewModel(gameTime: GameTime, model: MyGameModel, viewModel: Unit, inputState: InputState, dice: Dice): Outcome[Unit] =
    Outcome(())

  def present(gameTime: GameTime, model: MyGameModel, viewModel: Unit, inputState: InputState): SceneUpdateFragment = {
    val button: ButtonViewUpdate = model.button.draw(
      bounds = Rectangle(10, 10, 16, 16), // Where should the button be on the screen?
      depth = Depth(2),                   // At what depth?
      inputState = inputState,            // delegate events
      buttonAssets = ButtonAssets(        // We could cache the graphics much earlier
        up = Graphic(0, 0, 16, 16, 2, "graphics").withCrop(32, 0, 16, 16),
        over = Graphic(0, 0, 16, 16, 2, "graphics").withCrop(32, 16, 16, 16),
        down = Graphic(0, 0, 16, 16, 2, "graphics").withCrop(32, 32, 16, 16)
      )
    )

    button.toSceneUpdateFragment
  }
}

// We need a button in our model
final case class MyGameModel(button: Button, count: Int)
case object MyButtonEvent extends GlobalEvent
