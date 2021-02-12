package com.example.sandbox

import indigo._
import indigo.json.Json

import indigoextras.subsystems.FPSCounter
import indigoextras.ui.InputField
import indigoextras.ui.InputFieldAssets
import indigo.scenes._

import scala.scalajs.js.annotation._

import com.example.sandbox.scenes.OriginalScene
import com.example.sandbox.scenes.Shaders
import com.example.sandbox.scenes.ShapeShaders
import com.example.sandbox.scenes.ShapesScene

@JSExportTopLevel("IndigoGame")
object SandboxGame extends IndigoGame[SandboxBootData, SandboxStartupData, SandboxGameModel, SandboxViewModel] {

  private val targetFPS: Int          = 60
  private val magnificationLevel: Int = 2
  private val viewportWidth: Int      = 228 * magnificationLevel
  private val viewportHeight: Int     = 128 * magnificationLevel

  def initialScene(bootData: SandboxBootData): Option[SceneName] =
    Some(ShapesScene.name)

  def scenes(bootData: SandboxBootData): NonEmptyList[Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]] =
    NonEmptyList(OriginalScene, ShapesScene)

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
      ).withAssets(SandboxAssets.assets ++ Shaders.assets ++ ShapeShaders.assets)
        .withFonts(Fonts.fontInfo)
        .withSubSystems(FPSCounter(Fonts.fontKey, Point(5, 165), targetFPS, Depth(200), SandboxAssets.fontMaterial))
        .withShaders(
          Shaders.circle,
          Shaders.external,
          Shaders.sea,
          ShapeShaders.circleExternal
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
                .withMaterial(SandboxAssets.dudeMaterial)
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
        Text("placeholder", 0, 0, 0, Fonts.fontKey, SandboxAssets.fontMaterial).alignLeft,
        Graphic(0, 0, 16, 16, 2, StandardMaterial.ImageEffects(SandboxAssets.smallFontName).withTint(RGB(0, 0, 1)))
          .withCrop(188, 78, 14, 23)
      )

    Outcome(
      SandboxViewModel(
        Point.zero,
        InputField("single", assets).withKey(BindingKey("single")).makeSingleLine,
        InputField("multi\nline", assets).withKey(BindingKey("multi")).makeMultiLine.moveTo(5, 5)
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
    Outcome(SceneUpdateFragment.empty)
}

final case class Dude(aseprite: Aseprite, sprite: Sprite)
final case class SandboxBootData(message: String)
final case class SandboxStartupData(dude: Dude)
final case class SandboxViewModel(offset: Point, single: InputField, multi: InputField)
