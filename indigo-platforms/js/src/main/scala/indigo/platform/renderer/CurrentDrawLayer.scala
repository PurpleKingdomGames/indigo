package indigo.platform.renderer

import indigo.shared.metrics._

sealed trait CurrentDrawLayer {
  val metricStart: Metric
  val metricEnd: Metric
  val metricDraw: Metric
  def isMerge: Boolean
  val name: String
}
object CurrentDrawLayer {
  case object Game extends CurrentDrawLayer {
    val metricStart: Metric = NormalDrawCallLengthStartMetric
    val metricEnd: Metric   = NormalDrawCallLengthEndMetric
    val metricDraw: Metric  = NormalLayerDrawCallMetric
    def isMerge: Boolean    = false
    val name: String = "game layer"
  }
  case object Lighting extends CurrentDrawLayer {
    val metricStart: Metric = LightingDrawCallLengthStartMetric
    val metricEnd: Metric   = LightingDrawCallLengthEndMetric
    val metricDraw: Metric  = LightingDrawCallMetric
    def isMerge: Boolean    = false
    val name: String = "lighting layer"
  }
  case object UI extends CurrentDrawLayer {
    val metricStart: Metric = NormalDrawCallLengthStartMetric
    val metricEnd: Metric   = NormalDrawCallLengthEndMetric
    val metricDraw: Metric  = NormalLayerDrawCallMetric
    def isMerge: Boolean    = false
    val name: String = "ui layer"
  }
  case object Merge extends CurrentDrawLayer {
    val metricStart: Metric = ToWindowDrawCallLengthStartMetric
    val metricEnd: Metric   = ToWindowDrawCallLengthEndMetric
    val metricDraw: Metric  = ToWindowDrawCallMetric
    def isMerge: Boolean    = true
    val name: String = "merge layer"
  }
}
