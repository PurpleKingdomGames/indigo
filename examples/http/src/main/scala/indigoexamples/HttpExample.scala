package indigoexamples

import indigo._
import indigoextras.ui._

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object HttpExample extends IndigoDemo[Unit, Unit, Unit, Button] {

  val eventFilters: EventFilters = EventFilters.Permissive

  def boot(flags: Map[String, String]): Outcome[BootResult[Unit]] =
    Outcome(
      BootResult
        .noData(defaultGameConfig)
        .withAssets(AssetType.Image(AssetName("graphics"), AssetPath("assets/graphics.png")))
    )

  def setup(bootData: Unit, assetCollection: AssetCollection, dice: Dice): Outcome[Startup[Unit]] =
    Outcome(Startup.Success(()))

  def initialModel(startupData: Unit): Outcome[Unit] =
    Outcome(())

  def initialViewModel(startupData: Unit, model: Unit): Outcome[Button] =
    Outcome(
      Button(
        ButtonAssets(
          up = Graphic(0, 0, 16, 16, 2, Material.Bitmap(AssetName("graphics"))).withCrop(32, 0, 16, 16),
          over = Graphic(0, 0, 16, 16, 2, Material.Bitmap(AssetName("graphics"))).withCrop(32, 16, 16, 16),
          down = Graphic(0, 0, 16, 16, 2, Material.Bitmap(AssetName("graphics"))).withCrop(32, 32, 16, 16)
        ),
        bounds = Rectangle(10, 10, 16, 16),
        depth = Depth(2)
      ).withUpActions(HttpRequest.GET("http://localhost:8080/ping"))
    )

  def updateModel(context: FrameContext[Unit], model: Unit): GlobalEvent => Outcome[Unit] = {
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

  def updateViewModel(context: FrameContext[Unit], model: Unit, viewModel: Button): GlobalEvent => Outcome[Button] = {
    case FrameTick =>
      viewModel.update(context.inputState.mouse)

    case _ =>
      Outcome(viewModel)
  }

  def present(context: FrameContext[Unit], model: Unit, viewModel: Button): Outcome[SceneUpdateFragment] =
    Outcome(SceneUpdateFragment(viewModel.draw))
}
