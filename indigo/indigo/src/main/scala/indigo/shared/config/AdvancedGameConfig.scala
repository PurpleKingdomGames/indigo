package indigo.shared.config

import indigo.Millis
import indigo.shared.config.RenderingTechnology.WebGL1
import indigo.shared.config.RenderingTechnology.WebGL2
import indigo.shared.config.RenderingTechnology.WebGL2WithFallback

/** Additional settings to help tune aspects of your game's performance.
  *
  * @param renderingTechnology
  *   Use WebGL 1.0 or 2.0? Defaults to 2.0 with fallback to 1.0.
  * @param antiAliasing
  *   Smooth the rendered view? Defaults to false.
  * @param batchSize
  *   How many scene nodes to batch together between draws, defaults to 256.
  * @param premultipliedAlpha
  *   Should the renderer use premultiplied alpha? All the standard shaders expect the answer to be yes! Disable with
  *   caution, defaults to true.
  * @param autoLoadStandardShaders
  *   Should all the standard shaders be made available by default? They can be added individually / manually if you
  *   prefer. Defaults to true, to include them.
  * @param disableContextMenu
  *   By default, context menu on right-click is disable for the canvas.
  */
final case class AdvancedGameConfig(
    renderingTechnology: RenderingTechnology,
    antiAliasing: Boolean,
    batchSize: Int,
    premultipliedAlpha: Boolean,
    autoLoadStandardShaders: Boolean,
    disableContextMenu: Boolean,
    clickTime: Millis
) derives CanEqual {

  def withRenderingTechnology(tech: RenderingTechnology): AdvancedGameConfig =
    this.copy(renderingTechnology = tech)
  def useWebGL1: AdvancedGameConfig =
    this.copy(renderingTechnology = RenderingTechnology.WebGL1)
  def useWebGL2: AdvancedGameConfig =
    this.copy(renderingTechnology = RenderingTechnology.WebGL2)
  def useWebGL2WithFallback: AdvancedGameConfig =
    this.copy(renderingTechnology = RenderingTechnology.WebGL2WithFallback)

  def withAntiAliasing(enabled: Boolean): AdvancedGameConfig =
    this.copy(antiAliasing = enabled)
  def useAntiAliasing: AdvancedGameConfig =
    withAntiAliasing(true)
  def noAntiAliasing: AdvancedGameConfig =
    withAntiAliasing(false)

  def withPremultipliedAlpha(enabled: Boolean): AdvancedGameConfig =
    this.copy(premultipliedAlpha = enabled)
  def usePremultipliedAlpha: AdvancedGameConfig =
    withPremultipliedAlpha(true)
  def noPremultipliedAlpha: AdvancedGameConfig =
    withPremultipliedAlpha(false)

  def withBatchSize(size: Int): AdvancedGameConfig =
    this.copy(batchSize = size)

  def withAutoLoadStandardShaders(autoLoad: Boolean): AdvancedGameConfig =
    this.copy(autoLoadStandardShaders = autoLoad)

  def withContextMenu: AdvancedGameConfig =
    this.copy(disableContextMenu = false)
  def noContextMenu: AdvancedGameConfig =
    this.copy(disableContextMenu = true)

  def withClickTime(millis: Millis): AdvancedGameConfig =
    this.copy(clickTime = millis)

  val asString: String =
    s"""
       |Advanced settings
       |- Rendering technology:        ${renderingTechnology.name}
       |- AntiAliasing enabled:        ${antiAliasing.toString}
       |- Render batch size:           ${batchSize.toString}
       |- Pre-Multiplied Alpha:        ${premultipliedAlpha.toString}
       |- Auto-Load Shaders:           ${autoLoadStandardShaders.toString}
       |- Disable Context Menu:        ${disableContextMenu.toString}
       |- Click Time (ms):             ${clickTime.toString}
       |""".stripMargin
}

object AdvancedGameConfig {
  val default: AdvancedGameConfig =
    AdvancedGameConfig(
      renderingTechnology = WebGL2WithFallback,
      antiAliasing = false,
      premultipliedAlpha = true,
      batchSize = 256,
      autoLoadStandardShaders = true,
      disableContextMenu = true,
      clickTime = Millis(250)
    )
}

/** ADT that specifies which renderer to use. The default is to try and use WebGL 2.0 and fallback to WebGL 1.0, but you
  * can force one or the other.
  */
enum RenderingTechnology derives CanEqual:
  case WebGL1, WebGL2, WebGL2WithFallback

  def isWebGL1: Boolean =
    this match
      case WebGL1 => true
      case _      => false

  def isWebGL2: Boolean =
    this match
      case WebGL2 => true
      case _      => false

  // Note: This isn't really a thing. Immediately after initialisation the rendering tech is decided to be 1.0 or 2.0
  def isWebGL2WithFallback: Boolean =
    this match
      case WebGL2WithFallback => true
      case _                  => false

object RenderingTechnology:
  extension (t: RenderingTechnology)
    def name: String =
      t match {
        case WebGL1             => "WebGL 1.0"
        case WebGL2             => "WebGL 2.0"
        case WebGL2WithFallback => "WebGL 2.0 (will fallback to WebGL 1.0)"
      }
