
package object indigogame {

  /**
    * defaultGameConfig Provides a useful default config set up:
    * - Game Viewport = 550 x 400
    * - FPS = 30
    * - Clear color = Black
    * - Magnification = 1
    * - No advanced settings enabled
    * @return A GameConfig instance
    */
  val defaultGameConfig: indigo.shared.config.GameConfig = indigo.shared.config.GameConfig.default

  /**
    * noRender Convenience value, alias for SceneUpdateFragment.empty
    * @return An Empty SceneUpdateFragment
    */
  val noRender: indigo.shared.scenegraph.SceneUpdateFragment = indigo.shared.scenegraph.SceneUpdateFragment.empty

  type SubSystem = indigoexts.subsystems.SubSystem

}
