package indigo.shared

import indigo.platform.renderer.ScreenCaptureConfig
import indigo.shared.assets.AssetType
import indigo.shared.collections.Batch
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
    _startUpData: => StartUpData,
    _captureScreen: Batch[ScreenCaptureConfig] => Batch[Either[String, AssetType.Image]]
):

  lazy val startUpData = _startUpData

  export gameTime.running
  export gameTime.delta
  export inputState.mouse
  export inputState.keyboard
  export inputState.gamepad
  export boundaryLocator.findBounds
  export boundaryLocator.bounds

  /** Capture the screen as a number of images, each with the specified configuration
    *
    * @param captureConfig
    *   The configurations to use when capturing the screen
    * @return
    *   A batch containing either the captured images, or error messages
    */
  def captureScreen(captureConfig: Batch[ScreenCaptureConfig]): Batch[Either[String, AssetType.Image]] =
    _captureScreen(captureConfig)

  /** Capture the screen as an image, with the specified configuration
    *
    * @param captureConfig
    *   The configuration to use when capturing the screen
    * @return
    *   The captured image, or an error message
    */
  def captureScreen(captureConfig: ScreenCaptureConfig): Either[String, AssetType.Image] =
    captureScreen(Batch(captureConfig)).headOption match {
      case Some(v) => v
      case None    => Left("Could not capture image")
    }
