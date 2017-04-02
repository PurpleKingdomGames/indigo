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

    private def as2DecimalPlacePercent(a: Int, b: Int): Double =
      Math.round((100d / a * b) * 100).toDouble / 100

    private def report(metrics: List[Metric]): Unit = {

      val frames: List[List[Metric]] = splitIntoFrames(metrics)
      val frameCount: Int = frames.length
      val period: Option[Long] =
        metrics
          .headOption
          .map(_.time)
          .flatMap { start =>
            metrics.reverse.headOption.map(_.time - start)
          }
      val meanFps: String =
        period.map(p => frameCount / (p / 1000).toInt).map(_.toString()).getOrElse("<missing>")

      val modelUpdatesSkipped: Int = metrics.collect { case m @ SkippedModelUpdateMetric(_) => m }.length
      val modelSkipsPercent: Double = as2DecimalPlacePercent(frameCount, modelUpdatesSkipped)
      val viewUpdatesSkipped: Int = metrics.collect { case m @ SkippedViewUpdateMetric(_) => m }.length
      val viewSkipsPercent: Double = as2DecimalPlacePercent(frameCount, viewUpdatesSkipped)

      Logger.info(
        s"""
          |**********************
          |Statistics:
          |Frames since last report:  $frameCount
          |Mean FPS (average):        $meanFps
          |
          |Model updates skipped:     $modelUpdatesSkipped\t($modelSkipsPercent %)
          |View updates skipped:      $viewUpdatesSkipped\t($viewSkipsPercent %)
          |**********************
        """.stripMargin
      )
    }

    private def splitIntoFrames(metrics: List[Metric]): List[List[Metric]] = {
      def rec(remaining: List[Metric], accFrame: List[Metric], acc: List[List[Metric]]): List[List[Metric]] = {
        remaining match {
          case Nil => acc
          case FrameEndMetric(time) :: ms =>
            rec(ms, Nil, (FrameEndMetric(time) :: accFrame) :: acc)
          case m :: ms =>
            rec(ms, m :: accFrame, acc)
        }
      }

      rec(metrics, Nil, Nil)
    }

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
case class CallUpdateGameModelStartMetric(time: Long = System.currentTimeMillis()) extends Metric
case class CallUpdateGameModelEndMetric(time: Long = System.currentTimeMillis()) extends Metric
case class CallUpdateViewStartMetric(time: Long = System.currentTimeMillis()) extends Metric
case class CallUpdateViewEndMetric(time: Long = System.currentTimeMillis()) extends Metric

case class ProcessViewStartMetric(time: Long = System.currentTimeMillis()) extends Metric
case class ProcessViewEndMetric(time: Long = System.currentTimeMillis()) extends Metric
case class ToDisplayableStartMetric(time: Long = System.currentTimeMillis()) extends Metric
case class ToDisplayableEndMetric(time: Long = System.currentTimeMillis()) extends Metric

case class SkippedModelUpdateMetric(time: Long = System.currentTimeMillis()) extends Metric
case class SkippedViewUpdateMetric(time: Long = System.currentTimeMillis()) extends Metric
case class UpdateEndMetric(time: Long = System.currentTimeMillis()) extends Metric

case class RenderStartMetric(time: Long = System.currentTimeMillis()) extends Metric
case class RenderEndMetric(time: Long = System.currentTimeMillis()) extends Metric

case class FrameEndMetric(time: Long = System.currentTimeMillis()) extends Metric