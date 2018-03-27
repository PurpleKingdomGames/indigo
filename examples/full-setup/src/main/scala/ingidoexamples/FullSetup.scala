package ingidoexamples

import com.purplekingdomgames.indigo.Indigo
import com.purplekingdomgames.indigo.gameengine._
import com.purplekingdomgames.indigo.gameengine.assets.AssetCollection
import com.purplekingdomgames.indigo.gameengine.events.{FrameInputEvents, GameEvent}
import com.purplekingdomgames.indigo.gameengine.scenegraph.SceneGraphUpdate
import com.purplekingdomgames.shared._

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
    Set(ImageAsset("my image", "assets/graphics.png"))

  val setup: AssetCollection => Startup[MyStartUpError, MyStartupData] =
    _ => MyStartupData()

  val initialModel: MyStartupData => MyGameModel =
    _ => MyGameModel()

  val updateModel: (GameTime, MyGameModel) => GameEvent => MyGameModel =
    (_, model) => _ => model

  val renderer: (GameTime, MyGameModel, FrameInputEvents) => SceneGraphUpdate =
    (_, _, _) => SceneGraphUpdate.skip

  @JSExportTopLevel("Example.main")
  def main(args: Array[String]): Unit =
    Indigo.game
      .withConfig(config)
      .withAssets(assets)
      .startUpGameWith(setup)
      .usingInitialModel(initialModel)
      .updateModelUsing(updateModel)
      .presentUsing(renderer)
      .start()

}

// Start up types - can be anything, but you must supply a way to render the
// error cases
case class MyStartupData()
case class MyStartUpError(errors: List[String])
object MyStartUpError {
  implicit val toReportable: ToReportable[MyStartUpError] =
    ToReportable.createToReportable(e => e.errors.mkString("\n"))
}

// Your game model is anything you like!
case class MyGameModel()