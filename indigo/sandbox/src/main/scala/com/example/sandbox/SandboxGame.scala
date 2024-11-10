package com.example.sandbox

import com.example.sandbox.scenes.Archetype
import com.example.sandbox.scenes.BoundingCircleScene
import com.example.sandbox.scenes.BoundsScene
import com.example.sandbox.scenes.BoxesScene
import com.example.sandbox.scenes.CameraScene
import com.example.sandbox.scenes.CameraWithCloneTilesScene
import com.example.sandbox.scenes.CaptureScreenScene
import com.example.sandbox.scenes.CaptureScreenScene.CaptureScreenSceneViewModel
import com.example.sandbox.scenes.ClipScene
import com.example.sandbox.scenes.ConfettiScene
import com.example.sandbox.scenes.CratesScene
import com.example.sandbox.scenes.LegacyEffectsScene
import com.example.sandbox.scenes.LightsScene
import com.example.sandbox.scenes.LineReflectionScene
import com.example.sandbox.scenes.ManyEventHandlers
import com.example.sandbox.scenes.MutantsScene
import com.example.sandbox.scenes.OriginalScene
import com.example.sandbox.scenes.PathFindingScene
import com.example.sandbox.scenes.PointersScene
import com.example.sandbox.scenes.RefractionScene
import com.example.sandbox.scenes.Shaders
import com.example.sandbox.scenes.ShapesScene
import com.example.sandbox.scenes.TextBoxScene
import com.example.sandbox.scenes.TextScene
import com.example.sandbox.scenes.TextureTileScene
import com.example.sandbox.scenes.TimelineScene
import com.example.sandbox.scenes.UVShaders
import com.example.sandbox.scenes.UiScene
import com.example.sandbox.scenes.UiSceneViewModel
import com.example.sandbox.scenes.UltravioletScene
import example.TestFont
import indigo.*
import indigo.json.Json
import indigo.scenes._
import indigo.syntax.*
import indigoextras.effectmaterials.LegacyEffects
import indigoextras.effectmaterials.Refraction
import indigoextras.subsystems.FPSCounter
import indigoextras.ui.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("IndigoGame")
object SandboxGame extends IndigoGame[SandboxBootData, SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  val magnificationLevel: Int = 2
  val gameWidth: Int          = 228
  val gameHeight: Int         = 128
  val viewportWidth: Int      = gameWidth * magnificationLevel  // 456
  val viewportHeight: Int     = gameHeight * magnificationLevel // 256

  def initialScene(bootData: SandboxBootData): Option[SceneName] =
    Some(ConfettiScene.name)

  def scenes(bootData: SandboxBootData): NonEmptyList[Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]] =
    NonEmptyList(
      OriginalScene,
      ShapesScene,
      LightsScene,
      RefractionScene,
      LegacyEffectsScene,
      TextBoxScene,
      BoundsScene,
      CameraScene,
      TextureTileScene,
      UiScene,
      ConfettiScene,
      MutantsScene,
      CratesScene,
      ClipScene,
      TextScene,
      BoxesScene,
      ManyEventHandlers,
      TimelineScene,
      UltravioletScene,
      PointersScene,
      BoundingCircleScene,
      LineReflectionScene,
      CameraWithCloneTilesScene,
      PathFindingScene,
      CaptureScreenScene
    )

  val eventFilters: EventFilters = EventFilters.Permissive

  def boot(flags: Map[String, String]): Outcome[BootResult[SandboxBootData, SandboxGameModel]] = {
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
          clearColor = RGBA(0.4, 0.2, 0.5, 1),
          magnification = magnificationLevel
        ),
        SandboxBootData(flags.getOrElse("key", "No entry for 'key'."), gameViewport)
      ).withAssets(
        SandboxAssets.assets ++
          Shaders.assets ++
          Archetype.assets
      ).withFonts(
        Fonts.fontInfo,
        TestFont.fontInfo
      ).withSubSystems(
        FPSCounter[SandboxGameModel](
          Point(5, 165),
          BindingKey("fps counter")
        )
      ).withShaders(
        Shaders.circle,
        Shaders.external,
        Shaders.sea,
        LegacyEffects.entityShader,
        Archetype.shader,
        UVShaders.circle,
        UVShaders.voronoi,
        UVShaders.redBlend
      ).addShaders(Refraction.shaders)
    )
  }

  def setup(
      bootData: SandboxBootData,
      assetCollection: AssetCollection,
      dice: Dice
  ): Outcome[Startup[SandboxStartupData]] = {
    println(bootData.message)

    val screenCenter: Point =
      bootData.gameViewport.giveDimensions(magnificationLevel).center

    def makeStartupData(
        aseprite: Aseprite,
        spriteAndAnimations: SpriteAndAnimations,
        clips: Map[CycleLabel, Clip[Material.Bitmap]]
    ): Startup.Success[SandboxStartupData] =
      Startup
        .Success(
          SandboxStartupData(
            Dude(
              aseprite,
              spriteAndAnimations.sprite
                .withDepth(Depth(3))
                .withRef(16, 16)      // Initial offset, so when talk about his position it's the center of the sprite
                .moveTo(screenCenter) // Also place him in the middle of the screen initially
                .withMaterial(SandboxAssets.dudeMaterial),
              clips
            ),
            screenCenter,
            bootData.gameViewport
          )
        )
        .addAnimations(spriteAndAnimations.animations)

    val res: Option[Startup.Success[SandboxStartupData]] = for {
      json                <- assetCollection.findTextDataByName(AssetName(SandboxAssets.dudeName.toString + "-json"))
      aseprite            <- Json.asepriteFromJson(json)
      spriteAndAnimations <- aseprite.toSpriteAndAnimations(dice, SandboxAssets.dudeName)
      clips               <- aseprite.toClips(SandboxAssets.dudeName)
    } yield makeStartupData(aseprite, spriteAndAnimations, clips)

    Outcome(res.getOrElse(Startup.Failure("Failed to load the dude")))
  }

  def initialModel(startupData: SandboxStartupData): Outcome[SandboxGameModel] =
    Outcome(SandboxModel.initialModel(startupData))

  def initialViewModel(startupData: SandboxStartupData, model: SandboxGameModel): Outcome[SandboxViewModel] = {
    val assets =
      InputFieldAssets(
        Text("placeholder", 0, 0, 0, Fonts.fontKey, SandboxAssets.fontMaterial).alignLeft,
        Graphic(0, 0, 16, 16, 2, Material.ImageEffects(SandboxAssets.smallFontName).withTint(RGB(0, 0, 1)))
          .withCrop(188, 78, 14, 23)
      )

    Outcome(
      SandboxViewModel(
        Point.zero,
        InputField("single", assets).withKey(BindingKey("single")).makeSingleLine,
        InputField("multi\nline", assets).withKey(BindingKey("multi")).makeMultiLine.moveTo(5, 5),
        true,
        UiSceneViewModel.initial,
        CaptureScreenSceneViewModel(None, None, Point.zero)
      )
    )
  }

  def updateModel(
      context: Context[SandboxStartupData],
      model: SandboxGameModel
  ): GlobalEvent => Outcome[SandboxGameModel] =
    SandboxModel.updateModel(model)

  def updateViewModel(
      context: Context[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): GlobalEvent => Outcome[SandboxViewModel] = {
    case RendererDetails(RenderingTechnology.WebGL1, _, _) =>
      Outcome(viewModel.copy(useLightingLayer = false))

    case FrameTick =>
      val updateOffset: Point =
        context.frame.input.gamepad.dpad match {
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

      // more stuff
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

    case KeyboardEvent.KeyDown(Key.PAGE_UP) =>
      Outcome(viewModel)
        .addGlobalEvents(SceneEvent.LoopNext)

    case KeyboardEvent.KeyDown(Key.PAGE_DOWN) =>
      Outcome(viewModel)
        .addGlobalEvents(SceneEvent.LoopPrevious)

    case KeyboardEvent.KeyDown(Key.HOME) =>
      Outcome(viewModel)
        .addGlobalEvents(SceneEvent.First)

    case KeyboardEvent.KeyDown(Key.END) =>
      Outcome(viewModel)
        .addGlobalEvents(SceneEvent.Last)

    case _ =>
      Outcome(viewModel)
  }

  def present(
      context: Context[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        "fps counter".bindingKey ->
          Layer.empty
            .withDepth(200.depth)
            .withCamera(Camera.default)
      )
    )

final case class Dude(
    aseprite: Aseprite,
    sprite: Sprite[Material.ImageEffects],
    clips: Map[CycleLabel, Clip[Material.Bitmap]]
)
final case class SandboxBootData(message: String, gameViewport: GameViewport)
final case class SandboxStartupData(dude: Dude, viewportCenter: Point, gameViewport: GameViewport)
final case class SandboxViewModel(
    offset: Point,
    single: InputField,
    multi: InputField,
    useLightingLayer: Boolean,
    uiScene: UiSceneViewModel,
    captureScreenScene: CaptureScreenSceneViewModel
)

final case class Log(message: String) extends GlobalEvent
