package com.example.perf
import indigo.time.GameTime

final case class FpsCounter(fps: Int, lastInterval: Double, frameCountSinceInterval: Int) {
  def asString: String = s"FPS: ${fps.toString}"
}
object FpsCounter {
  val empty: FpsCounter =
    FpsCounter(0, 0, 0)

  def update(gameTime: GameTime, previous: FpsCounter): FpsCounter =
    if (gameTime.running.toDouble >= previous.lastInterval + 1000) {
      previous.copy(
        fps = previous.frameCountSinceInterval + 1,
        lastInterval = gameTime.running.toDouble,
        frameCountSinceInterval = 0
      )
    } else {
      previous.copy(frameCountSinceInterval = previous.frameCountSinceInterval + 1)
    }
}
