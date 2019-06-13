package indigo.platform

import indigo.shared.platform.Window
import indigo.shared.config.GameConfig
import indigo.shared.IndigoLogger

object PlatformWindow extends Window {

  def windowSetup(gameConfig: GameConfig): Unit = {
    IndigoLogger.info("Running experimental LWJGL startup...")

    Experiment.run(gameConfig)
  }

}
