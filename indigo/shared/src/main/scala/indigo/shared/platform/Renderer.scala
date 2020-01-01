package indigo.shared.platform

import indigo.shared.metrics.Metrics
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.time.GameTime

trait Renderer {
  def init(): Unit
  def drawScene(gameTime: GameTime, scene: SceneUpdateFragment, assetMapping: AssetMapping, metrics: Metrics): Unit
}
