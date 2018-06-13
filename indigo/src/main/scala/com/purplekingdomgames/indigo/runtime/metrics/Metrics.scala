package com.purplekingdomgames.indigo.runtime.metrics

import scala.collection.mutable

trait IMetrics {
  def record(m: Metric): Unit
  def recordForSpecificTime(m: Metric, time: Long): Unit
  def giveTime(): Long
}

object Metrics {

  private class MetricsInstance(logReportIntervalMs: Int) extends IMetrics {

    @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
    private val metrics: mutable.Queue[MetricWrapper] = new mutable.Queue[MetricWrapper]()

    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var lastReportTime: Long = System.currentTimeMillis()

    def record(m: Metric): Unit =
      recordForSpecificTime(m, giveTime())

    def recordForSpecificTime(m: Metric, time: Long): Unit = {
      metrics += MetricWrapper(m, time)

      m match {
        case FrameEndMetric if time >= lastReportTime + logReportIntervalMs =>
          lastReportTime = time
          MetricsLogReporter.report(metrics.dequeueAll(_ => true).toList)
        case _ => ()
      }

    }

    def giveTime(): Long = System.currentTimeMillis()

  }

  private class NullMetricsInstance extends IMetrics {
    def record(m: Metric): Unit                            = ()
    def recordForSpecificTime(m: Metric, time: Long): Unit = ()
    def giveTime(): Long                                   = 1
  }

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var instance: Option[IMetrics] = None

  def getInstance(enabled: Boolean, logReportIntervalMs: Int): IMetrics =
    instance match {
      case Some(i) => i
      case None =>
        val i = if (enabled) new MetricsInstance(logReportIntervalMs) else new NullMetricsInstance
        instance = Some(i)
        i
    }

  def getNullInstance: IMetrics = new NullMetricsInstance

}
