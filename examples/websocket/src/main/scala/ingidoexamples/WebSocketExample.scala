package ingidoexamples

import indigo._
import indigoexts.entrypoint._
import indigoexts.ui._

/*
Two examples in server project:
a) ws://localhost:8080/ws      // Server sends a Ping! every second
b) ws://localhost:8080/wsecho  // Server echos back whatever it is sent.

So we want a button that send a message to 2) and outputs to the console

We also want to establish a connection on startup that repeated writes 1)'s
Ping! to the console.
 */
object WebSocketExample extends IndigoGameBasic[MySetupData, MyGameModel, Unit] {

  val config: GameConfig = defaultGameConfig

  val assets: Set[AssetType] = Set(AssetType.Image("graphics", "assets/graphics.png"))

  val fonts: Set[FontInfo] = Set()

  val animations: Set[Animations] = Set()

  def setup(assetCollection: AssetCollection): Either[StartupErrors, MySetupData] =
    Right(
      MySetupData(
        pingSocket = WebSocketConfig(
          id = WebSocketId("ping"),
          address = "ws://localhost:8080/ws"
        ),
        echoSocket = WebSocketConfig(
          id = WebSocketId("echo"),
          address = "ws://localhost:8080/wsecho"
        )
      )
    )

  def initialModel(startupData: MySetupData): MyGameModel =
    MyGameModel(
      ping = Button(ButtonState.Up).withUpAction { () =>
        Option(WebSocketEvent.ConnectOnly(startupData.pingSocket))
      },
      echo = Button(ButtonState.Up).withUpAction { () =>
        Option(WebSocketEvent.Send("Hello!", startupData.echoSocket))
      },
      count = 0
    )

  def update(gameTime: GameTime, model: MyGameModel): GameEvent => MyGameModel = {
    case e: ButtonEvent =>
      model.copy(
        ping = model.ping.update(e),
        echo = model.echo.update(e)
      )

    case WebSocketEvent.Receive(WebSocketId("ping"), message) =>
      println("Message from Server: " + message)
      model

    case WebSocketEvent.Receive(WebSocketId("echo"), message) =>
      println("Server says you said: " + message)
      model

    case WebSocketEvent.Error(WebSocketId(id), message) =>
      println(s"Connection [$id] errored with: " + message)
      model

    case WebSocketEvent.Close(WebSocketId(id)) =>
      println(s"Connection [$id] closed.")
      model

    case _ =>
      model
  }

  def initialViewModel(startupData: MySetupData): MyGameModel => Unit = _ => ()

  def updateViewModel(gameTime: GameTime, model: MyGameModel, viewModel: Unit, frameInputEvents: FrameInputEvents): Unit =
    ()

  def present(gameTime: GameTime, model: MyGameModel, viewModel: Unit, frameInputEvents: FrameInputEvents): SceneUpdateFragment = {
    val pingButton: ButtonViewUpdate = model.ping.draw(
      bounds = Rectangle(10, 10, 16, 16),
      depth = Depth(2),
      frameInputEvents = frameInputEvents,
      buttonAssets = ButtonAssets(
        up = Graphic(0, 0, 16, 16, 2, "graphics").withCrop(32, 0, 16, 16),
        over = Graphic(0, 0, 16, 16, 2, "graphics").withCrop(32, 16, 16, 16),
        down = Graphic(0, 0, 16, 16, 2, "graphics").withCrop(32, 32, 16, 16)
      )
    )

    val echoButton: ButtonViewUpdate = model.echo.draw(
      bounds = Rectangle(10, 32, 16, 16),
      depth = Depth(2),
      frameInputEvents = frameInputEvents,
      buttonAssets = ButtonAssets(
        up = Graphic(0, 0, 16, 16, 2, "graphics").withCrop(32, 0, 16, 16),
        over = Graphic(0, 0, 16, 16, 2, "graphics").withCrop(32, 16, 16, 16),
        down = Graphic(0, 0, 16, 16, 2, "graphics").withCrop(32, 32, 16, 16)
      )
    )

    pingButton.toSceneUpdateFragment |+| echoButton.toSceneUpdateFragment
  }
}

case class MySetupData(pingSocket: WebSocketConfig, echoSocket: WebSocketConfig)

// We need a button in our model
case class MyGameModel(ping: Button, echo: Button, count: Int)
