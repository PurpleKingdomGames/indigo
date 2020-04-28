package indigoexamples

import indigo._
import indigoexts.entrypoint._
import indigoexts.ui._

object HttpExample extends IndigoGameBasic[Unit, Unit, Button] {

  val config: GameConfig = defaultGameConfig

  val assets: Set[AssetType] = Set(AssetType.Image(AssetName("graphics"), AssetPath("assets/graphics.png")))

  val fonts: Set[FontInfo] = Set()

  val animations: Set[Animation] = Set()

  val subSystems: Set[SubSystem] = Set()

  def setup(assetCollection: AssetCollection): Startup[StartupErrors, Unit] =
    Startup.Success(())

  def initialModel(startupData: Unit): Unit =
    ()

  def update(gameTime: GameTime, model: Unit, inputState: InputState, dice: Dice): GlobalEvent => Outcome[Unit] = {
    case HttpResponse(status, headers, body) =>
      println("Status code: " + status.toString)
      println("Headers: " + headers.map(p => p._1 + ": " + p._2).mkString(", "))
      println("Body: " + body.getOrElse("<EMPTY>"))
      Outcome(model)

    case HttpError =>
      println("Http error message")
      Outcome(model)

    case _ =>
      Outcome(model)
  }

  def initialViewModel(startupData: Unit): Unit => Button =
    _ =>
      Button(
        ButtonAssets(
          up = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 0, 16, 16),
          over = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 16, 16, 16),
          down = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 32, 16, 16)
        ),
        bounds = Rectangle(10, 10, 16, 16),
        depth = Depth(2)
      ).withUpAction {
        List(HttpRequest.GET("http://localhost:8080/ping"))
      }

  def updateViewModel(gameTime: GameTime, model: Unit, viewModel: Button, inputState: InputState, dice: Dice): Outcome[Button] =
    viewModel.update(inputState.mouse)

  def present(gameTime: GameTime, model: Unit, viewModel: Button, inputState: InputState): SceneUpdateFragment =
    viewModel.draw
}
