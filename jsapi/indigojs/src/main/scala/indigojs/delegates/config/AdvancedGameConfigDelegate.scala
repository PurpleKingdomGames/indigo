package indigojs.delegates.config

import scala.scalajs.js.annotation._

import indigo.shared.config.AdvancedGameConfig

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("GameConfig")
final class AdvancedGameConfigDelegate(
    val antiAliasing: Boolean,
    val batchSize: Int,
    val recordMetrics: Boolean,
    val logMetricsReportIntervalMs: Int,
    val disableSkipModelUpdates: Boolean,
    val disableSkipViewUpdates: Boolean
) {
  def toInternal: AdvancedGameConfig =
    AdvancedGameConfig(
      antiAliasing,
      batchSize,
      recordMetrics,
      logMetricsReportIntervalMs,
      disableSkipModelUpdates,
      disableSkipViewUpdates
    )
}

object AdvancedGameConfigDelegate {
  val default: AdvancedGameConfigDelegate =
    new AdvancedGameConfigDelegate(
      antiAliasing = false,
      batchSize = 256,
      recordMetrics = false,
      logMetricsReportIntervalMs = 10000,
      disableSkipModelUpdates = false,
      disableSkipViewUpdates = false
    )
}
