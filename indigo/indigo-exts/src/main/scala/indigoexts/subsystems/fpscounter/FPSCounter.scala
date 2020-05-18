package indigoexts.subsystems.fpscounter

import indigo._
import indigoexts.subsystems.SubSystem

@SuppressWarnings(Array("org.wartremover.warts.Var"))
final class FPSCounter(fontKey: FontKey, position: Point, targetFPS: Int) extends SubSystem {

  var fps: Int                     = 0
  var lastInterval: Seconds         = Seconds.zero
  var frameCountSinceInterval: Int = 0

  type EventType = GlobalEvent

  val eventFilter: GlobalEvent => Option[GlobalEvent] = {
    case e: FrameTick => Option(e)
    case _            => None
  }

  def update(frameContext: FrameContext): GlobalEvent => Outcome[FPSCounter] = {
    case FrameTick =>
      if (frameContext.gameTime.running >= (this.lastInterval + Seconds(1))) {
        fps = Math.min(targetFPS, frameCountSinceInterval + 1)
        lastInterval = frameContext.gameTime.running
        frameCountSinceInterval = 0
      } else {
        frameCountSinceInterval += 1
      }
      Outcome(this)
  }

  def pickTint: RGBA =
    if (fps > targetFPS - (targetFPS * 0.05)) RGBA.Green
    else if (fps > targetFPS / 2) RGBA.Yellow
    else RGBA.Red

  def render(frameContext: FrameContext): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addUiLayerNodes(Text(fpsCount, position.x, position.y, 1, fontKey).withTint(pickTint))

  def fpsCount(implicit showI: AsString[Int]): String =
    s"""FPS: ${showI.show(fps)}"""
}

object FPSCounter {

  def apply(fontKey: FontKey, position: Point, targetFPS: Int): FPSCounter =
    new FPSCounter(fontKey, position, targetFPS)

  def subSystem(fontKey: FontKey, position: Point, targetFPS: Int): FPSCounter =
    FPSCounter(fontKey, position, targetFPS)

}
