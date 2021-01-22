package indigo.shared.platform

import indigo.shared.time.GameTime
import indigo.shared.scenegraph.SceneUpdateFragment
import scala.scalajs.js

final case class SceneFrameData(
  gameTime: GameTime,
  scene: SceneUpdateFragment,
  assetMapping: AssetMapping,
  screenWidth: Double,
  screenHeight: Double,
  orthographicProjectionMatrix: js.Array[Double]
)
