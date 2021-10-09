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
import indigo.shared.time.Seconds

object FPSCounter {

  def apply(
      position: Point,
      targetFPS: Int,
      layerKey: Option[BindingKey],
      fontFamily: FontFamily,
      fontSize: Pixels
  ): SubSystem =
    SubSystem[GlobalEvent, FPSCounterState](
      _eventFilter = eventFilter,
      _initialModel = Outcome(FPSCounterState.default),
      _update = update(targetFPS),
      _present = present(position, targetFPS, layerKey, fontFamily, fontSize)
    )

  def apply(position: Point, targetFPS: Int, layerKey: Option[BindingKey]): SubSystem =
    SubSystem[GlobalEvent, FPSCounterState](
      _eventFilter = eventFilter,
      _initialModel = Outcome(FPSCounterState.default),
      _update = update(targetFPS),
      _present = present(position, targetFPS, layerKey, FontFamily.sansSerif, Pixels(12))
    )

  def apply(position: Point, targetFPS: Int, layerKey: BindingKey): SubSystem =
    SubSystem[GlobalEvent, FPSCounterState](
      _eventFilter = eventFilter,
      _initialModel = Outcome(FPSCounterState.default),
      _update = update(targetFPS),
      _present = present(position, targetFPS, Option(layerKey), FontFamily.sansSerif, Pixels(12))
    )

  def apply(position: Point, targetFPS: Int): SubSystem =
    SubSystem[GlobalEvent, FPSCounterState](
      _eventFilter = eventFilter,
      _initialModel = Outcome(FPSCounterState.default),
      _update = update(targetFPS),
      _present = present(position, targetFPS, None, FontFamily.sansSerif, Pixels(12))
    )

  lazy val eventFilter: GlobalEvent => Option[GlobalEvent] = {
    case FrameTick => Option(FrameTick)
    case _         => None
  }

  def update(targetFPS: Int): (SubSystemFrameContext, FPSCounterState) => GlobalEvent => Outcome[FPSCounterState] =
    (frameContext, model) => {
      case FrameTick =>
        if (frameContext.gameTime.running >= (model.lastInterval + Seconds(1)))
          Outcome(
            FPSCounterState(
              fps = Math.min(targetFPS, model.frameCountSinceInterval + 1),
              lastInterval = frameContext.gameTime.running,
              frameCountSinceInterval = 0
            )
          )
        else
          Outcome(model.copy(frameCountSinceInterval = model.frameCountSinceInterval + 1))

      case _ =>
        Outcome(model)
    }

  def present(
      position: Point,
      targetFPS: Int,
      layerKey: Option[BindingKey],
      fontFamily: FontFamily,
      fontSize: Pixels
  ): (SubSystemFrameContext, FPSCounterState) => Outcome[SceneUpdateFragment] =
    (ctx, model) => {
      val text: TextBox =
        TextBox(s"""FPS ${model.fps.toString}""")
          .withFontFamily(fontFamily)
          .withColor(pickTint(targetFPS, model.fps))
          .withFontSize(fontSize)
          .moveTo(position + 2)

      val size: Rectangle =
        ctx.boundaryLocator
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
          .Box(Rectangle(position, boxSize), Fill.Color(RGBA.Black.withAlpha(0.5)))

      Outcome(
        SceneUpdateFragment(
          layerKey match {
            case None      => Layer(bg, text.withSize(size.size))
            case Some(key) => Layer(key, bg, text.withSize(size.size))
          }
        )
      )
    }

  def pickTint(targetFPS: Int, fps: Int): RGBA =
    if (fps > targetFPS - (targetFPS * 0.05)) RGBA.Green
    else if (fps > targetFPS / 2) RGBA.Yellow
    else RGBA.Red

}

final case class FPSCounterState(fps: Int, lastInterval: Seconds, frameCountSinceInterval: Int)
object FPSCounterState {
  def default: FPSCounterState =
    FPSCounterState(0, Seconds.zero, 0)
}
