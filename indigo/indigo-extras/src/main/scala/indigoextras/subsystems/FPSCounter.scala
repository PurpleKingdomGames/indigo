package indigoextras.subsystems

import indigo.shared.subsystems.SubSystem
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.FontKey
import indigo.shared.time.Seconds
import indigo.shared.events.GlobalEvent
import indigo.shared.subsystems.SubSystemFrameContext
import indigo.shared.Outcome
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.datatypes.RGBA
import indigo.shared.scenegraph.Text
import indigo.shared.events.FrameTick
import indigo.shared.scenegraph.Layer
import indigo.shared.datatypes.Depth
import indigo.shared.materials.StandardMaterial

object FPSCounter {

  def apply(fontKey: FontKey, position: Point, targetFPS: Int, depth: Depth, material: StandardMaterial.ImageEffects): SubSystem =
    SubSystem[GlobalEvent, FPSCounterState](
      _eventFilter = eventFilter,
      _initialModel = Outcome(FPSCounterState.default),
      _update = update(targetFPS),
      _present = present(fontKey, position, targetFPS, depth, material)
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
    }

  def present(fontKey: FontKey, position: Point, targetFPS: Int, depth: Depth, material: StandardMaterial.ImageEffects): (SubSystemFrameContext, FPSCounterState) => Outcome[SceneUpdateFragment] =
    (_, model) => {
      Outcome(
        SceneUpdateFragment.empty
          .addLayer(
            Layer(
              Text(
                s"""FPS ${model.fps.toString}""",
                position.x,
                position.y,
                1,
                fontKey,
                material.withTint(pickTint(targetFPS, model.fps))
              )
            ).withDepth(depth)
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
