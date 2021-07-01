package com.example.assetloading

import indigo._
import indigoextras.ui._
import indigoextras.subsystems._

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object AssetLoadingExample extends IndigoDemo[Unit, Unit, MyGameModel, MyViewModel] {

  val eventFilters: EventFilters = EventFilters.Permissive

  def boot(flags: Map[String, String]): Outcome[BootResult[Unit]] =
    Outcome {
      BootResult
        .noData(
          defaultGameConfig.withMagnification(2)
        )
        .withAssets(Assets.assets)
        .withSubSystems(AssetBundleLoader)
    }

  given CanEqual[Option[String], Option[String]] = CanEqual.derived

  def setup(bootData: Unit, assetCollection: AssetCollection, dice: Dice): Outcome[Startup[Unit]] =
    Outcome {
      assetCollection.findTextDataByName(AssetName("text")) match {
        case Some(value) =>
          println("Loaded text! " + value)
          Startup.Success(()).addShaders(MyColoredEntity.shader)
        case None =>
          Startup.Success(())
      }
    }

  def initialModel(startupData: Unit): Outcome[MyGameModel] =
    Outcome(MyGameModel(loaded = false))

  def initialViewModel(startupData: Unit, model: MyGameModel): Outcome[MyViewModel] =
    Outcome {
      MyViewModel(
        button = Button(
          buttonAssets = Assets.buttonAssets,
          bounds = Rectangle(10, 10, 16, 16),
          depth = Depth(2)
        ).withUpActions {
          println("Start loading assets...")
          List(AssetBundleLoaderEvent.Load(BindingKey("Junction box assets"), Assets.junctionboxImageAssets ++ Assets.otherAssetsToLoad))
        }
      )
    }

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  def updateModel(context: FrameContext[Unit], model: MyGameModel): GlobalEvent => Outcome[MyGameModel] = {
    case AssetBundleLoaderEvent.Started(key) =>
      println("Load started! " + key.toString())
      Outcome(model)

    case AssetBundleLoaderEvent.LoadProgress(key, percent, completed, total) =>
      println(s"In progress...: ${key.toString()} - ${percent.toString()}%, ${completed.toString()} of ${total.toString()}")
      Outcome(model)

    case AssetBundleLoaderEvent.Success(key) =>
      println("Got it! " + key.toString())
      Outcome(model.copy(loaded = true)).addGlobalEvents(PlaySound(AssetName("sfx"), Volume.Max))

    case AssetBundleLoaderEvent.Failure(key, message) =>
      println(s"Lost it... '$message' " + key.toString())
      Outcome(model)

    case _ =>
      Outcome(model)
  }

  def updateViewModel(context: FrameContext[Unit], model: MyGameModel, viewModel: MyViewModel): GlobalEvent => Outcome[MyViewModel] = {
    case FrameTick =>
      viewModel.button.update(context.inputState.mouse).map { btn =>
        viewModel.copy(button = btn)
      }

    case _ =>
      Outcome(viewModel)
  }

  def present(context: FrameContext[Unit], model: MyGameModel, viewModel: MyViewModel): Outcome[SceneUpdateFragment] = {
    val stuff = if (model.loaded) {
      List(
        Graphic(Rectangle(0, 0, 64, 64), 1, Assets.junctionBoxMaterial)
          .moveTo(30, 30),
        MyColoredEntity(Point(0, 50))
      )
    } else Nil

    Outcome(
      SceneUpdateFragment(viewModel.button.draw :: stuff)
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
      AssetType.Image(junctionBoxAlbedo, AssetPath("assets/" + junctionBoxAlbedo + ".png")),
      AssetType.Image(junctionBoxEmission, AssetPath("assets/" + junctionBoxEmission + ".png")),
      AssetType.Image(junctionBoxNormal, AssetPath("assets/" + junctionBoxNormal + ".png")),
      AssetType.Image(junctionBoxSpecular, AssetPath("assets/" + junctionBoxSpecular + ".png"))
    )

  def otherAssetsToLoad: Set[AssetType] =
    Set(
      AssetType.Text(AssetName("text"), AssetPath("assets/test.txt")),
      AssetType.Audio(AssetName("sfx"), AssetPath("assets/RetroGameJump.mp3"))
    )

  val junctionBoxMaterial: Material.Bitmap =
    Material.Bitmap(
      junctionBoxAlbedo,
      LightingModel.Lit(
        junctionBoxEmission,
        junctionBoxNormal,
        junctionBoxSpecular
      )
    )

  def assets: Set[AssetType] =
    Set(
      AssetType.Image(AssetName("graphics"), AssetPath("assets/graphics.png"))
    )

  val buttonAssets: ButtonAssets =
    ButtonAssets(
      up = Graphic(0, 0, 16, 16, 2, Material.Bitmap(AssetName("graphics"))).withCrop(32, 0, 16, 16),
      over = Graphic(0, 0, 16, 16, 2, Material.Bitmap(AssetName("graphics"))).withCrop(32, 16, 16, 16),
      down = Graphic(0, 0, 16, 16, 2, Material.Bitmap(AssetName("graphics"))).withCrop(32, 32, 16, 16)
    )

}

final case class MyColoredEntity(position: Point) extends EntityNode:
  def size: Size        = Size(32, 32)
  def flip: Flip        = Flip.default
  def ref: Point        = Point.zero
  def rotation: Radians = Radians.zero
  def scale: Vector2    = Vector2.one
  def depth: Depth      = Depth(1)

  def withDepth(newDepth: Depth): MyColoredEntity =
    this

  def toShaderData: ShaderData =
    ShaderData(MyColoredEntity.shader.id)

object MyColoredEntity:
  val shader: EntityShader =
    EntityShader
      .Source(ShaderId("my-colored-shader"))
      .withFragmentProgram(
        """
        |void fragment() {
        |  COLOR = vec4(0.0, 1.0, 0.0, 1.0);
        |}
        |""".stripMargin
      )
