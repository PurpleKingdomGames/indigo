package indigo.shared.scenegraph

import indigo.shared.datatypes.Flip
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Size
import indigo.shared.datatypes.Vector2
import indigo.shared.events.GlobalEvent
import indigo.shared.materials.Material
import indigo.shared.shader.ShaderData
import indigo.shared.shader.StandardShaders
import indigo.shared.shader.ToUniformBlock
import indigo.shared.shader.UniformBlock
import indigo.shared.time.FPS
import indigo.shared.time.Seconds

final case class Clip[M <: Material](
    size: Size,
    sheet: ClipSheet,
    playMode: ClipPlayMode,
    material: M,
    eventHandlerEnabled: Boolean,
    eventHandler: ((Clip[?], GlobalEvent)) => Option[GlobalEvent],
    position: Point,
    rotation: Radians,
    scale: Vector2,
    ref: Point,
    flip: Flip
) extends EntityNode[Clip[M]]
    with Cloneable
    with SpatialModifiers[Clip[M]] derives CanEqual:

  export sheet.frameCount
  export sheet.frameDuration

  lazy val length: Seconds =
    sheet.frameDuration * sheet.frameCount

  def loop: Clip[M] =
    this.copy(playMode = ClipPlayMode.Loop(playMode.direction))

  def playOnce: Clip[M] =
    this.copy(playMode = ClipPlayMode.PlayOnce(playMode.direction, Seconds.zero))
  def playOnce(startTime: Seconds): Clip[M] =
    this.copy(playMode = ClipPlayMode.PlayOnce(playMode.direction, startTime))

  def play: Clip[M] =
    loop
  def play(startTime: Seconds): Clip[M] =
    this.copy(playMode = ClipPlayMode.PlayOnce(playMode.direction, startTime))
  def play(startTime: Seconds, numOfTimes: Int): Clip[M] =
    this.copy(playMode = ClipPlayMode.PlayCount(playMode.direction, startTime, numOfTimes))

  def forwards: Clip[M]       = this.copy(playMode = playMode.forwards)
  def backwards: Clip[M]      = this.copy(playMode = playMode.backwards)
  def reverse: Clip[M]        = this.copy(playMode = playMode.reverse)
  def pingPong: Clip[M]       = this.copy(playMode = playMode.pingPong)
  def smoothPingPong: Clip[M] = this.copy(playMode = playMode.smoothPingPong)

  def withFrameCount(newFrameCount: Int): Clip[M] =
    this.copy(sheet = sheet.withFrameCount(newFrameCount))

  def withFrameDuration(newFrameDuration: Seconds): Clip[M] =
    this.copy(sheet = sheet.withFrameDuration(newFrameDuration))

  def withWrapAt(newWrapAt: Int): Clip[M] =
    this.copy(sheet = sheet.withWrapAt(newWrapAt))

  def withArrangement(newArrangement: ClipSheetArrangement): Clip[M] =
    this.copy(sheet = sheet.withArrangement(newArrangement))

  def withStartOffset(newStartOffset: Int): Clip[M] =
    this.copy(sheet = sheet.withStartOffset(newStartOffset))

  def withFPS(fps: FPS): Clip[M] =
    this.copy(sheet = sheet.withFPS(fps))

  lazy val x: Int = position.x
  lazy val y: Int = position.y

  def withMaterial[MB <: Material](newMaterial: MB): Clip[MB] =
    this.copy(material = newMaterial)
  def modifyMaterial[MB <: Material](alter: M => MB): Clip[MB] =
    this.copy(material = alter(material))

  def withSize(newSize: Size): Clip[M] =
    this.copy(size = newSize)
  def withSize(width: Int, height: Int): Clip[M] =
    withSize(Size(width, height))

  def withSheet(newSheet: ClipSheet): Clip[M] =
    this.copy(sheet = newSheet)
  def modifySheet(alter: ClipSheet => ClipSheet): Clip[M] =
    this.copy(sheet = alter(sheet))

  def withPlayMode(newPlayMode: ClipPlayMode): Clip[M] =
    this.copy(playMode = newPlayMode)
  def modifyPlayMode(alter: ClipPlayMode => ClipPlayMode): Clip[M] =
    this.copy(playMode = alter(playMode))

  def moveTo(pt: Point): Clip[M] =
    this.copy(position = pt)
  def moveTo(x: Int, y: Int): Clip[M] =
    moveTo(Point(x, y))
  def withPosition(newPosition: Point): Clip[M] =
    moveTo(newPosition)

  def moveBy(pt: Point): Clip[M] =
    this.copy(position = position + pt)
  def moveBy(x: Int, y: Int): Clip[M] =
    moveBy(Point(x, y))

  def rotateTo(angle: Radians): Clip[M] =
    this.copy(rotation = angle)
  def rotateBy(angle: Radians): Clip[M] =
    rotateTo(rotation + angle)
  def withRotation(newRotation: Radians): Clip[M] =
    rotateTo(newRotation)

  def scaleBy(amount: Vector2): Clip[M] =
    this.copy(scale = scale * amount)
  def scaleBy(x: Double, y: Double): Clip[M] =
    scaleBy(Vector2(x, y))
  def withScale(newScale: Vector2): Clip[M] =
    this.copy(scale = newScale)

  def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): Clip[M] =
    this.copy(position = newPosition, rotation = newRotation, scale = newScale)

  def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): Clip[M] =
    transformTo(position + positionDiff, rotation + rotationDiff, scale * scaleDiff)

  def flipHorizontal(isFlipped: Boolean): Clip[M] =
    this.copy(flip = flip.withHorizontalFlip(isFlipped))
  def flipVertical(isFlipped: Boolean): Clip[M] =
    this.copy(flip = flip.withVerticalFlip(isFlipped))
  def withFlip(newFlip: Flip): Clip[M] =
    this.copy(flip = newFlip)

  def withRef(newRef: Point): Clip[M] =
    this.copy(ref = newRef)
  def withRef(x: Int, y: Int): Clip[M] =
    withRef(Point(x, y))

  lazy val toShaderData: ShaderData =
    val data = material.toShaderData
    data
      .withShaderId(StandardShaders.shaderIdToClipShaderId(data.shaderId))
      .addUniformData(
        Clip.IndigoClipData(
          sheet.frameCount,
          sheet.frameDuration,
          sheet.wrapAt,
          sheet.arrangement.toInt,
          sheet.startOffset,
          playMode.direction.toInt,
          playMode.giveStartTime,
          playMode.giveTimes
        )
      )

  def withEventHandler(f: ((Clip[?], GlobalEvent)) => Option[GlobalEvent]): Clip[M] =
    this.copy(eventHandler = f, eventHandlerEnabled = true)
  def onEvent(f: PartialFunction[(Clip[?], GlobalEvent), GlobalEvent]): Clip[M] =
    withEventHandler(f.lift)
  def enableEvents: Clip[M] =
    this.copy(eventHandlerEnabled = true)
  def disableEvents: Clip[M] =
    this.copy(eventHandlerEnabled = false)

  def toGraphic: Graphic[M] =
    toGraphic(0)

  def toGraphic(frameNumber: Int): Graphic[M] =
    val num   = Math.min(sheet.frameCount - 1, Math.max(0, frameNumber))
    val frame = num + sheet.startOffset
    def framePositon: Point =
      sheet.arrangement match
        case ClipSheetArrangement.Horizontal =>
          Point(
            x = frame % sheet.wrapAt,
            y = frame / sheet.wrapAt
          )

        case ClipSheetArrangement.Vertical =>
          Point(
            x = frame / sheet.wrapAt,
            y = frame % sheet.wrapAt
          )

    Graphic(
      eventHandlerEnabled = false,
      eventHandler = Function.const(None),
      position = position,
      rotation = rotation,
      scale = scale,
      ref = ref,
      flip = flip,
      crop = Rectangle(framePositon * size.toPoint, size),
      material = material
    )

  def scrubTo(position: Double): Clip[M] =
    val p = Math.min(1.0d, Math.max(0.0d, position))
    toFrame((sheet.frameCount * p).toInt)

  def toFrame(frameNumber: Int): Clip[M] =
    this.copy(
      sheet = sheet
        .withStartOffset(frameNumber)
        .withFrameCount(1)
    )

object Clip:

  final case class IndigoClipData(
      CLIP_SHEET_FRAME_COUNT: Int,
      CLIP_SHEET_FRAME_DURATION: Seconds,
      CLIP_SHEET_WRAP_AT: Int,
      CLIP_SHEET_ARRANGEMENT: Int,
      CLIP_SHEET_START_OFFSET: Int,
      CLIP_PLAY_DIRECTION: Int,
      CLIP_PLAYMODE_START_TIME: Seconds,
      CLIP_PLAYMODE_TIMES: Int
  ) derives ToUniformBlock

  def apply[M <: Material](
      width: Int,
      height: Int,
      sheet: ClipSheet,
      playMode: ClipPlayMode,
      material: M
  ): Clip[M] =
    Clip(
      size = Size(width, height),
      sheet = sheet,
      playMode = playMode,
      material = material,
      eventHandlerEnabled = false,
      eventHandler = Function.const(None),
      position = Point.zero,
      rotation = Radians.zero,
      scale = Vector2.one,
      ref = Point.zero,
      flip = Flip.default
    )

  def apply[M <: Material](
      width: Int,
      height: Int,
      sheet: ClipSheet,
      material: M
  ): Clip[M] =
    Clip(
      size = Size(width, height),
      sheet = sheet,
      playMode = ClipPlayMode.default,
      material = material,
      eventHandlerEnabled = false,
      eventHandler = Function.const(None),
      position = Point.zero,
      rotation = Radians.zero,
      scale = Vector2.one,
      ref = Point.zero,
      flip = Flip.default
    )

  def apply[M <: Material](
      x: Int,
      y: Int,
      width: Int,
      height: Int,
      sheet: ClipSheet,
      playMode: ClipPlayMode,
      material: M
  ): Clip[M] =
    Clip(
      size = Size(width, height),
      sheet = sheet,
      playMode = playMode,
      material = material,
      eventHandlerEnabled = false,
      eventHandler = Function.const(None),
      position = Point(x, y),
      rotation = Radians.zero,
      scale = Vector2.one,
      ref = Point.zero,
      flip = Flip.default
    )

  def apply[M <: Material](
      x: Int,
      y: Int,
      width: Int,
      height: Int,
      sheet: ClipSheet,
      material: M
  ): Clip[M] =
    Clip(
      size = Size(width, height),
      sheet = sheet,
      playMode = ClipPlayMode.default,
      material = material,
      eventHandlerEnabled = false,
      eventHandler = Function.const(None),
      position = Point(x, y),
      rotation = Radians.zero,
      scale = Vector2.one,
      ref = Point.zero,
      flip = Flip.default
    )

  def apply[M <: Material](
      size: Size,
      sheet: ClipSheet,
      playMode: ClipPlayMode,
      material: M
  ): Clip[M] =
    Clip(
      size = size,
      sheet = sheet,
      playMode = playMode,
      material = material,
      eventHandlerEnabled = false,
      eventHandler = Function.const(None),
      position = Point.zero,
      rotation = Radians.zero,
      scale = Vector2.one,
      ref = Point.zero,
      flip = Flip.default
    )

  def apply[M <: Material](
      size: Size,
      sheet: ClipSheet,
      material: M
  ): Clip[M] =
    Clip(
      size = size,
      sheet = sheet,
      playMode = ClipPlayMode.default,
      material = material,
      eventHandlerEnabled = false,
      eventHandler = Function.const(None),
      position = Point.zero,
      rotation = Radians.zero,
      scale = Vector2.one,
      ref = Point.zero,
      flip = Flip.default
    )

  def apply[M <: Material](
      position: Point,
      size: Size,
      sheet: ClipSheet,
      playMode: ClipPlayMode,
      material: M
  ): Clip[M] =
    Clip(
      size = size,
      sheet = sheet,
      playMode = playMode,
      material = material,
      eventHandlerEnabled = false,
      eventHandler = Function.const(None),
      position = position,
      rotation = Radians.zero,
      scale = Vector2.one,
      ref = Point.zero,
      flip = Flip.default
    )

  def apply[M <: Material](
      position: Point,
      size: Size,
      sheet: ClipSheet,
      material: M
  ): Clip[M] =
    Clip(
      size = size,
      sheet = sheet,
      playMode = ClipPlayMode.default,
      material = material,
      eventHandlerEnabled = false,
      eventHandler = Function.const(None),
      position = position,
      rotation = Radians.zero,
      scale = Vector2.one,
      ref = Point.zero,
      flip = Flip.default
    )

enum ClipSheetArrangement derives CanEqual:
  case Horizontal, Vertical

  // Using this instead of `ordinal` so that order shouldn't break implementation...
  def toInt: Int =
    this match
      case ClipSheetArrangement.Horizontal => 0
      case ClipSheetArrangement.Vertical   => 1

object ClipSheetArrangement:
  val default: ClipSheetArrangement =
    ClipSheetArrangement.Horizontal

final case class ClipSheet(
    frameCount: Int,
    frameDuration: Seconds,
    wrapAt: Int,
    arrangement: ClipSheetArrangement,
    startOffset: Int
):
  def withFrameCount(newFrameCount: Int): ClipSheet =
    this.copy(frameCount = newFrameCount)

  def withFrameDuration(newFrameDuration: Seconds): ClipSheet =
    this.copy(frameDuration = newFrameDuration)

  def withWrapAt(newWrapAt: Int): ClipSheet =
    this.copy(wrapAt = newWrapAt)

  def withArrangement(newArrangement: ClipSheetArrangement): ClipSheet =
    this.copy(arrangement = newArrangement)

  def withStartOffset(newStartOffset: Int): ClipSheet =
    this.copy(startOffset = newStartOffset)

  def withFPS(fps: FPS): ClipSheet =
    withFrameDuration(fps.toSeconds)

object ClipSheet:

  def apply(frameCount: Int, frameDuration: Seconds): ClipSheet =
    ClipSheet(frameCount, frameDuration, frameCount, ClipSheetArrangement.default, 0)

  def apply(frameCount: Int, fps: FPS): ClipSheet =
    ClipSheet(frameCount, fps.toSeconds, frameCount, ClipSheetArrangement.default, 0)

  def apply(frameCount: Int, frameDuration: Seconds, wrapAt: Int): ClipSheet =
    ClipSheet(frameCount, frameDuration, wrapAt, ClipSheetArrangement.default, 0)

  def apply(frameCount: Int, fps: FPS, wrapAt: Int): ClipSheet =
    ClipSheet(frameCount, fps.toSeconds, wrapAt, ClipSheetArrangement.default, 0)

  def apply(frameCount: Int, frameDuration: Seconds, wrapAt: Int, arrangement: ClipSheetArrangement): ClipSheet =
    ClipSheet(frameCount, frameDuration, wrapAt, arrangement, 0)

  def apply(frameCount: Int, fps: FPS, wrapAt: Int, arrangement: ClipSheetArrangement): ClipSheet =
    ClipSheet(frameCount, fps.toSeconds, wrapAt, arrangement, 0)

enum ClipPlayDirection derives CanEqual:
  case Forward, Backward, PingPong, SmoothPingPong

  // Using this instead of `ordinal` so that order shouldn't break implementation...
  def toInt: Int =
    this match
      case ClipPlayDirection.Forward        => 0
      case ClipPlayDirection.Backward       => 1
      case ClipPlayDirection.PingPong       => 2
      case ClipPlayDirection.SmoothPingPong => 3

  def reverse: ClipPlayDirection =
    this match
      case ClipPlayDirection.Forward  => ClipPlayDirection.Backward
      case ClipPlayDirection.Backward => ClipPlayDirection.Forward
      case d                          => d

object ClipPlayDirection:
  val default: ClipPlayDirection =
    ClipPlayDirection.Forward

enum ClipPlayMode derives CanEqual:
  val direction: ClipPlayDirection

  case Loop(direction: ClipPlayDirection)                                      extends ClipPlayMode
  case PlayOnce(direction: ClipPlayDirection, startTime: Seconds)              extends ClipPlayMode
  case PlayCount(direction: ClipPlayDirection, startTime: Seconds, times: Int) extends ClipPlayMode

  def giveStartTime: Seconds =
    this match
      case _: ClipPlayMode.Loop      => Seconds.zero
      case x: ClipPlayMode.PlayOnce  => x.startTime
      case x: ClipPlayMode.PlayCount => x.startTime

  def giveTimes: Int =
    this match
      case _: ClipPlayMode.Loop      => -1
      case _: ClipPlayMode.PlayOnce  => 1
      case x: ClipPlayMode.PlayCount => x.times

  def forwards: ClipPlayMode =
    this match
      case x: ClipPlayMode.Loop      => ClipPlayMode.Loop(ClipPlayDirection.Forward)
      case x: ClipPlayMode.PlayOnce  => ClipPlayMode.PlayOnce(ClipPlayDirection.Forward, x.startTime)
      case x: ClipPlayMode.PlayCount => ClipPlayMode.PlayCount(ClipPlayDirection.Forward, x.startTime, x.times)

  def backwards: ClipPlayMode =
    this match
      case x: ClipPlayMode.Loop      => ClipPlayMode.Loop(ClipPlayDirection.Backward)
      case x: ClipPlayMode.PlayOnce  => ClipPlayMode.PlayOnce(ClipPlayDirection.Backward, x.startTime)
      case x: ClipPlayMode.PlayCount => ClipPlayMode.PlayCount(ClipPlayDirection.Backward, x.startTime, x.times)

  def reverse: ClipPlayMode =
    this match
      case x: ClipPlayMode.Loop      => ClipPlayMode.Loop(x.direction.reverse)
      case x: ClipPlayMode.PlayOnce  => ClipPlayMode.PlayOnce(x.direction.reverse, x.startTime)
      case x: ClipPlayMode.PlayCount => ClipPlayMode.PlayCount(x.direction.reverse, x.startTime, x.times)

  def pingPong: ClipPlayMode =
    this match
      case x: ClipPlayMode.Loop      => ClipPlayMode.Loop(ClipPlayDirection.PingPong)
      case x: ClipPlayMode.PlayOnce  => ClipPlayMode.PlayOnce(ClipPlayDirection.PingPong, x.startTime)
      case x: ClipPlayMode.PlayCount => ClipPlayMode.PlayCount(ClipPlayDirection.PingPong, x.startTime, x.times)

  def smoothPingPong: ClipPlayMode =
    this match
      case x: ClipPlayMode.Loop      => ClipPlayMode.Loop(ClipPlayDirection.SmoothPingPong)
      case x: ClipPlayMode.PlayOnce  => ClipPlayMode.PlayOnce(ClipPlayDirection.SmoothPingPong, x.startTime)
      case x: ClipPlayMode.PlayCount => ClipPlayMode.PlayCount(ClipPlayDirection.SmoothPingPong, x.startTime, x.times)

object ClipPlayMode:
  val default: ClipPlayMode =
    ClipPlayMode.Loop(ClipPlayDirection.default)

  val loop: ClipPlayMode =
    ClipPlayMode.Loop(ClipPlayDirection.default)

  def playOnce(startTime: Seconds): ClipPlayMode =
    ClipPlayMode.PlayOnce(ClipPlayDirection.default, startTime)

  def playCount(startTime: Seconds, times: Int): ClipPlayMode =
    ClipPlayMode.PlayCount(ClipPlayDirection.default, startTime, times)
