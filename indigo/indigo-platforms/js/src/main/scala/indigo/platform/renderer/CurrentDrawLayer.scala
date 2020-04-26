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
    val name: String        = "game layer"
  }
  case object Lights extends CurrentDrawLayer {
    val metricStart: Metric = LightsDrawCallLengthStartMetric
    val metricEnd: Metric   = LightsDrawCallLengthEndMetric
    val metricDraw: Metric  = LightsLayerDrawCallMetric
    def isMerge: Boolean    = false
    val name: String        = "lights pass"
  }
  case object Lighting extends CurrentDrawLayer {
    val metricStart: Metric = LightingDrawCallLengthStartMetric
    val metricEnd: Metric   = LightingDrawCallLengthEndMetric
    val metricDraw: Metric  = LightingDrawCallMetric
    def isMerge: Boolean    = false
    val name: String        = "lighting layer"
  }
  case object Distortion extends CurrentDrawLayer {
    val metricStart: Metric = DistortionDrawCallLengthStartMetric
    val metricEnd: Metric   = DistortionDrawCallLengthEndMetric
    val metricDraw: Metric  = DistortionDrawCallMetric
    def isMerge: Boolean    = false
    val name: String        = "distortion layer"
  }
  case object UI extends CurrentDrawLayer {
    val metricStart: Metric = UiDrawCallLengthStartMetric
    val metricEnd: Metric   = UiDrawCallLengthEndMetric
    val metricDraw: Metric  = UiLayerDrawCallMetric
    def isMerge: Boolean    = false
    val name: String        = "ui layer"
  }
  case object Merge extends CurrentDrawLayer {
    val metricStart: Metric = ToWindowDrawCallLengthStartMetric
    val metricEnd: Metric   = ToWindowDrawCallLengthEndMetric
    val metricDraw: Metric  = ToWindowDrawCallMetric
    def isMerge: Boolean    = true
    val name: String        = "merge layer"
  }
}
