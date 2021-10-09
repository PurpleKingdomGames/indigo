package indigo.shared

import indigo.shared.datatypes.Rectangle
import indigo.shared.dice.Dice
import indigo.shared.events.InputState
import indigo.shared.input.Gamepad
import indigo.shared.input.Keyboard
import indigo.shared.input.Mouse
import indigo.shared.scenegraph.SceneNode
import indigo.shared.time.GameTime
import indigo.shared.time.Seconds

/** The FrameContext is the context in which the current frame will be processed. In includes values that are unique to
  * this frame, and also globally available services.
  *
  * @param gameTime
  *   A sampled instance of time that you should use everywhere that you need a time value.
  * @param dice
  *   A pseudo-random number generator, made predictable / reproducible by being seeded on the current running time.
  * @param inputState
  *   A snapshot of the state of the various input methods, also allows input mapping of combinations of inputs.
  * @param boundaryLocator
  *   A service that can be interrogated for the calculated dimensions of screen elements.
  * @param startUpData
  *   A read only reference to any and all data created during start up / set up.
  */
final class FrameContext[StartUpData](
    val gameTime: GameTime,
    val dice: Dice,
    val inputState: InputState,
    val boundaryLocator: BoundaryLocator,
    _startUpData: => StartUpData
) {

  lazy val startUpData = _startUpData
  val running: Seconds = gameTime.running
  val delta: Seconds   = gameTime.delta

  val mouse: Mouse       = inputState.mouse
  val keyboard: Keyboard = inputState.keyboard
  val gamepad: Gamepad   = inputState.gamepad

  def findBounds(sceneNode: SceneNode): Option[Rectangle] =
    boundaryLocator.findBounds(sceneNode)

}
