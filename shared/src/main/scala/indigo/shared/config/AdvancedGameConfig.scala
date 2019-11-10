package indigo.shared.config

import scala.scalajs.js.annotation._

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("AdvancedGameConfig")
@JSExportAll
final case class AdvancedGameConfig(antiAliasing: Boolean, batchSize: Int, recordMetrics: Boolean, logMetricsReportIntervalMs: Int, disableSkipModelUpdates: Boolean, disableSkipViewUpdates: Boolean)

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("AdvancedGameConfigOps")
@JSExportAll
object AdvancedGameConfig {
  val default: AdvancedGameConfig = AdvancedGameConfig(
    antiAliasing = false,
    batchSize = 256,
    recordMetrics = false,
    logMetricsReportIntervalMs = 10000,
    disableSkipModelUpdates = false,
    disableSkipViewUpdates = false
  )
}
