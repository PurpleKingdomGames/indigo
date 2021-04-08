package indigoexamples

import indigo._

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object SpriteExample extends IndigoSandbox[Unit, Unit] {

  val config: GameConfig = defaultGameConfig

  val assets: Set[AssetType] = Set(AssetType.Image(AssetName("trafficlights"), AssetPath("assets/trafficlights.png")))

  val fonts: Set[FontInfo] = Set()

  val animationsKey: AnimationKey = AnimationKey("anims")

  val animations: Set[Animation] = Set(
    Animation(
      animationsKey,
      Frame(Rectangle(0, 0, 64, 64), Millis(250)),
      Frame(Rectangle(64, 0, 64, 64), Millis(250)),
      Frame(Rectangle(0, 64, 64, 64), Millis(250))
    )
  )

  val shaders: Set[Shader] = Set()

  def setup(assetCollection: AssetCollection, dice: Dice): Outcome[Startup[Unit]] =
    Outcome(Startup.Success(()))

  def initialModel(startupData: Unit): Outcome[Unit] =
    Outcome(())

  def updateModel(context: FrameContext[Unit], model: Unit): GlobalEvent => Outcome[Unit] =
    _ => Outcome(model)

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
  def present(context: FrameContext[Unit], model: Unit): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        Sprite(BindingKey("lights animation"), 0, 0, 1, animationsKey, Material.Bitmap(AssetName("trafficlights"))).play()
      )
    )
}
