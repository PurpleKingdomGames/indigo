package indigo.shared.config

import indigo.shared.config.RenderingTechnology.WebGL2WithFallback
import indigo.shared.config.RenderingTechnology.WebGL1
import indigo.shared.config.RenderingTechnology.WebGL2

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
