package indigo.scenes

import indigo.shared.BoundaryLocator
import indigo.shared.FrameContext
import indigo.shared.datatypes.Rectangle
import indigo.shared.dice.Dice
import indigo.shared.events.InputState
import indigo.shared.input.Gamepad
import indigo.shared.input.Keyboard
import indigo.shared.input.Mouse
import indigo.shared.scenegraph.SceneNode
import indigo.shared.time.GameTime
import indigo.shared.time.Seconds

/** SceneContext is a Scene specific equivalent of `FrameContext`, and exposes all of the fields and methods or a normal
  * `FrameContext` object. It adds information about the scene currently running.
  *
  * @param sceneName
  *   The name of the current scene.
  * @param sceneTime
  *   The running time of the current scene calculated as the time the scene was entered minus game running time.
  * @param frameContext
  *   The normal frame context object that all other fields delegate to.
  */
final class SceneContext[StartUpData](
    val sceneName: SceneName,
    val sceneTime: Seconds,
    val frameContext: FrameContext[StartUpData]
):

  lazy val gameTime: GameTime               = frameContext.gameTime
  lazy val dice: Dice                       = frameContext.dice
  lazy val inputState: InputState           = frameContext.inputState
  lazy val boundaryLocator: BoundaryLocator = frameContext.boundaryLocator
  lazy val startUpData: StartUpData         = frameContext.startUpData
  lazy val running: Seconds                 = frameContext.gameTime.running
  lazy val delta: Seconds                   = frameContext.gameTime.delta
  lazy val mouse: Mouse                     = frameContext.inputState.mouse
  lazy val keyboard: Keyboard               = frameContext.inputState.keyboard
  lazy val gamepad: Gamepad                 = frameContext.inputState.gamepad

  def findBounds(sceneNode: SceneNode): Option[Rectangle] =
    boundaryLocator.findBounds(sceneNode)

  def bounds(sceneGraphNode: SceneNode): Rectangle =
    boundaryLocator.bounds(sceneGraphNode)
