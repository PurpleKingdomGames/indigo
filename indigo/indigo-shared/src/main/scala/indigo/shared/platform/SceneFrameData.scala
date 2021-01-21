package indigo.shared.platform

import indigo.shared.time.GameTime
import indigo.shared.scenegraph.SceneUpdateFragment
import scala.scalajs.js

final class SceneFrameData(
  val gameTime: GameTime,
  val scene: SceneUpdateFragment,
  val assetMapping: AssetMapping,
  val screenWidth: Double,
  val screenHeight: Double,
  val orthographicProjectionMatrix: js.Array[Double]
)
