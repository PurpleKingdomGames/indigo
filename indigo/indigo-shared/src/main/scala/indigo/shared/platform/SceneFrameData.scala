package indigo.shared.platform

import indigo.shared.time.GameTime
import indigo.shared.scenegraph.SceneUpdateFragment
  import indigo.shared.datatypes.mutable.CheapMatrix4
import scala.scalajs.js

final case class SceneFrameData(
  gameTime: GameTime,
  scene: SceneUpdateFragment,
  assetMapping: AssetMapping,
  screenWidth: Double,
  screenHeight: Double,
  orthographicProjectionMatrix: CheapMatrix4
)
