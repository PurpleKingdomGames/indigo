package indigo.shared.config

final case class AdvancedGameConfig(antiAliasing: Boolean, batchSize: Int, recordMetrics: Boolean, logMetricsReportIntervalMs: Int, disableSkipModelUpdates: Boolean, disableSkipViewUpdates: Boolean)
object AdvancedGameConfig {
  val default: AdvancedGameConfig = AdvancedGameConfig(
    antiAliasing = false,
    batchSize = 64,
    recordMetrics = false,
    logMetricsReportIntervalMs = 10000,
    disableSkipModelUpdates = false,
    disableSkipViewUpdates = false
  )
}
