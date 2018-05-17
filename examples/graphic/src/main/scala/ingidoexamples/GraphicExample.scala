package ingidoexamples

import com.purplekingdomgames.indigo._
import com.purplekingdomgames.indigo.gameengine.assets.AssetCollection
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.FontInfo
import com.purplekingdomgames.indigo.gameengine.{GameTime, StartupErrors, events}
import com.purplekingdomgames.indigo.gameengine.scenegraph.{Animations, Graphic, SceneUpdateFragment}
import com.purplekingdomgames.shared.{AssetType, GameConfig, ImageAsset}

object GraphicExample extends IndigoGameBasic[Unit, Unit] {

  val config: GameConfig = defaultGameConfig

  val assets: Set[AssetType] = Set(ImageAsset("graphics", "assets/graphics.png"))

  val fonts: Set[FontInfo] = Set()

  val animations: Set[Animations] = Set()

  def setup(assetCollection: AssetCollection): Either[StartupErrors, Unit] =
    Right(())

  def initialModel(startupData: Unit): Unit =
    ()

  def update(gameTime: GameTime, model: Unit): events.GameEvent => Unit =
    _ => model

  def present(gameTime: GameTime, model: Unit, frameInputEvents: events.FrameInputEvents): SceneUpdateFragment =
    SceneUpdateFragment().addGameLayerNodes(
      Graphic(0, 0, 256, 256, 1, "graphics").moveTo(64, 10),
      Graphic(0, 0, 32, 32, 1, "graphics").withCrop(32, 0, 32, 32),
      Graphic(0, 0, 128, 128, 1, "graphics").moveTo(0, 128).withCrop(128, 0, 128, 128).withTint(0, 1, 1).withAlpha(0.5)
    )
}
