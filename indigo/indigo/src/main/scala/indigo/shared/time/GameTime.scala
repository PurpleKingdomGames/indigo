package indigo.shared.time

import annotation.targetName

/** An instance of `GameTime` is present on every frame, and the values it holds do not change during that frame. This
  * allows for "synchronous" programming, where it is assumed that everything happens at the exact same time during the
  * current frame. The most commonly used fields (e.g. for animation) are the running time of the game and the time
  * delta since the last frame.
  */
final case class GameTime(running: Seconds, delta: Seconds, targetFPS: Option[FPS]) derives CanEqual:
  lazy val frameDuration: Option[Millis] = targetFPS.map(_.toMillis)

  def setTargetFPS(fps: FPS): GameTime =
    this.copy(targetFPS = Option(fps))
  @targetName("setTargetFPS_Int")
  def setTargetFPS(fps: Int): GameTime =
    setTargetFPS(FPS(fps))

object GameTime:

  val zero: GameTime =
    GameTime(Seconds.zero, Seconds.zero, None)

  def is(running: Seconds): GameTime =
    GameTime(running, Seconds.zero, None)

  def withDelta(running: Seconds, delta: Seconds): GameTime =
    GameTime(running, delta, None)
