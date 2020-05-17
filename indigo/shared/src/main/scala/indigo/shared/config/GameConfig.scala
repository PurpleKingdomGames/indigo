package indigo.shared.config

import indigo.shared.ClearColor
import indigo.shared.datatypes.Rectangle

final case class GameConfig(viewport: GameViewport, frameRate: Int, clearColor: ClearColor, magnification: Int, advanced: AdvancedGameConfig) {
  val frameRateDeltaMillis: Int = 1000 / frameRate

  val haltViewUpdatesAt: Int  = frameRateDeltaMillis * 2
  val haltModelUpdatesAt: Int = frameRateDeltaMillis * 3

  def screenDimensions: Rectangle =
    viewport.giveDimensions(magnification)

  val asString: String =
    s"""
       |Viewpoint:      [${viewport.width.toString()}, ${viewport.height.toString()}]
       |FPS:            ${frameRate.toString()}
       |frameRateDelta: ${frameRateDeltaMillis.toString()} (view updates stop at: ${haltViewUpdatesAt.toString()}, model at: ${haltModelUpdatesAt.toString()}
       |Clear color:    {red: ${clearColor.r.toString()}, green: ${clearColor.g.toString()}, blue: ${clearColor.b.toString()}, alpha: ${clearColor.a.toString()}}
       |Magnification:  ${magnification.toString()}
       |""".stripMargin

  def withViewport(width: Int, height: Int): GameConfig   = this.copy(viewport = GameViewport(width, height))
  def withViewport(newViewport: GameViewport): GameConfig = this.copy(viewport = newViewport)
  def withFrameRate(frameRate: Int): GameConfig           = this.copy(frameRate = frameRate)
  def withClearColor(clearColor: ClearColor): GameConfig  = this.copy(clearColor = clearColor)
  def withMagnification(magnification: Int): GameConfig   = this.copy(magnification = magnification)

  def withAdvancedSettings(advanced: AdvancedGameConfig): GameConfig = this.copy(advanced = advanced)
  def disableSkipModelUpdates: GameConfig = this.copy(advanced = advanced.copy(disableSkipModelUpdates = true))
  def enableSkipModelUpdates: GameConfig  = this.copy(advanced = advanced.copy(disableSkipModelUpdates = false))
  def disableSkipViewUpdates: GameConfig  = this.copy(advanced = advanced.copy(disableSkipViewUpdates = true))
  def enableSkipViewUpdates: GameConfig   = this.copy(advanced = advanced.copy(disableSkipViewUpdates = false))
}

object GameConfig {

  val default: GameConfig =
    GameConfig(GameViewport(550, 400), 60, ClearColor.Black, 1, AdvancedGameConfig.default)

  def apply(width: Int, height: Int, frameRate: Int): GameConfig =
    GameConfig(GameViewport(width, height), frameRate, ClearColor.Black, 1, AdvancedGameConfig.default)

  def apply(viewport: GameViewport, frameRate: Int, clearColor: ClearColor, magnification: Int): GameConfig =
    GameConfig(viewport, frameRate, clearColor, magnification, AdvancedGameConfig.default)

}
