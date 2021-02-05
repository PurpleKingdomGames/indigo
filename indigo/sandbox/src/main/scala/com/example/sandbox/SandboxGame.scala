package com.example.sandbox

import indigo._
import indigo.json.Json

import indigoextras.subsystems.FPSCounter
import indigoextras.ui.InputField
import indigoextras.ui.InputFieldAssets
import indigo.scenes._

import scala.scalajs.js.annotation._
import indigo.shared.shader.Uniform
import indigo.shared.shader.ShaderPrimitive._

@JSExportTopLevel("IndigoGame")
object SandboxGame extends IndigoGame[SandboxBootData, SandboxStartupData, SandboxGameModel, SandboxViewModel] {

  private val targetFPS: Int          = 60
  private val magnificationLevel: Int = 2
  private val viewportWidth: Int      = 228 * magnificationLevel
  private val viewportHeight: Int     = 128 * magnificationLevel

  def initialScene(bootData: SandboxBootData): Option[SceneName] =
    None

  def scenes(bootData: SandboxBootData): NonEmptyList[Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]] =
    NonEmptyList(TestScene)

  val eventFilters: EventFilters = EventFilters.Permissive

  def boot(flags: Map[String, String]): Outcome[BootResult[SandboxBootData]] = {
    val gameViewport =
      (flags.get("width"), flags.get("height")) match {
        case (Some(w), Some(h)) =>
          GameViewport(w.toInt, h.toInt)

        case _ =>
          GameViewport(viewportWidth, viewportHeight)
      }

    Outcome(
      BootResult(
        GameConfig(
          viewport = gameViewport,
          frameRate = targetFPS,
          clearColor = RGBA(0.4, 0.2, 0.5, 1),
          magnification = magnificationLevel
        ),
        SandboxBootData(flags.getOrElse("key", "No entry for 'key'."))
      ).withAssets(SandboxAssets.assets ++ Shaders.assets)
        .withFonts(SandboxView.fontInfo)
        .withSubSystems(FPSCounter(SandboxView.fontKey, Point(3, 100), targetFPS, Depth(200)))
        .withShaders(
          Shaders.circle,
          Shaders.external,
          Shaders.sea
        )
    )
  }

  def setup(bootData: SandboxBootData, assetCollection: AssetCollection, dice: Dice): Outcome[Startup[SandboxStartupData]] = {
    println(bootData.message)

    def makeStartupData(aseprite: Aseprite, spriteAndAnimations: SpriteAndAnimations): Startup.Success[SandboxStartupData] =
      Startup
        .Success(
          SandboxStartupData(
            Dude(
              aseprite,
              spriteAndAnimations.sprite
                .withDepth(Depth(3))
                .withRef(16, 16)                                                                         // Initial offset, so when talk about his position it's the center of the sprite
                .moveTo(viewportWidth / 2 / magnificationLevel, viewportHeight / 2 / magnificationLevel) // Also place him in the middle of the screen initially
            )
          )
        )
        .addAnimations(spriteAndAnimations.animations)

    val res: Option[Startup.Success[SandboxStartupData]] = for {
      json                <- assetCollection.findTextDataByName(AssetName(SandboxAssets.dudeName.value + "-json"))
      aseprite            <- Json.asepriteFromJson(json)
      spriteAndAnimations <- aseprite.toSpriteAndAnimations(dice, SandboxAssets.dudeName)
    } yield makeStartupData(aseprite, spriteAndAnimations)

    Outcome(res.getOrElse(Startup.Failure("Failed to load the dude")))
  }

  def initialModel(startupData: SandboxStartupData): Outcome[SandboxGameModel] =
    Outcome(SandboxModel.initialModel(startupData))

  def initialViewModel(startupData: SandboxStartupData, model: SandboxGameModel): Outcome[SandboxViewModel] = {
    val assets =
      new InputFieldAssets(
        Text("placeholder", 0, 0, 0, SandboxView.fontKey).alignLeft,
        Graphic(0, 0, 16, 16, 2, Material.Textured(SandboxAssets.smallFontName)).withCrop(188, 78, 14, 23).withTint(0, 0, 1)
      )

    Outcome(
      SandboxViewModel(
        Point.zero,
        InputField("single", assets).withKey(BindingKey("single")).makeSingleLine,
        InputField("multi\nline", assets).withKey(BindingKey("multi")).makeMultiLine
      )
    )
  }

  def updateModel(context: FrameContext[SandboxStartupData], model: SandboxGameModel): GlobalEvent => Outcome[SandboxGameModel] =
    SandboxModel.updateModel(model)

  def updateViewModel(context: FrameContext[SandboxStartupData], model: SandboxGameModel, viewModel: SandboxViewModel): GlobalEvent => Outcome[SandboxViewModel] = {
    case FrameTick =>
      val updateOffset: Point =
        context.inputState.gamepad.dpad match {
          case GamepadDPad(true, _, _, _) =>
            viewModel.offset + Point(0, -1)

          case GamepadDPad(_, true, _, _) =>
            viewModel.offset + Point(0, 1)

          case GamepadDPad(_, _, true, _) =>
            viewModel.offset + Point(-1, 0)

          case GamepadDPad(_, _, _, true) =>
            viewModel.offset + Point(1, 0)

          case _ =>
            viewModel.offset
        }

      //more stuff
      for {
        single <- viewModel.single.update(context)
        multi  <- viewModel.multi.update(context)
      } yield viewModel.copy(updateOffset, single, multi)

    case FullScreenEntered =>
      println("Entered full screen mode")
      Outcome(viewModel)

    case FullScreenExited =>
      println("Exited full screen mode")
      Outcome(viewModel)

    case _ =>
      Outcome(viewModel)
  }

  def present(context: FrameContext[SandboxStartupData], model: SandboxGameModel, viewModel: SandboxViewModel): Outcome[SceneUpdateFragment] = {
    val scene =
      SandboxView
        .updateView(model, viewModel, context.inputState)
        .addLayer(
          Layer(
            // viewModel.single.draw(gameTime, boundaryLocator) //|+|
            viewModel.multi.draw(context.gameTime, context.boundaryLocator)
          ).withDepth(Depth(1000))
        )

    Outcome(
      SceneUpdateFragment.empty
        .addLayer(
          Layer.empty
            .withKey(BindingKey("bg"))
            // .withDepth(Depth(500))
            .withMagnification(1)
        ) |+| scene
    )
  }
}

final case class Dude(aseprite: Aseprite, sprite: Sprite)
final case class SandboxBootData(message: String)
final case class SandboxStartupData(dude: Dude)
final case class SandboxViewModel(offset: Point, single: InputField, multi: InputField)

object TestScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel] {

  type SceneModel     = Unit
  type SceneViewModel = Unit

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: indigo.scenes.Lens[SandboxGameModel, Unit] =
    Lens.unit[SandboxGameModel]

  def viewModelLens: Lens[SandboxViewModel, Unit] =
    Lens.unit[SandboxViewModel]

  def name: SceneName =
    SceneName("test")

  def subSystems: Set[SubSystem] =
    Set()

  def updateModel(context: FrameContext[SandboxStartupData], model: Unit): GlobalEvent => Outcome[Unit] =
    _ => Outcome(model)

  def updateViewModel(context: FrameContext[SandboxStartupData], model: Unit, viewModel: Unit): GlobalEvent => Outcome[Unit] =
    _ => Outcome(viewModel)

  def present(context: FrameContext[SandboxStartupData], model: Unit, viewModel: Unit): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment.empty
        .addLayer(
          Layer(
            Graphic(0, 0, 228 * 3, 140 * 3, 10, Material.Custom(Shaders.seaId, Map(), SandboxAssets.dots))
          ).withKey(BindingKey("bg"))
            .withMagnification(1)
        )
        .addLayer(
          Layer(
            Graphic(120, 10, 32, 32, 1, SandboxAssets.dotsMaterial),
            Graphic(140, 50, 32, 32, 1, Material.Custom(Shaders.circleId, Map(), SandboxAssets.dots)),
            Graphic(
              140,
              50,
              32,
              32,
              1,
              Material.Custom(
                Shaders.externalId,
                Map(
                  Uniform("ALPHA")        -> float(0.75),
                  Uniform("BORDER_COLOR") -> vec4(1.0, 1.0, 0.0, 0.5)
                ),
                SandboxAssets.dots
              )
            )
          )
        )
    )

}

object Shaders {

  val circleId: ShaderId =
    ShaderId("circle")

  def circleVertex(orbitDist: Double): String =
    s"""
    |float timeToRadians(float t) {
    |  return TAU * mod(t, 1.0);
    |}
    |
    |void vertex() {
    |  float x = sin(timeToRadians(TIME / 2.0)) * ${orbitDist.toString()} + VERTEX.x;
    |  float y = cos(timeToRadians(TIME / 2.0)) * ${orbitDist.toString()} + VERTEX.y;
    |  vec2 orbit = vec2(x, y);
    |  VERTEX = vec4(orbit, VERTEX.zw);
    |}
    |""".stripMargin

  val circleFragment: String =
    """
    |float timeToRadians(float t) {
    |  return TAU * mod(t, 1.0);
    |}
    |
    |void fragment() {
    |  float red = UV.x * (1.0 - ((cos(timeToRadians(TIME)) + 1.0) / 2.0));
    |  float alpha = 1.0 - step(0.0, length(UV - 0.5) - 0.5);
    |  vec4 circle = vec4(red, UV.y, 0.0, alpha);
    |  COLOR = circle;
    |}
    |""".stripMargin

  val circle: CustomShader.Source =
    CustomShader
      .Source(circleId)
      .withVertexProgram(circleVertex(0.5))
      .withFragmentProgram(circleFragment)

  val externalId: ShaderId =
    ShaderId("external")

  val vertAsset: AssetName = AssetName("vertex")
  val fragAsset: AssetName = AssetName("fragment")
  val seaAsset: AssetName  = AssetName("sea")

  val external: CustomShader.External =
    CustomShader
      .External(externalId)
      .withVertexProgram(vertAsset)
      .withFragmentProgram(fragAsset)
      .withLightProgram(fragAsset)

  val seaId: ShaderId =
    ShaderId("sea")

  val sea: CustomShader.External =
    CustomShader
      .External(seaId)
      .withFragmentProgram(seaAsset)

  def assets: Set[AssetType] =
    Set(
      AssetType.Text(vertAsset, AssetPath("assets/shader.vert")),
      AssetType.Text(fragAsset, AssetPath("assets/shader.frag")),
      AssetType.Text(seaAsset, AssetPath("assets/sea.frag"))
    )

}
