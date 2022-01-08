package indigo.shared.config

import indigo.shared.config.RenderingTechnology.WebGL1
import indigo.shared.config.RenderingTechnology.WebGL2
import indigo.shared.config.RenderingTechnology.WebGL2WithFallback

/** Additional settings to help tune a games performance.
  *
  * @param renderingTechnology
  *   Use WebGL 1.0 or 2.0? Defaults to 2.0 with fallback to 1.0.
  * @param antiAliasing
  *   Smooth the rendered view? Defaults to false.
  * @param batchSize
  *   How many scene nodes to batch together between draws, defaults to 256.
  * @param disableSkipModelUpdates
  *   By default, model updates will be skipped if the frame rate drops too low.
  * @param disableSkipViewUpdates
  *   By default, view updates will be skipped if the frame rate drops too low.
  */
final case class AdvancedGameConfig(
    renderingTechnology: RenderingTechnology,
    antiAliasing: Boolean,
    batchSize: Int,
    premultipliedAlpha: Boolean,
    disableSkipModelUpdates: Boolean,
    disableSkipViewUpdates: Boolean,
    autoLoadStandardShaders: Boolean
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

  def withSkipModelUpdates(skip: Boolean): AdvancedGameConfig =
    this.copy(disableSkipModelUpdates = skip)

  def withSkipViewUpdates(skip: Boolean): AdvancedGameConfig =
    this.copy(disableSkipViewUpdates = skip)

  def withAutoLoadStandardShaders(autoLoad: Boolean): AdvancedGameConfig =
    this.copy(autoLoadStandardShaders = autoLoad)

  val asString: String =
    s"""
       |Advanced settings
       |- Rendering technology:        ${renderingTechnology.name}
       |- AntiAliasing enabled:        ${antiAliasing.toString()}
       |- Render batch size:           ${batchSize.toString()}
       |- Disabled skip model updates: ${disableSkipModelUpdates.toString()}
       |- Disabled skip view updates:  ${disableSkipViewUpdates.toString()}
       |""".stripMargin
}

object AdvancedGameConfig {
  val default: AdvancedGameConfig =
    AdvancedGameConfig(
      renderingTechnology = WebGL2WithFallback,
      antiAliasing = false,
      premultipliedAlpha = true,
      batchSize = 256,
      disableSkipModelUpdates = false,
      disableSkipViewUpdates = false,
      autoLoadStandardShaders = true
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
