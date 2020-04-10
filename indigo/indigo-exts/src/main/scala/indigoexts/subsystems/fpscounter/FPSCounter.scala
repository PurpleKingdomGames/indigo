package indigoexts.subsystems.fpscounter

import indigo._
import indigoexts.subsystems.SubSystem

@SuppressWarnings(Array("org.wartremover.warts.Var"))
final class FPSCounter(fontKey: FontKey, position: Point, targetFPS: Int) extends SubSystem {

  var fps: Int                     = 0
  var lastInterval: Millis         = Millis(0)
  var frameCountSinceInterval: Int = 0

  type EventType = GlobalEvent

  val eventFilter: GlobalEvent => Option[GlobalEvent] = {
    case e: FrameTick => Option(e)
    case _            => None
  }

  def update(gameTime: GameTime, dice: Dice): GlobalEvent => Outcome[FPSCounter] = {
    case FrameTick =>
      if (gameTime.running >= (this.lastInterval + Millis(1000))) {
        fps = Math.min(targetFPS, frameCountSinceInterval + 1)
        lastInterval = gameTime.running
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

  def render(gameTime: GameTime): SceneUpdateFragment =
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
