package indigo.shared.platform
import indigo.shared.config.GameConfig

trait Window {

  def windowSetup(gameConfig: GameConfig): Unit

}
