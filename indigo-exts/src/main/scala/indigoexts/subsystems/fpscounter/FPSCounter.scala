package indigoexts.subsystems.fpscounter

import indigo._
import indigoexts.subsystems.SubSystem

final case class FPSCounter(fps: Int, lastInterval: Millis, frameCountSinceInterval: Int, fontKey: FontKey, position: Point) extends SubSystem {

  type EventType = GlobalEvent

  val eventFilter: GlobalEvent => Option[GlobalEvent] = {
    case e: FrameTick => Option(e)
    case _            => None
  }

  def update(gameTime: GameTime, dice: Dice): GlobalEvent => Outcome[SubSystem] = {
    case FrameTick =>
      Outcome(
        if (gameTime.running >= (this.lastInterval + Millis(1000))) {
          this.copy(
            fps = this.frameCountSinceInterval + 1,
            lastInterval = gameTime.running,
            frameCountSinceInterval = 0
          )
        } else {
          this.copy(frameCountSinceInterval = this.frameCountSinceInterval + 1)
        }
      )
  }

  def render(gameTime: GameTime): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addUiLayerNodes(Text(fpsCount, position.x, position.y, 1, fontKey))

  def fpsCount(implicit showI: AsString[Int]): String =
    s"""FPS: ${showI.show(fps)}"""

  def report: String =
    "FPS Counter"
}

object FPSCounter {

  def subSystem(fontKey: FontKey, position: Point): FPSCounter =
    FPSCounter(0, Millis(0), 0, fontKey, position)

}
