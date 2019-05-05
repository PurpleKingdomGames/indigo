package indigo.shared.config

final case class AdvancedGameConfig(recordMetrics: Boolean, logMetricsReportIntervalMs: Int, disableSkipModelUpdates: Boolean, disableSkipViewUpdates: Boolean)
object AdvancedGameConfig {
  val default: AdvancedGameConfig = AdvancedGameConfig(
    recordMetrics = false,
    logMetricsReportIntervalMs = 10000,
    disableSkipModelUpdates = false,
    disableSkipViewUpdates = false
  )
}
