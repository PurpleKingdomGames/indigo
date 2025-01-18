package indigo.scenes

import indigo.shared.Context
import indigo.shared.time.Seconds

/** SceneContext is a Scene specific equivalent of `Context`, and exposes all of the fields and methods of a normal
  * `Context` object. It adds information about the scene currently running.
  *
  * @param sceneName
  *   The name of the current scene.
  * @param sceneStartTime
  *   The time that the current scene was entered.
  * @param context
  *   The normal frame context object that all other fields delegate to.
  */
final class SceneContext[StartUpData](
    val sceneName: SceneName,
    val sceneStartTime: Seconds,
    val context: Context[StartUpData]
):
  export context.*

  /** The running time of the current scene calculated as the game's total running time minus time the scene was
    * entered.
    */
  lazy val sceneRunning: Seconds =
    context.frame.time.running - sceneStartTime

  def toContext: Context[StartUpData] =
    context

object SceneContext:

  def fromContext[A](sceneName: SceneName, sceneStartTime: Seconds, ctx: Context[A]): SceneContext[A] =
    new SceneContext(sceneName, sceneStartTime, ctx)
