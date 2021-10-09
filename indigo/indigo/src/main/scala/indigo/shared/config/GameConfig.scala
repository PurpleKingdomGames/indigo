package indigo.shared.config

import indigo.shared.datatypes.RGBA
import indigo.shared.datatypes.Rectangle

/** All the base settings needed to get a game up and running.
  *
  * @param viewport
  *   How big is the window initially? Defaults to 550 x 400 pixels.
  * @param frameRate
  *   Desired frame rate (browsers cap at 60 FPS). Defaults to 60 FPS.
  * @param clearColor
  *   Default background colour. Defaults to Black.
  * @param magnification
  *   Pixel magnification level. Defaults to 1.
  * @param advanced
  *   Additional settings to help tune your game.
  */
final case class GameConfig(
    viewport: GameViewport,
    frameRate: Int,
    clearColor: RGBA,
    magnification: Int,
    advanced: AdvancedGameConfig
) derives CanEqual {
  val frameRateDeltaMillis: Int = 1000 / frameRate
  val haltViewUpdatesAt: Int    = frameRateDeltaMillis * 2
  val haltModelUpdatesAt: Int   = frameRateDeltaMillis * 3

  def screenDimensions: Rectangle =
    viewport.giveDimensions(magnification)

  val asString: String =
    s"""
       |Standard settings
       |- Viewpoint:      [${viewport.width.toString()}, ${viewport.height.toString()}]
       |- FPS:            ${frameRate.toString()}
       |- frameRateDelta: ${frameRateDeltaMillis.toString()} (view updates stop at: ${haltViewUpdatesAt
      .toString()}, model at: ${haltModelUpdatesAt.toString()}
       |- Clear color:    {red: ${clearColor.r.toString()}, green: ${clearColor.g.toString()}, blue: ${clearColor.b
      .toString()}, alpha: ${clearColor.a.toString()}}
       |- Magnification:  ${magnification.toString()}
       |${advanced.asString}
       |""".stripMargin

  def withViewport(width: Int, height: Int): GameConfig =
    this.copy(viewport = GameViewport(width, height))
  def withViewport(newViewport: GameViewport): GameConfig =
    this.copy(viewport = newViewport)
  def withFrameRate(frameRate: Int): GameConfig =
    this.copy(frameRate = frameRate)
  def withClearColor(clearColor: RGBA): GameConfig =
    this.copy(clearColor = clearColor)
  def withMagnification(magnification: Int): GameConfig =
    this.copy(magnification = magnification)

  def withAdvancedSettings(advanced: AdvancedGameConfig): GameConfig =
    this.copy(advanced = advanced)
  def modifyAdvancedSettings(modify: AdvancedGameConfig => AdvancedGameConfig): GameConfig =
    this.copy(advanced = modify(advanced))

  def useWebGL1: GameConfig =
    this.copy(advanced = advanced.copy(renderingTechnology = RenderingTechnology.WebGL1))
  def useWebGL2: GameConfig =
    this.copy(advanced = advanced.copy(renderingTechnology = RenderingTechnology.WebGL2))
  def useWebGL2WithFallback: GameConfig =
    this.copy(advanced = advanced.copy(renderingTechnology = RenderingTechnology.WebGL2WithFallback))
}

object GameConfig {

  val default: GameConfig =
    GameConfig(GameViewport(550, 400), 60, RGBA.Black, 1, AdvancedGameConfig.default)

  def apply(width: Int, height: Int, frameRate: Int): GameConfig =
    GameConfig(GameViewport(width, height), frameRate, RGBA.Black, 1, AdvancedGameConfig.default)

  def apply(viewport: GameViewport, frameRate: Int, clearColor: RGBA, magnification: Int): GameConfig =
    GameConfig(viewport, frameRate, clearColor, magnification, AdvancedGameConfig.default)

}
