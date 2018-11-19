package com.example.perf
import indigo.GameTime

case class FpsCounter(fps: Int, lastInterval: Double, frameCountSinceInterval: Int) {
  def asString: String = s"FPS: ${fps.toString}"
}
object FpsCounter {
  val empty: FpsCounter =
    FpsCounter(0, 0, 0)

  def update(gameTime: GameTime, previous: FpsCounter): FpsCounter =
    if (gameTime.running >= previous.lastInterval + 1000) {
      previous.copy(
        fps = previous.frameCountSinceInterval + 1,
        lastInterval = gameTime.running,
        frameCountSinceInterval = 0
      )
    } else {
      previous.copy(frameCountSinceInterval = previous.frameCountSinceInterval + 1)
    }
}
