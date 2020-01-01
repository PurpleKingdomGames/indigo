package indigo.shared.platform

import indigo.shared.config.GameConfig
import indigo.shared.GameContext

trait Platform {

  def initialiseRenderer(gameConfig: GameConfig): GameContext[(Renderer, AssetMapping)]

  def tick(loop: Long => Unit): Unit

}
