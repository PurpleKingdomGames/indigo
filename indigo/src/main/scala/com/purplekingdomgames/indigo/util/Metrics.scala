package com.purplekingdomgames.indigo.util

import scala.collection.mutable

object Metrics {

  trait IMetrics {
    def record(m: Metric): Unit
    def report(): Unit
  }

  private class MetricsInstance(logReportIntervalMs: Int) extends IMetrics {
    private val metrics: mutable.Queue[Metric] = new mutable.Queue[Metric]()

    def record(m: Metric): Unit = metrics += m

    def report(): Unit = Logger.info("I should err... report some metrics I guess...")
    
  }

  private class NullMetricsInstance extends IMetrics {

    def record(m: Metric): Unit = ()
    def report(): Unit = ()

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

  def report(): Unit = getInstance().report()

}

sealed trait Metric {
  val time: Long = System.currentTimeMillis()
}
case object FrameStart extends Metric

case object UpdateStart extends Metric
case object UpdateGameModelStart extends Metric
case object UpdateGameModelEnd extends Metric
case object UpdateGameViewStart extends Metric
case object UpdateGameViewEnd extends Metric
case object SkippedModelUpdate extends Metric
case object SkippedViewUpdate extends Metric
case object UpdateEnd extends Metric

case object RenderStart extends Metric
case object RenderEnd extends Metric

case object FrameEnd extends Metric