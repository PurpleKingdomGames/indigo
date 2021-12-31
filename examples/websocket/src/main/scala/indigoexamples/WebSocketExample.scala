package indigoexamples

import indigo._
import indigoextras.ui._

import scala.scalajs.js.annotation._

/*
Two examples in server project:
a) ws://localhost:8080/ws      // Server sends a Ping! every second
b) ws://localhost:8080/wsecho  // Server echos back whatever it is sent.

So we want a button that send a message to 2) and outputs to the console

We also want to establish a connection on startup that repeated writes 1)'s
Ping! to the console.
 */
@JSExportTopLevel("IndigoGame")
object WebSocketExample extends IndigoDemo[Unit, MySetupData, Unit, MyViewModel] {

  val eventFilters: EventFilters = EventFilters.Permissive

  def boot(flags: Map[String, String]): Outcome[BootResult[Unit]] =
    Outcome(
      BootResult
        .noData(defaultGameConfig)
        .withAssets(AssetType.Image(AssetName("graphics"), AssetPath("assets/graphics.png")))
    )

  val buttonAssets: ButtonAssets =
    ButtonAssets(
      up = Graphic(0, 0, 16, 16, 2, Material.Bitmap(AssetName("graphics"))).withCrop(32, 0, 16, 16),
      over = Graphic(0, 0, 16, 16, 2, Material.Bitmap(AssetName("graphics"))).withCrop(32, 16, 16, 16),
      down = Graphic(0, 0, 16, 16, 2, Material.Bitmap(AssetName("graphics"))).withCrop(32, 32, 16, 16)
    )

  def setup(bootData: Unit, assetCollection: AssetCollection, dice: Dice): Outcome[Startup[MySetupData]] =
    Outcome(
      Startup.Success(
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
    )

  def initialModel(startupData: MySetupData): Outcome[Unit] =
    Outcome(())

  def initialViewModel(startupData: MySetupData, model: Unit): Outcome[MyViewModel] =
    Outcome(
      MyViewModel(
        ping = Button(
          buttonAssets = buttonAssets,
          bounds = Rectangle(10, 32, 16, 16),
          depth = Depth(2)
        ).withUpActions(WebSocketEvent.ConnectOnly(startupData.pingSocket)),
        echo = Button(
          buttonAssets = buttonAssets,
          bounds = Rectangle(10, 32, 16, 16),
          depth = Depth(2)
        ).withUpActions(WebSocketEvent.Send("Hello!", startupData.echoSocket))
      )
    )

  def updateModel(context: FrameContext[MySetupData], model: Unit): GlobalEvent => Outcome[Unit] = {
    case WebSocketEvent.Receive(WebSocketId("ping"), message) =>
      println("Message from Server: " + message)
      Outcome(model)

    case WebSocketEvent.Receive(WebSocketId("echo"), message) =>
      println("Server says you said: " + message)
      Outcome(model)

    case WebSocketEvent.Error(WebSocketId(id), message) =>
      println(s"Connection [$id] errored with: " + message)
      Outcome(model)

    case WebSocketEvent.Close(WebSocketId(id)) =>
      println(s"Connection [$id] closed.")
      Outcome(model)

    case _ =>
      Outcome(model)
  }

  def updateViewModel(context: FrameContext[MySetupData], model: Unit, viewModel: MyViewModel): GlobalEvent => Outcome[MyViewModel] = {
    case FrameTick =>
      (viewModel.ping.update(context.inputState.mouse), viewModel.echo.update(context.inputState.mouse)).combine
        .map {
          case (ping, echo) =>
            MyViewModel(ping, echo)
        }

    case _ =>
      Outcome(viewModel)
  }

  def present(context: FrameContext[MySetupData], model: Unit, viewModel: MyViewModel): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        viewModel.ping.draw,
        viewModel.echo.draw
      )
    )
}

final case class MySetupData(pingSocket: WebSocketConfig, echoSocket: WebSocketConfig)

final case class MyViewModel(ping: Button, echo: Button)
