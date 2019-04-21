package indigo.shared.platform

import indigo.shared.GameConfig
import indigo.shared.GameContext

trait Platform {

  def initialiseRenderer(gameConfig: GameConfig): GameContext[(Renderer, AssetMapping)]

  def tick(loop: Long => Unit): Unit

}