package indigoexamples

import indigo._
import indigogame._

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object SpriteExample extends IndigoGameBasic[Unit, Unit, Unit] {

  val config: GameConfig = defaultGameConfig

  val assets: Set[AssetType] = Set(AssetType.Image(AssetName("trafficlights"), AssetPath("assets/trafficlights.png")))

  val fonts: Set[FontInfo] = Set()

  val subSystems: Set[SubSystem] = Set()

  val animationsKey: AnimationKey = AnimationKey("anims")

  val animations: Set[Animation] = Set(
    Animation.create(
      animationsKey,
      Material.Textured(AssetName("trafficlights")),
      Point(128, 128),
      cycle = Cycle.create(
        "lights",
        NonEmptyList(
          Frame(Rectangle(0, 0, 64, 64), Millis(250)),
          Frame(Rectangle(64, 0, 64, 64), Millis(250)),
          Frame(Rectangle(0, 64, 64, 64), Millis(250))
        )
      )
    )
  )

  def setup(assetCollection: AssetCollection, flags: Map[String, String]): Startup[StartupErrors, Unit] =
    Startup.Success(())

  def initialModel(startupData: Unit): Unit =
    ()

  def update(gameTime: GameTime, model: Unit, inputState: InputState, dice: Dice): GlobalEvent => Outcome[Unit] =
    _ => Outcome(model)

  def initialViewModel(startupData: Unit): Unit => Unit = _ => ()

  def updateViewModel(gameTime: GameTime, model: Unit, viewModel: Unit, inputState: InputState, dice: Dice): Outcome[Unit] =
    Outcome(())

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
  def present(gameTime: GameTime, model: Unit, viewModel: Unit, inputState: InputState): SceneUpdateFragment =
    SceneUpdateFragment.empty.addGameLayerNodes(
      Sprite(
        BindingKey("lights animation"),
        0,
        0,
        64,
        64,
        1,
        animationsKey
      ).play()
    )
}
