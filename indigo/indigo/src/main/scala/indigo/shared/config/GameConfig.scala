package indigo.shared.config

import indigo.shared.datatypes.RGBA
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Size

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
    transparentBackground: Boolean,
    advanced: AdvancedGameConfig
) derives CanEqual:
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
  def withViewport(size: Size): GameConfig =
    this.copy(viewport = GameViewport(size.width, size.height))
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

  def withTransparentBackground(enabled: Boolean): GameConfig =
    this.copy(transparentBackground = enabled)
  def useTransparentBackground: GameConfig =
    withTransparentBackground(true)
  def noTransparentBackground: GameConfig =
    withTransparentBackground(false)

object GameConfig:

  val default: GameConfig =
    GameConfig(
      viewport = GameViewport(550, 400),
      frameRate = 60,
      clearColor = RGBA.Black,
      magnification = 1,
      transparentBackground = false,
      advanced = AdvancedGameConfig.default
    )

  def apply(width: Int, height: Int, frameRate: Int): GameConfig =
    GameConfig(
      viewport = GameViewport(width, height),
      frameRate = frameRate,
      clearColor = RGBA.Black,
      magnification = 1,
      transparentBackground = false,
      advanced = AdvancedGameConfig.default
    )

  def apply(viewport: GameViewport, frameRate: Int, clearColor: RGBA, magnification: Int): GameConfig =
    GameConfig(
      viewport = viewport,
      frameRate = frameRate,
      clearColor = clearColor,
      magnification = magnification,
      transparentBackground = false,
      advanced = AdvancedGameConfig.default
    )
