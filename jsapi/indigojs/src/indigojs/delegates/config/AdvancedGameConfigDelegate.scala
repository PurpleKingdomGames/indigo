package indigojs.delegates.config

import scala.scalajs.js.annotation._

import indigo.shared.config.AdvancedGameConfig
import indigo.shared.config.RenderingTechnology

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("AdvancedGameConfig")
final class AdvancedGameConfigDelegate(
    _renderingTechnology: String,
    _antiAliasing: Boolean,
    _batchSize: Int,
    _disableSkipModelUpdates: Boolean,
    _disableSkipViewUpdates: Boolean
) {

  @JSExport
  val renderingTechnology = _renderingTechnology
  @JSExport
  val antiAliasing = _antiAliasing
  @JSExport
  val batchSize = _batchSize
  @JSExport
  val disableSkipModelUpdates = _disableSkipModelUpdates
  @JSExport
  val disableSkipViewUpdates = _disableSkipViewUpdates

  def toInternal: AdvancedGameConfig =
    AdvancedGameConfig(
      renderingTechnology.toLowerCase match {
        case "webgl1"             => RenderingTechnology.WebGL1
        case "webgl2"             => RenderingTechnology.WebGL2
        case "webgl2withfallback" => RenderingTechnology.WebGL2WithFallback
        case _                    => RenderingTechnology.WebGL2WithFallback
      },
      antiAliasing,
      batchSize,
      disableSkipModelUpdates,
      disableSkipViewUpdates
    )
}

object AdvancedGameConfigDelegate {
  val default: AdvancedGameConfigDelegate =
    new AdvancedGameConfigDelegate(
      _renderingTechnology = "WebGL2WithFallback",
      _antiAliasing = false,
      _batchSize = 256,
      _disableSkipModelUpdates = false,
      _disableSkipViewUpdates = false
    )
}
