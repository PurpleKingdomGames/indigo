package com.example.sandbox.scenes

import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo._
import indigo.scenes._
import indigo.shared.scenegraph.SpatialModifiers

object ClipScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  def eventFilters: EventFilters =
    EventFilters.Permissive

  def modelLens: indigo.scenes.Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepLatest

  def name: SceneName =
    SceneName("clips")

  def subSystems: Set[SubSystem] =
    Set()

  def updateModel(
      context: FrameContext[SandboxStartupData],
      model: SandboxGameModel
  ): GlobalEvent => Outcome[SandboxGameModel] =
    _ => Outcome(model)

  def updateViewModel(
      context: FrameContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): GlobalEvent => Outcome[SandboxViewModel] =
    _ => Outcome(viewModel)

  def present(
      context: FrameContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        Graphic(Size(128), Material.Bitmap(SandboxAssets.trafficLightsName)),
        Clip(Point(80, 80), Size(64), ClipSheet(3, Seconds(0.25), 2)),
        Shape.Box(Rectangle(Point.zero, Size(64)), Fill.None, Stroke(1, RGBA.Green)).moveTo(Point(80, 80)),
        Clip(Point(144, 80), Size(64), ClipSheet(3, Seconds(0.5), 2)),
        Shape.Box(Rectangle(Point.zero, Size(64)), Fill.None, Stroke(1, RGBA.Green)).moveTo(Point(144, 80))
      )
    )

final case class Clip(
    size: Size,
    sheet: ClipSheet,
    playMode: ClipPlayMode,
    position: Point,
    rotation: Radians,
    scale: Vector2,
    depth: Depth,
    ref: Point,
    flip: Flip
) extends EntityNode
    with Cloneable
    with SpatialModifiers[Clip]
    derives CanEqual:

  def moveTo(pt: Point): Clip =
    this.copy(position = pt)
  def moveTo(x: Int, y: Int): Clip =
    moveTo(Point(x, y))
  def withPosition(newPosition: Point): Clip =
    moveTo(newPosition)

  def moveBy(pt: Point): Clip =
    this.copy(position = position + pt)
  def moveBy(x: Int, y: Int): Clip =
    moveBy(Point(x, y))

  def rotateTo(angle: Radians): Clip =
    this.copy(rotation = angle)
  def rotateBy(angle: Radians): Clip =
    rotateTo(rotation + angle)
  def withRotation(newRotation: Radians): Clip =
    rotateTo(newRotation)

  def scaleBy(amount: Vector2): Clip =
    this.copy(scale = scale * amount)
  def scaleBy(x: Double, y: Double): Clip =
    scaleBy(Vector2(x, y))
  def withScale(newScale: Vector2): Clip =
    this.copy(scale = newScale)

  def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): Clip =
    this.copy(position = newPosition, rotation = newRotation, scale = newScale)

  def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): Clip =
    transformTo(position + positionDiff, rotation + rotationDiff, scale * scaleDiff)

  def withDepth(newDepth: Depth): Clip =
    this.copy(depth = newDepth)

  def flipHorizontal(isFlipped: Boolean): Clip =
    this.copy(flip = flip.withHorizontalFlip(isFlipped))
  def flipVertical(isFlipped: Boolean): Clip =
    this.copy(flip = flip.withVerticalFlip(isFlipped))
  def withFlip(newFlip: Flip): Clip =
    this.copy(flip = newFlip)

  def withRef(newRef: Point): Clip =
    this.copy(ref = newRef)
  def withRef(x: Int, y: Int): Clip =
    withRef(Point(x, y))

  def toShaderData: ShaderData =
    ShaderData(Clip.shaderId)
      .withChannel0(SandboxAssets.trafficLightsName)
      .addUniformBlock(
        UniformBlock(
          "IndigoClipData",
          List(
            Uniform("CLIP_FRAME_COUNT")    -> float(sheet.frameCount),
            Uniform("CLIP_FRAME_DURATION") -> float.fromSeconds(sheet.frameDuration),
            Uniform("CLIP_WRAP_AT")        -> float(sheet.wrapAt)
          )
        )
      )

object Clip:

  def apply(
      width: Int,
      height: Int,
      sheet: ClipSheet,
      playMode: ClipPlayMode
  ): Clip =
    Clip(
      size = Size(width, height),
      sheet = sheet,
      playMode = playMode,
      position = Point.zero,
      rotation = Radians.zero,
      scale = Vector2.one,
      depth = Depth.zero,
      ref = Point.zero,
      flip = Flip.default
    )

  def apply(
      width: Int,
      height: Int,
      sheet: ClipSheet
  ): Clip =
    Clip(
      size = Size(width, height),
      sheet = sheet,
      playMode = ClipPlayMode.default,
      position = Point.zero,
      rotation = Radians.zero,
      scale = Vector2.one,
      depth = Depth.zero,
      ref = Point.zero,
      flip = Flip.default
    )

  def apply(
      x: Int,
      y: Int,
      width: Int,
      height: Int,
      sheet: ClipSheet,
      playMode: ClipPlayMode
  ): Clip =
    Clip(
      size = Size(width, height),
      sheet = sheet,
      playMode = playMode,
      position = Point(x, y),
      rotation = Radians.zero,
      scale = Vector2.one,
      depth = Depth.zero,
      ref = Point.zero,
      flip = Flip.default
    )

  def apply(
      x: Int,
      y: Int,
      width: Int,
      height: Int,
      sheet: ClipSheet
  ): Clip =
    Clip(
      size = Size(width, height),
      sheet = sheet,
      playMode = ClipPlayMode.default,
      position = Point(x, y),
      rotation = Radians.zero,
      scale = Vector2.one,
      depth = Depth.zero,
      ref = Point.zero,
      flip = Flip.default
    )

  def apply(
      size: Size,
      sheet: ClipSheet,
      playMode: ClipPlayMode
  ): Clip =
    Clip(
      size = size,
      sheet = sheet,
      playMode = playMode,
      position = Point.zero,
      rotation = Radians.zero,
      scale = Vector2.one,
      depth = Depth.zero,
      ref = Point.zero,
      flip = Flip.default
    )

  def apply(
      size: Size,
      sheet: ClipSheet
  ): Clip =
    Clip(
      size = size,
      sheet = sheet,
      playMode = ClipPlayMode.default,
      position = Point.zero,
      rotation = Radians.zero,
      scale = Vector2.one,
      depth = Depth.zero,
      ref = Point.zero,
      flip = Flip.default
    )

  def apply(
      position: Point,
      size: Size,
      sheet: ClipSheet,
      playMode: ClipPlayMode
  ): Clip =
    Clip(
      size = size,
      sheet = sheet,
      playMode = playMode,
      position = position,
      rotation = Radians.zero,
      scale = Vector2.one,
      depth = Depth.zero,
      ref = Point.zero,
      flip = Flip.default
    )

  def apply(
      position: Point,
      size: Size,
      sheet: ClipSheet
  ): Clip =
    Clip(
      size = size,
      sheet = sheet,
      playMode = ClipPlayMode.default,
      position = position,
      rotation = Radians.zero,
      scale = Vector2.one,
      depth = Depth.zero,
      ref = Point.zero,
      flip = Flip.default
    )

  // TODO: Remove?
  val shaderId: ShaderId =
    ShaderId("clip shader")

  val vertAsset: AssetName = AssetName("clip vertex")
  val fragAsset: AssetName = AssetName("clip fragment")

  val clipShader: EntityShader.External =
    EntityShader
      .External(shaderId)
      .withVertexProgram(vertAsset)
      .withFragmentProgram(fragAsset)

  def assets: Set[AssetType] =
    Set(
      AssetType.Text(vertAsset, AssetPath("assets/clip.vert")),
      AssetType.Text(fragAsset, AssetPath("assets/clip.frag"))
    )

enum ClipSheetArrangement:
  case Horizontal, Vertical

object ClipSheetArrangement:
  val default: ClipSheetArrangement =
    ClipSheetArrangement.Horizontal

final case class ClipSheet(frameCount: Int, frameDuration: Seconds, wrapAt: Int, arrangement: ClipSheetArrangement):
  def withFrameCount(newFrameCount: Int): ClipSheet =
    this.copy(frameCount = newFrameCount)

  def withFrameDuration(newFrameDuration: Seconds): ClipSheet =
    this.copy(frameDuration = newFrameDuration)

  def withWrapAt(newWrapAt: Int): ClipSheet =
    this.copy(wrapAt = newWrapAt)

  def withArrangement(newArrangement: ClipSheetArrangement): ClipSheet =
    this.copy(arrangement = newArrangement)

object ClipSheet:
  def apply(frameCount: Int, frameDuration: Seconds): ClipSheet =
    ClipSheet(frameCount, frameDuration, frameCount, ClipSheetArrangement.default)
  def apply(frameCount: Int, frameDuration: Seconds, wrapAt: Int): ClipSheet =
    ClipSheet(frameCount, frameDuration, wrapAt, ClipSheetArrangement.default)

enum ClipPlayDirection:
  case Forward, Backward, PingPong

object ClipPlayDirection:
  val default: ClipPlayDirection =
    ClipPlayDirection.Forward

enum ClipPlayMode:
  val direction: ClipPlayDirection

  case Loop(direction: ClipPlayDirection) extends ClipPlayMode
  case PlayOnce(direction: ClipPlayDirection, startTime: Seconds) extends ClipPlayMode
  case PlayCount(direction: ClipPlayDirection, startTime: Seconds, times: Int) extends ClipPlayMode

object ClipPlayMode:
  val default: ClipPlayMode =
    ClipPlayMode.Loop(ClipPlayDirection.default)
