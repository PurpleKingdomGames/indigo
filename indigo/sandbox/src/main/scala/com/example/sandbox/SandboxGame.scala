package com.example.sandbox

import indigo._
import indigo.json.Json
import indigoexts.formats._
import indigoexts.subsystems.fpscounter.FPSCounter
import indigogame._

import scala.scalajs.js.annotation._
import indigoexts.uicomponents.InputField
import indigoexts.uicomponents.InputFieldAssets

@JSExportTopLevel("IndigoGame")
object SandboxGame extends IndigoDemo[SandboxStartupData, SandboxGameModel, SandboxViewModel] {

  val targetFPS: Int = 60

  private val magnificationLevel: Int = 2
  private val viewportWidth: Int      = 228 * magnificationLevel
  private val viewportHeight: Int     = 128 * magnificationLevel

  val config: GameConfig =
    GameConfig(
      viewport = GameViewport(viewportWidth, viewportHeight),
      frameRate = targetFPS,
      clearColor = ClearColor(0.4, 0.2, 0.5, 1),
      magnification = magnificationLevel
    )

  val assets: Set[AssetType] =
    SandboxAssets.assets

  val fonts: Set[FontInfo] =
    Set(SandboxView.fontInfo)

  val animations: Set[Animation] =
    Set()

  val subSystems: Set[SubSystem] =
    Set(FPSCounter.subSystem(SandboxView.fontKey, Point(3, 100), targetFPS))

  def setup(assetCollection: AssetCollection, dice: Dice, flags: Map[String, String]): Startup[StartupErrors, SandboxStartupData] = {
    println("flags")
    println(flags.mkString(", "))
    println(flags.get("data"))
    println(flags.get("fish"))

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

  def initialViewModel(startupData: SandboxStartupData): SandboxGameModel => SandboxViewModel =
    _ => {
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

  def update(context: FrameContext, model: SandboxGameModel): GlobalEvent => Outcome[SandboxGameModel] =
    SandboxModel.updateModel(model)

  def updateViewModel(context: FrameContext, model: SandboxGameModel, viewModel: SandboxViewModel): Outcome[SandboxViewModel] = {
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
  }

  def present(context: FrameContext, model: SandboxGameModel, viewModel: SandboxViewModel): SceneUpdateFragment =
    SandboxView.updateView(model, viewModel, context.inputState) |+|
      // viewModel.single.draw(gameTime, boundaryLocator) //|+|
      viewModel.multi.draw(context.gameTime, context.boundaryLocator)
}

final case class Dude(aseprite: Aseprite, sprite: Sprite)
final case class SandboxStartupData(dude: Dude)
final case class SandboxViewModel(offset: Point, single: InputField, multi: InputField)
