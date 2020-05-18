package indigojs.delegates.config

import scala.scalajs.js.annotation._

import indigo.shared.config.AdvancedGameConfig

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("AdvancedGameConfig")
final class AdvancedGameConfigDelegate(
    _antiAliasing: Boolean,
    _batchSize: Int,
    _disableSkipModelUpdates: Boolean,
    _disableSkipViewUpdates: Boolean
) {

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
      antiAliasing,
      batchSize,
      disableSkipModelUpdates,
      disableSkipViewUpdates
    )
}

object AdvancedGameConfigDelegate {
  val default: AdvancedGameConfigDelegate =
    new AdvancedGameConfigDelegate(
      _antiAliasing = false,
      _batchSize = 256,
      _disableSkipModelUpdates = false,
      _disableSkipViewUpdates = false
    )
}
