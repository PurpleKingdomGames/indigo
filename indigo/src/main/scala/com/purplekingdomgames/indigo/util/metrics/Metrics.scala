package com.purplekingdomgames.indigo.util.metrics

import scala.collection.mutable

trait IMetrics {
  def record(m: Metric): Unit
  def recordWithTime(m: Metric, time: Long): Unit
  def giveTime(): Long
}

object Metrics {

  private class MetricsInstance(logReportIntervalMs: Int) extends IMetrics {
    private val metrics: mutable.Queue[MetricWrapper] = new mutable.Queue[MetricWrapper]()

    private var lastReportTime: Long = System.currentTimeMillis()

    def record(m: Metric): Unit = recordWithTime(m, giveTime())

    def recordWithTime(m: Metric, time: Long): Unit = {
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
    def record(m: Metric): Unit = ()
    def recordWithTime(m: Metric, time: Long): Unit = ()
    def giveTime(): Long = 1
  }

  private var instance: Option[IMetrics] = None
  private var savedEnabled: Boolean = false
  private var savedLogReportIntervalMs: Int = 10000

  def getInstance(): IMetrics =
    getInstanceEnabledWithIntervalMs(savedEnabled, savedLogReportIntervalMs)

  def getInstanceEnabledWithIntervalMs(enabled: Boolean, logReportIntervalMs: Int): IMetrics =
    instance match {
      case Some(i) => i
      case None =>
        savedEnabled = enabled
        savedLogReportIntervalMs = logReportIntervalMs
        val nextInstance = if(enabled) new MetricsInstance(logReportIntervalMs) else new NullMetricsInstance
        instance = Some(nextInstance)
        nextInstance
    }

  def getNullInstance: IMetrics = new NullMetricsInstance

}