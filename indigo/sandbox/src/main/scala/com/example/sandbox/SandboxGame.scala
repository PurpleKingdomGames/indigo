package com.example.sandbox

import indigo._
import indigo.json.Json

import indigoextras.formats._
import indigoextras.subsystems.FPSCounter
import indigoextras.ui.InputField
import indigoextras.ui.InputFieldAssets

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object SandboxGame extends IndigoDemo[SandboxBootData, SandboxStartupData, SandboxGameModel, SandboxViewModel] {

  private val targetFPS: Int          = 60
  private val magnificationLevel: Int = 2
  private val viewportWidth: Int      = 228 * magnificationLevel
  private val viewportHeight: Int     = 128 * magnificationLevel

  def boot(flags: Map[String, String]): BootResult[SandboxBootData] =
    BootResult(
      GameConfig(
        viewport = GameViewport(viewportWidth, viewportHeight),
        frameRate = targetFPS,
        clearColor = ClearColor(0.4, 0.2, 0.5, 1),
        magnification = magnificationLevel
      ),
      SandboxBootData(flags.getOrElse("key", "No entry for 'key'."))
    ).withAssets(SandboxAssets.assets)
      .withFonts(SandboxView.fontInfo)
      .withSubSystems(FPSCounter(SandboxView.fontKey, Point(3, 100), targetFPS))

  def setup(bootData: SandboxBootData, assetCollection: AssetCollection, dice: Dice): Startup[StartupErrors, SandboxStartupData] = {
    println(bootData.message)

    def makeStartupData(aseprite: Aseprite, spriteAndAnimations: SpriteAndAnimations): Startup.Success[SandboxStartupData] =
      Startup
        .Success(
          SandboxStartupData(
            Dude(
              aseprite,
              spriteAndAnimations.sprite
                .withRef(16, 16)                                                                         // Initial offset, so when talk about his position it's the center of the sprite
                .moveTo(viewportWidth / 2 / magnificationLevel, viewportHeight / 2 / magnificationLevel) // Also place him in the middle of the screen initially
            )
          )
        )
        .addAnimations(spriteAndAnimations.animations)

    val res: Option[Startup.Success[SandboxStartupData]] = for {
      json                <- assetCollection.findTextDataByName(AssetName(SandboxAssets.dudeName.value + "-json"))
      aseprite            <- Json.asepriteFromJson(json)
      spriteAndAnimations <- AsepriteConverter.toSpriteAndAnimations(dice, aseprite, Depth(3), SandboxAssets.dudeName)
    } yield makeStartupData(aseprite, spriteAndAnimations)

    res.getOrElse(Startup.Failure(StartupErrors("Failed to load the dude")))
  }

  def initialModel(startupData: SandboxStartupData): SandboxGameModel =
    SandboxModel.initialModel(startupData)

  def initialViewModel(startupData: SandboxStartupData, model: SandboxGameModel): SandboxViewModel = {
    val assets =
      new InputFieldAssets(
        Text("placeholder", 0, 0, 0, SandboxView.fontKey).alignLeft,
        Graphic(0, 0, 16, 16, 2, Material.Textured(SandboxAssets.smallFontName)).withCrop(188, 78, 14, 23).withTint(0, 0, 1)
      )

    SandboxViewModel(
      Point.zero,
      InputField("single", assets).makeSingleLine,
      InputField("multi\nline", assets).makeMultiLine
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
      Outcome(
        viewModel.copy(
          offset = updateOffset,
          single = viewModel.single.update(context),
          multi = viewModel.multi.update(context)
        )
      )

    case _ =>
      Outcome(viewModel)
  }

  def present(context: FrameContext[SandboxStartupData], model: SandboxGameModel, viewModel: SandboxViewModel): SceneUpdateFragment =
    SandboxView.updateView(model, viewModel, context.inputState) |+|
      // viewModel.single.draw(gameTime, boundaryLocator) //|+|
      viewModel.multi.draw(context.gameTime, context.boundaryLocator)
}

final case class Dude(aseprite: Aseprite, sprite: Sprite)
final case class SandboxBootData(message: String)
final case class SandboxStartupData(dude: Dude)
final case class SandboxViewModel(offset: Point, single: InputField, multi: InputField)
