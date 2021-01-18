package indigo.shared.platform

import indigo.shared.time.GameTime
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.datatypes.mutable.CheapMatrix4

trait SceneFrameData {
  val gameTime: GameTime
  val scene: SceneUpdateFragment
  val assetMapping: AssetMapping
  val screenWidth: Double
  val screenHeight: Double
  val orthographicProjectionMatrix: CheapMatrix4
}
