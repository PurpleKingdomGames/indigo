package indigo.shared.config

import indigo.shared.config.RenderingTechnology.WebGL2WithFallback
import indigo.shared.config.RenderingTechnology.WebGL1
import indigo.shared.config.RenderingTechnology.WebGL2

/**
  * Additional settings to help tune a games performance. 
  *
  * @param renderingTechnology Use WebGL 1.0 or 2.0? Defaults to 2.0 with fallback to 1.0.
  * @param antiAliasing Smooth the rendered view? Defaults to false.
  * @param batchSize How many scene nodes to batch together between draws, defaults to 256.
  * @param disableSkipModelUpdates By default, model updates will be skipped if the frame rate drops too low.
  * @param disableSkipViewUpdates By default, view updates will be skipped if the frame rate drops too low.
  */
final case class AdvancedGameConfig(renderingTechnology: RenderingTechnology, antiAliasing: Boolean, batchSize: Int, disableSkipModelUpdates: Boolean, disableSkipViewUpdates: Boolean) {
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
    batchSize = 256,
    disableSkipModelUpdates = false,
    disableSkipViewUpdates = false
  )
}

/**
  * ADT that specifies which renderer to use.
  * The default is to try and use WebGL 2.0 and fallback to WebGL 1.0, but you can force one or the other.
  */
sealed trait RenderingTechnology {
  def name: String =
    this match {
      case WebGL1 => "WebGL 1.0"
      case WebGL2 => "WebGL 2.0"
      case WebGL2WithFallback => "WebGL 2.0 (will fallback to WebGL 1.0)"
    }
}
object RenderingTechnology {
  case object WebGL1 extends RenderingTechnology
  case object WebGL2 extends RenderingTechnology
  case object WebGL2WithFallback extends RenderingTechnology
}
