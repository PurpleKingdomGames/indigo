package com.example.sandbox

import indigo._
import indigo.json.Json

import indigoextras.subsystems.FPSCounter
import indigoextras.ui.InputField
import indigoextras.ui.InputFieldAssets
import indigo.scenes._

import scala.scalajs.js.annotation._
import indigo.shared.events.FullScreenEntered
import indigo.shared.events.FullScreenExited

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
      ).withAssets(SandboxAssets.assets)
        .withFonts(SandboxView.fontInfo)
        .withSubSystems(FPSCounter(SandboxView.fontKey, Point(3, 100), targetFPS))
        .withShaders(Shaders.circle)
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

  def present(context: FrameContext[SandboxStartupData], model: SandboxGameModel, viewModel: SandboxViewModel): Outcome[SceneUpdateFragment] =
    Outcome(
      SandboxView.updateView(model, viewModel, context.inputState) |+|
        // viewModel.single.draw(gameTime, boundaryLocator) //|+|
        viewModel.multi.draw(context.gameTime, context.boundaryLocator)
    )
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
      SceneUpdateFragment(
        Graphic(120, 10, 32, 32, 1, SandboxAssets.dotsMaterial),
        Graphic(140, 50, 32, 32, 1, Material.Custom(Shaders.circleId, SandboxAssets.dots))
      )
    )

}

object Shaders {

  val circleId: ShaderId =
    ShaderId("circle")

  val circleVertex: String =
    """
    |void vertex() {
    | // Do nothing.
    |}
    |""".stripMargin

  val circleFragment: String =
    """
    |void fragment() {
    |  float alpha = 1.0 - step(0.0, length(UV - 0.5) - 0.5);
    |  COLOR = vec4(UV, 0.0, alpha);
    |}
    |""".stripMargin

  val circle: CustomShader.Source =
    CustomShader.Source(
      id = circleId,
      vertex = circleVertex,
      fragment = circleFragment
    )

}
