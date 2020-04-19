package indigoexamples

import indigo._
import indigoexts.entrypoint._
import indigoexts.ui._
import indigo.shared.events.AssetEvent.AssetBatchLoaded

object PreloaderExample extends IndigoGameBasic[Unit, MyGameModel, Unit] {

  val config: GameConfig =
    defaultGameConfig.withMagnification(2)

  // We'll need some graphics.
  val assets: Set[AssetType] =
    Assets.assets

  val fonts: Set[FontInfo] = Set()

  val animations: Set[Animation] = Set()

  val subSystems: Set[SubSystem] = Set()

  def setup(assetCollection: AssetCollection): Startup[StartupErrors, Unit] =
    Startup.Success(())

  // Let's setup our button's initial state
  def initialModel(startupData: Unit): MyGameModel =
    MyGameModel(
      button = Button(ButtonState.Up).withUpAction { () =>
        println("Start loading assets...")
        List(
          LoadAssetBatch(Set(Assets.junctionboxImageAsset), Some(BindingKey("Junction box assets")))
        ) // On mouse release will emit this event.
      },
      loaded = false
    )

  // Match on event type, forward ButtonEvents to all buttons! (they'll work out if it's for the right button)
  def update(gameTime: GameTime, model: MyGameModel, inputState: InputState, dice: Dice): GlobalEvent => Outcome[MyGameModel] = {
    case e: ButtonEvent =>
      Outcome(
        model.copy(
          button = model.button.update(e)
        )
      )

    case AssetBatchLoaded(key) =>
      println("Got it! " + key.map(_.value).getOrElse(""))
      Outcome(model.copy(loaded = true))

    case AssetBatchLoadError(key) =>
      println("Lost it... " + key.map(_.value).getOrElse(""))
      Outcome(model)

    case _ =>
      Outcome(model)
  }

  def initialViewModel(startupData: Unit): MyGameModel => Unit = _ => ()

  def updateViewModel(gameTime: GameTime, model: MyGameModel, viewModel: Unit, inputState: InputState, dice: Dice): Outcome[Unit] =
    Outcome(())

  def present(gameTime: GameTime, model: MyGameModel, viewModel: Unit, inputState: InputState): SceneUpdateFragment = {
    val button: ButtonViewUpdate = model.button.draw(
      bounds = Rectangle(10, 10, 16, 16), // Where should the button be on the screen?
      depth = Depth(2),                   // At what depth?
      inputState = inputState,            // delegate events
      buttonAssets = ButtonAssets(        // We could cache the graphics much earlier
        up = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 0, 16, 16),
        over = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 16, 16, 16),
        down = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 32, 16, 16)
      )
    )

    val box = if (model.loaded) {
      List(
        Graphic(Rectangle(0, 0, 64, 64), 1, Assets.junctionBoxMaterial)
          .moveTo(30, 30)
      )
    } else Nil

    button.toSceneUpdateFragment.addGameLayerNodes(box)
  }
}

// We need a button in our model
final case class MyGameModel(button: Button, loaded: Boolean)

object Assets {

  val junctionBoxAlbedo: AssetName = AssetName("junctionbox_albedo")

  def junctionboxImageAsset: AssetType.Image =
    AssetType.Image(junctionBoxAlbedo, AssetPath("assets/" + junctionBoxAlbedo.value + ".png"))

  val junctionBoxMaterial: Material.Textured =
    Material.Textured(junctionBoxAlbedo)

  def assets: Set[AssetType] =
    Set(
      AssetType.Image(AssetName("graphics"), AssetPath("assets/graphics.png"))
    )

}
