package com.purplekingdomgames.indigo.util

import scala.collection.mutable

object Metrics {

  trait IMetrics {
    def record(m: Metric): Unit
  }

  private class MetricsInstance(logReportIntervalMs: Int) extends IMetrics {
    private val metrics: mutable.Queue[Metric] = new mutable.Queue[Metric]()

    private var lastReportTime: Long = System.currentTimeMillis()

    def record(m: Metric): Unit = {
      metrics += m

      m match {
        case FrameEndMetric(time) if time >= lastReportTime + logReportIntervalMs =>
          lastReportTime = time
          report(metrics.dequeueAll(_ => true).toList)
        case _ => ()
      }

    }

    private def report(metrics: List[Metric]): Unit =
      Logger.info(
        s"""**********************
          |Metrics! ${metrics.length} of them!
          |**********************
        """.stripMargin
      )

  }

  private class NullMetricsInstance extends IMetrics {
    def record(m: Metric): Unit = ()
  }

  private var instance: Option[IMetrics] = None
  private var savedEnabled: Boolean = false
  private var savedLogReportIntervalMs: Int = 10000

  def getInstance(enabled: Boolean = savedEnabled, logReportIntervalMs: Int = savedLogReportIntervalMs): IMetrics =
    instance match {
      case Some(i) => i
      case None =>
        savedEnabled = enabled
        savedLogReportIntervalMs = logReportIntervalMs
        instance = if(enabled) Some(new MetricsInstance(logReportIntervalMs)) else Some(new NullMetricsInstance)
        instance.get
    }

  def record(m: Metric): Unit = getInstance().record(m)

}

sealed trait Metric {
  val time: Long
}
case class FrameStartMetric(time: Long = System.currentTimeMillis()) extends Metric

case class UpdateStartMetric(time: Long = System.currentTimeMillis()) extends Metric
case class UpdateGameModelStartMetric(time: Long = System.currentTimeMillis()) extends Metric
case class UpdateGameModelEndMetric(time: Long = System.currentTimeMillis()) extends Metric
case class UpdateGameViewStartMetric(time: Long = System.currentTimeMillis()) extends Metric
case class UpdateGameViewEndMetric(time: Long = System.currentTimeMillis()) extends Metric
case class SkippedModelUpdateMetric(time: Long = System.currentTimeMillis()) extends Metric
case class SkippedViewUpdateMetric(time: Long = System.currentTimeMillis()) extends Metric
case class UpdateEndMetric(time: Long = System.currentTimeMillis()) extends Metric

case class RenderStartMetric(time: Long = System.currentTimeMillis()) extends Metric
case class RenderEndMetric(time: Long = System.currentTimeMillis()) extends Metric

case class FrameEndMetric(time: Long = System.currentTimeMillis()) extends Metric