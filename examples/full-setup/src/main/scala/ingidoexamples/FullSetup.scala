package ingidoexamples

import indigo._
import indigoexts.entrypoint._

import scala.scalajs.js.annotation.JSExportTopLevel

object FullSetup {

  val config: GameConfig =
    GameConfig(
      viewport = GameViewport(550, 400),
      frameRate = 60,
      clearColor = ClearColor.Black,
      magnification = 1
    )

  val assets: Set[AssetType] =
    Set(AssetType.Image("my image", "assets/graphics.png"))

  val setup: AssetCollection => Startup[MyStartUpError, MyStartupData] =
    _ => Startup.Success(MyStartupData())

  val initialModel: MyStartupData => MyGameModel =
    _ => MyGameModel()

  val updateModel: (GameTime, MyGameModel) => GlobalEvent => UpdatedModel[MyGameModel] =
    (_, model) => _ => UpdatedModel(model)

  val initialViewModel: (MyStartupData, MyGameModel) => MyViewModel = (_, _) => MyViewModel()

  val updateViewModel: (GameTime, MyGameModel, MyViewModel, FrameInputEvents) => UpdatedViewModel[MyViewModel] = (_, _, _, _) => UpdatedViewModel(MyViewModel())

  val renderer: (GameTime, MyGameModel, MyViewModel, FrameInputEvents) => SceneUpdateFragment =
    (_, _, _, _) => SceneUpdateFragment.empty

  @JSExportTopLevel("Example.main")
  def main(args: Array[String]): Unit =
    Indigo.game
      .withConfig(config)
      .withAssets(assets)
      .noFonts
      .noAnimations
      .noSubSystems
      .startUpGameWith(setup)
      .usingInitialModel(initialModel)
      .updateModelUsing(updateModel)
      .initialiseViewModelUsing(initialViewModel)
      .updateViewModelUsing(updateViewModel)
      .presentUsing(renderer)
      .start()

}

// Start up types - can be anything, but you must supply a way to render the
// error cases
final case class MyStartupData()
final case class MyStartUpError(errors: List[String])
object MyStartUpError {
  implicit val toReportable: ToReportable[MyStartUpError] =
    ToReportable.createToReportable(e => e.errors.mkString("\n"))
}

// Your game model is anything you like!
final case class MyGameModel()

// Your view model is also ...anything you like!
final case class MyViewModel()
