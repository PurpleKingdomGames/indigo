package com.example.assetloading

import indigo._
import indigoextras.ui._
import indigoextras.subsystems._

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object AssetLoadingExample extends IndigoDemo[Unit, MyGameModel, MyViewModel] {

  val config: GameConfig =
    defaultGameConfig.withMagnification(2)

  val assets: Set[AssetType] =
    Assets.assets

  val fonts: Set[FontInfo] = Set()

  val animations: Set[Animation] = Set()

  val subSystems: Set[SubSystem] =
    Set(AssetBundleLoader.subSystem)

  def setup(assetCollection: AssetCollection, dice: Dice, flags: Map[String, String]): Startup[StartupErrors, Unit] =
    assetCollection.findTextDataByName(AssetName("text")) match {
      case Some(value) =>
        println("Loaded text! " + value)
        Startup.Success(())
      case None =>
        Startup.Success(())
    }

  def initialModel(startupData: Unit): MyGameModel =
    MyGameModel(loaded = false)

  def initialViewModel(startupData: Unit, model: MyGameModel): MyViewModel =
    MyViewModel(
      button = Button(
        buttonAssets = Assets.buttonAssets,
        bounds = Rectangle(10, 10, 16, 16),
        depth = Depth(2)
      ).withUpAction {
        println("Start loading assets...")
        List(AssetBundleLoaderEvent.Load(BindingKey("Junction box assets"), Assets.junctionboxImageAssets ++ Assets.otherAssetsToLoad))
      }
    )

  def updateModel(context: FrameContext, model: MyGameModel): GlobalEvent => Outcome[MyGameModel] = {
    case AssetBundleLoaderEvent.Started(key) =>
      println("Load started! " + key.toString())
      Outcome(model)

    case AssetBundleLoaderEvent.LoadProgress(key, percent, completed, total) =>
      println(s"In progress...: ${key.toString()} - ${percent.toString()}%, ${completed.toString()} of ${total.toString()}")
      Outcome(model)

    case AssetBundleLoaderEvent.Success(key) =>
      println("Got it! " + key.toString())
      Outcome(model.copy(loaded = true)).addGlobalEvents(PlaySound(AssetName("sfx"), Volume.Max))

    case AssetBundleLoaderEvent.Failure(key) =>
      println("Lost it... " + key.toString())
      Outcome(model)

    case _ =>
      Outcome(model)
  }

  def updateViewModel(context: FrameContext, model: MyGameModel, viewModel: MyViewModel): Outcome[MyViewModel] =
    viewModel.button.update(context.inputState.mouse).map { btn =>
      viewModel.copy(button = btn)
    }

  def present(context: FrameContext, model: MyGameModel, viewModel: MyViewModel): SceneUpdateFragment = {
    val box = if (model.loaded) {
      List(
        Graphic(Rectangle(0, 0, 64, 64), 1, Assets.junctionBoxMaterial)
          .moveTo(30, 30)
      )
    } else Nil

    SceneUpdateFragment(
      viewModel.button.draw :: box
    )
  }
}

final case class MyGameModel(loaded: Boolean)
final case class MyViewModel(button: Button)

object Assets {

  val junctionBoxAlbedo: AssetName   = AssetName("junctionbox_albedo")
  val junctionBoxEmission: AssetName = AssetName("junctionbox_emission")
  val junctionBoxNormal: AssetName   = AssetName("junctionbox_normal")
  val junctionBoxSpecular: AssetName = AssetName("junctionbox_specular")

  def junctionboxImageAssets: Set[AssetType] =
    Set(
      AssetType.Image(junctionBoxAlbedo, AssetPath("assets/" + junctionBoxAlbedo.value + ".png")),
      AssetType.Image(junctionBoxEmission, AssetPath("assets/" + junctionBoxEmission.value + ".png")),
      AssetType.Image(junctionBoxNormal, AssetPath("assets/" + junctionBoxNormal.value + ".png")),
      AssetType.Image(junctionBoxSpecular, AssetPath("assets/" + junctionBoxSpecular.value + ".png"))
    )

  def otherAssetsToLoad: Set[AssetType] =
    Set(
      AssetType.Text(AssetName("text"), AssetPath("assets/test.txt")),
      AssetType.Audio(AssetName("sfx"), AssetPath("assets/RetroGameJump.mp3"))
    )

  val junctionBoxMaterial: Material.Lit =
    Material.Lit(
      junctionBoxAlbedo,
      junctionBoxEmission,
      junctionBoxNormal,
      junctionBoxSpecular
    )

  def assets: Set[AssetType] =
    Set(
      AssetType.Image(AssetName("graphics"), AssetPath("assets/graphics.png"))
    )

  val buttonAssets: ButtonAssets =
    ButtonAssets(
      up = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 0, 16, 16),
      over = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 16, 16, 16),
      down = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 32, 16, 16)
    )

}
