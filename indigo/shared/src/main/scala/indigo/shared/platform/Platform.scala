package indigo.shared.platform

import indigo.shared.config.GameConfig
import scala.util.Try

trait Platform {

  def initialise(gameConfig: GameConfig): Try[(Renderer, AssetMapping)]

  def tick(loop: Long => Unit): Unit

}
