package indigoextras.subsystems

import indigo.shared.Outcome
import indigo.shared.datatypes.BindingKey
import indigo.shared.datatypes.Fill
import indigo.shared.datatypes.FontFamily
import indigo.shared.datatypes.Pixels
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.RGBA
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Size
import indigo.shared.events.FrameTick
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.Layer
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.scenegraph.Shape
import indigo.shared.scenegraph.TextBox
import indigo.shared.subsystems.SubSystem
import indigo.shared.subsystems.SubSystemFrameContext
import indigo.shared.time.FPS
import indigo.shared.time.Seconds

final case class FPSCounter(
    startPosition: Point,
    targetFPS: Option[FPS],
    layerKey: Option[BindingKey],
    fontFamily: FontFamily,
    fontSize: Pixels
) extends SubSystem:
  type EventType      = GlobalEvent
  type SubSystemModel = FPSCounterState

  private val idealFps: Int = targetFPS.getOrElse(FPS.`60`).toInt
  private val decideNextFps: Int => Int =
    targetFPS match
      case None =>
        frameCountSinceInterval => frameCountSinceInterval + 1

      case Some(target) =>
        frameCountSinceInterval => Math.min(target.toInt, frameCountSinceInterval + 1)

  def eventFilter: GlobalEvent => Option[EventType] = {
    case FrameTick          => Some(FrameTick)
    case e: FPSCounter.Move => Some(e)
    case _                  => None
  }

  def initialModel: Outcome[SubSystemModel] =
    Outcome(FPSCounterState.initial(startPosition))

  def update(context: SubSystemFrameContext, model: FPSCounterState): GlobalEvent => Outcome[FPSCounterState] = {
    case FrameTick =>
      if (context.gameTime.running >= (model.lastInterval + Seconds(1)))
        Outcome(
          model.copy(
            fps = decideNextFps(model.frameCountSinceInterval),
            lastInterval = context.gameTime.running,
            frameCountSinceInterval = 0
          )
        )
      else Outcome(model.copy(frameCountSinceInterval = model.frameCountSinceInterval + 1))

    case FPSCounter.Move(to) =>
      Outcome(model.copy(position = to))

    case _ =>
      Outcome(model)
  }

  private val textBox: TextBox =
    TextBox("")
      .withFontFamily(fontFamily)
      .withFontSize(fontSize)

  def present(context: SubSystemFrameContext, model: FPSCounterState): Outcome[SceneUpdateFragment] =
    val text: TextBox =
      textBox
        .withText(s"""FPS ${model.fps.toString}""")
        .withColor(pickTint(idealFps, model.fps))
        .moveTo(model.position + 2)

    val size: Rectangle =
      context.boundaryLocator
        .measureText(text)

    val boxSize =
      ({ (s: Size) =>
        Size(
          if s.width  % 2 == 0 then s.width else s.width + 1,
          if s.height % 2 == 0 then s.height else s.height + 1
        )
      })(size.expand(2).size)

    val bg: Shape.Box =
      Shape
        .Box(Rectangle(model.position, boxSize), Fill.Color(RGBA.Black.withAlpha(0.5)))

    Outcome(
      SceneUpdateFragment(
        layerKey match {
          case None      => Layer(bg, text.withSize(size.size))
          case Some(key) => Layer(key, bg, text.withSize(size.size))
        }
      )
    )

  def pickTint(targetFPS: Int, fps: Int): RGBA =
    if (fps > targetFPS - (targetFPS * 0.05)) RGBA.Green
    else if (fps > targetFPS / 2) RGBA.Yellow
    else RGBA.Red

object FPSCounter:
  def apply(position: Point): SubSystem =
    FPSCounter(position, None, None, FontFamily.sansSerif, Pixels(12))

  def apply(position: Point, targetFPS: FPS): SubSystem =
    FPSCounter(position, Option(targetFPS), None, FontFamily.sansSerif, Pixels(12))

  def apply(position: Point, layerKey: BindingKey): SubSystem =
    FPSCounter(position, None, Option(layerKey), FontFamily.sansSerif, Pixels(12))

  def apply(position: Point, targetFPS: FPS, layerKey: BindingKey): SubSystem =
    FPSCounter(position, Option(targetFPS), Option(layerKey), FontFamily.sansSerif, Pixels(12))

  final case class Move(to: Point) extends GlobalEvent

final case class FPSCounterState(position: Point, fps: Int, lastInterval: Seconds, frameCountSinceInterval: Int)
object FPSCounterState:
  def initial(initialPosition: Point): FPSCounterState =
    FPSCounterState(initialPosition, 0, Seconds.zero, 0)
