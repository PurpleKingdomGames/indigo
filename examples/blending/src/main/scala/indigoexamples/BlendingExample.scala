package indigoexamples

import indigo._

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object BlendingExample extends IndigoSandbox[Unit, Unit]:

  val config: GameConfig = defaultGameConfig

  val assets: Set[AssetType] =
    Set(
      AssetType.Image(AssetName("graphics"), AssetPath("assets/graphics.png"))
    ) ++ MyBlendShader.assets

  val fonts: Set[FontInfo] = Set()

  val animations: Set[Animation] = Set()

  val shaders: Set[Shader] = Set(MyBlendShader.blendShader)

  def setup(assetCollection: AssetCollection, dice: Dice): Outcome[Startup[Unit]] =
    Outcome(Startup.Success(()))

  def initialModel(startupData: Unit): Outcome[Unit] =
    Outcome(())

  def updateModel(context: FrameContext[Unit], model: Unit): GlobalEvent => Outcome[Unit] =
    _ => Outcome(model)

  val graphic: Graphic[Material.ImageEffects] =
    Graphic(256, 256, Material.ImageEffects(AssetName("graphics")))
      .withCrop(128, 0, 96, 96)
      .moveTo(200, 200)
      .scaleBy(1.5, 1.5)
      .withRef(96, 96)

  def present(context: FrameContext[Unit], model: Unit): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        Layer(graphic)
          .withBlending(
            Blending.Normal
              .withBlendMaterial(MyBlendShader())
          )
      )
    )

final case class MyBlendShader() extends BlendMaterial:
  def toShaderData: BlendShaderData = BlendShaderData(MyBlendShader.shaderId)

object MyBlendShader:
  val fragShader             = AssetName("blend shader")
  val shaderId               = ShaderId("my blend shader")
  val assets: Set[AssetType] = Set(AssetType.Text(fragShader, AssetPath("assets/blend.frag")))
  val blendShader            = BlendShader.External(shaderId).withFragmentProgram(fragShader)
