package indigoexamples

import indigo._
import indigoexts.entrypoint._
import indigoexts.ui._
import indigo.shared.events.AssetEvent.AssetBatchLoaded
import indigo.shared.events.AssetEvent

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
          LoadAssetBatch(Set(Assets.junctionboxImageAsset), Some(BindingKey("Junction box assets")), true)
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

    case AssetBatchLoaded(key, true) =>
      println("Got it! " + key.map(_.value).getOrElse(""))
      Outcome(model.copy(loaded = true))

    case AssetBatchLoaded(key, false) =>
      println("Got it! ...but can't use it. " + key.map(_.value).getOrElse(""))
      Outcome(model)

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

/*
Latest:
This isn't quite right. I think it isn't a preloader so much as a batch loader.
All it does it track the progress of loading batches and emits events.
- Loading of batch X started
- Loading of batch X, N percent completed
- Loading of batch X completed success | failure

If you want to visualise the loader - make another sub system that's
listening for the events this one emits.

---
Convert Preload batch to individual load requests, each with a key.
Track loaded status of each asset.
As each asset loads, emit a percent loaded event.
Once all completed, emit a complete or errors event.

Should handle visuals (start, loading, end, error) optionally to encapsulate?

It's going to reinitialise the engine on every load. To avoid that,
could we ask the AssetLoader to load without reinitialising, and then once
they're all cached, load them again as a batch?



 */
final case class Preloader(count: Int, register: Map[BindingKey, Boolean], currentBatch: Set[AssetType]) extends SubSystem {
  val id: BindingKey = BindingKey.generate

  type EventType = PreloaderEvents

  val eventFilter: GlobalEvent => Option[PreloaderEvents] = {
    case e: PreloadAssets                   => Some(e)
    case AssetBatchLoaded(Some(key), _) => Some(PreloaderInternalAssetLoaded(key))
    case _                                  => None
  }

  def update(gameTime: GameTime, dice: Dice): PreloaderEvents => Outcome[SubSystem] = {
    case PreloadAssets(batch) =>
      val toLoad: List[(AssetEvent.LoadAsset, BindingKey)] = batch.toList.zipWithIndex.map {
        case (asset, index) =>
          val key =
            BindingKey(s"preloader_${id.value}_${(index + count).toString()}")

          val event =
            AssetEvent.LoadAsset(
              asset,
              Some(key),
              false
            )

          (event, key)
      }

      Outcome(
        this.copy(
          count = this.count + batch.size,
          register = register ++ toLoad.map(p => (p._2, false)).toMap,
          currentBatch = batch // Should be able to track multiple batches?
        )
      ).addGlobalEvents(toLoad.map(_._1))

    case PreloaderInternalAssetLoaded(key) =>
      if (register.contains(key)) {
        val nextRegister = register + (key -> true)

        val allKey =
          BindingKey(s"preloader_${id.value}_all")

        val outcomeEvent =
          if (nextRegister.forall(_._2))
            List(AssetEvent.LoadAssetBatch(currentBatch, Some(allKey), true))
          else Nil

        Outcome(
          this.copy(
            register = nextRegister
          )
        ).addGlobalEvents(outcomeEvent)
      } else Outcome(this)
  }

  def render(gameTime: GameTime): SceneUpdateFragment =
    SceneUpdateFragment.empty
}

sealed trait PreloaderEvents                                   extends GlobalEvent
final case class PreloadAssets(assets: Set[AssetType])         extends PreloaderEvents
final case class PreloaderInternalAssetLoaded(key: BindingKey) extends PreloaderEvents
