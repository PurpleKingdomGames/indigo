package ingidoexamples

import com.purplekingdomgames.indigo._
import com.purplekingdomgames.indigo.gameengine.assets.AssetCollection
import com.purplekingdomgames.indigo.gameengine.{GameTime, StartupErrors, events}
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.BindingKey
import com.purplekingdomgames.shared.{AssetType, GameConfig, ImageAsset}

object SpriteExample extends IndigoGameBasic[Unit, Unit] {

  val config: GameConfig = defaultGameConfig

  val assets: Set[AssetType] = Set(ImageAsset("trafficlights", "assets/trafficlights.png"))

  def setup(assetCollection: AssetCollection): Either[StartupErrors, Unit] =
    Right(())

  def initialModel(startupData: Unit): Unit =
    ()

  def update(gameTime: GameTime, model: Unit): events.GameEvent => Unit =
    _ => model

  /*
   Minimal sprite example, with one animation that we just play.
   Small thing to watch out for, the BindingKey. This example works
   because the key is a fixed string. If we'd used `BindingKey.generate`
   the animation would be constantly stuck on frame 2, because on everyframe it
   would create the animation, generate a new key, find no reference to the key
   so assume it's a new animation and play from the beginning. Two ways around this:
   Fixed key strings, or better (and best practice) is to generate these things in
   advance of the render loops (cache effectively), then the key wouldn't be
   regenerated.
  */
  def present(gameTime: GameTime, model: Unit, frameInputEvents: events.FrameInputEvents): SceneUpdateFragment =
    SceneUpdateFragment().addGameLayerNodes(
      Sprite(BindingKey("lights animation"), 0, 0, 64, 64, 1, "trafficlights",
        Animations(
          spriteSheetWidth = 128,
          spriteSheetHeight = 128,
          cycle = Cycle(
            label = "lights",
            frame = Frame(0, 0, 64, 64, 250),
            frames = List(
              Frame(64, 0, 64, 64, 250),
              Frame(0, 64, 64, 64, 250)
            )
          )
        )
      ).play()
    )
}