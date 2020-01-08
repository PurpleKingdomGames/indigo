package indigo.shared.platform

import indigo.shared.config.GameConfig
import indigo.shared.GameContext

trait Platform {

  def initialiseRenderer(gameConfig: GameConfig): GameContext[(Renderer, AssetMapping)]

  def tick(loop: Long => Unit): Unit

  def save(key: String, data: String): Unit

  def load(key: String): Option[String]

  def delete(key: String): Unit

  def deleteAll(): Unit

}
