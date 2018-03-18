package ingidoexamples

import com.purplekingdomgames.indigo._
import com.purplekingdomgames.indigo.gameengine.events
import com.purplekingdomgames.indigo.gameengine.events.HttpReceiveEvent.{HttpError, HttpResponse}
import com.purplekingdomgames.indigo.gameengine.events.HttpRequest
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.{Depth, Rectangle}
import com.purplekingdomgames.indigo.gameengine.scenegraph.{Graphic, SceneGraphUpdate}
import com.purplekingdomgames.indigoexts.ui._
import com.purplekingdomgames.shared.ImageAsset

object HttpExample extends IndigoGameBasic[Unit, MyGameModel] {

  val config: GameConfig = defaultGameConfig

  val assets: Set[AssetType] = Set(ImageAsset("graphics", "assets/graphics.png"))

  def setup(assetCollection: AssetCollection): Either[StartupErrors, Unit] =
    Right(())

  def initialModel(startupData: Unit): MyGameModel =
    MyGameModel(
      button = Button(ButtonState.Up).withUpAction { () =>
        Option(HttpRequest.GET("http://localhost:8080/ping"))
      },
      count = 0
    )

  def update(gameTime: GameTime, model: MyGameModel): events.GameEvent => MyGameModel = {
    case e: ButtonEvent =>
      model.copy(
        button = model.button.update(e)
      )

    case HttpResponse(status, headers, body) =>
      println("Status code: " + status)
      println("Headers: " + headers.map(p => p._1 + ": " + p._2).mkString(", "))
      println("Body: " + body.getOrElse("<EMPTY>"))
      model

    case HttpError =>
      println("Http error message")
      model

    case _ =>
      model
  }

  def render(gameTime: GameTime, model: MyGameModel, frameInputEvents: events.FrameInputEvents): SceneGraphUpdate = {
    val button: ButtonViewUpdate = model.button.draw(
      bounds = Rectangle(10, 10, 16, 16),
      depth = Depth(2),
      frameInputEvents = frameInputEvents,
      buttonAssets = ButtonAssets(
        up = Graphic(0, 0, 16, 16, 2, "graphics").withCrop(32, 0, 16, 16),
        over = Graphic(0, 0, 16, 16, 2, "graphics").withCrop(32, 16, 16, 16),
        down = Graphic(0, 0, 16, 16, 2, "graphics").withCrop(32, 32, 16, 16)
      )
    )

    SceneGraphUpdate(
      List(button.buttonGraphic),
      button.buttonEvents
    )
  }
}

// We need a button in our model
case class MyGameModel(button: Button, count: Int)