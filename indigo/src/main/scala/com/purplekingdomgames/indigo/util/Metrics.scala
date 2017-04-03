package com.purplekingdomgames.indigo.util

import scala.collection.mutable

object Metrics {

  trait IMetrics {
    def record(m: Metric, time: Long = giveTime()): Unit
    def giveTime(): Long
  }

  private class MetricsInstance(logReportIntervalMs: Int) extends IMetrics {
    private val metrics: mutable.Queue[MetricWrapper] = new mutable.Queue[MetricWrapper]()

    private var lastReportTime: Long = System.currentTimeMillis()

    def record(m: Metric, time: Long = giveTime()): Unit = {
      metrics += MetricWrapper(m, time)

      m match {
        case FrameEndMetric if time >= lastReportTime + logReportIntervalMs =>
          lastReportTime = time
          report(metrics.dequeueAll(_ => true).toList)
        case _ => ()
      }

    }

    private def to2DecimalPlaces(d: Double): Double =
      Math.round(d * 100) / 100

    private def as2DecimalPlacePercent(a: Long, b: Long): Double =
      to2DecimalPlaces(100d / a * b)
    private def as2DecimalPlacePercent(a: Int, b: Int): Double =
      to2DecimalPlaces(100d / a * b)

    case class FrameStats(frameDuration: Long,
                          updateDuration: Option[Long],
                          callUpdateModelDuration: Option[Long],
                          callUpdateViewDuration: Option[Long],
                          processViewDuration: Option[Long],
                          toDisplayableDuration: Option[Long],
                          renderDuration: Option[Long],
                          updatePercentage: Option[Double],
                          updateModelPercentage: Option[Double],
                          callUpdateViewPercentage: Option[Double],
                          processViewPercentage: Option[Double],
                          toDisplayablePercentage: Option[Double],
                          renderPercentage: Option[Double]
                         )

    private def extractDuration(metrics: List[MetricWrapper], startName: String, endName: String): Option[Long] =
      metrics.find(_.metric.name == startName).map(_.time).flatMap { start =>
        metrics.find(_.metric.name == endName).map(_.time - start)
      }

    private def extractFrameStatistics(metrics: List[MetricWrapper]): Option[FrameStats] = {

      // Durations
      val frameDuration = extractDuration(metrics, FrameStartMetric.name, FrameEndMetric.name)
      val updateDuration = extractDuration(metrics, UpdateStartMetric.name, UpdateEndMetric.name)
      val callUpdateModelDuration = extractDuration(metrics, CallUpdateGameModelStartMetric.name, CallUpdateGameModelEndMetric.name)
      val callUpdateViewDuration = extractDuration(metrics, CallUpdateViewStartMetric.name, CallUpdateViewEndMetric.name)
      val processViewDuration = extractDuration(metrics, ProcessViewStartMetric.name, ProcessViewEndMetric.name)
      val toDisplayableDuration = extractDuration(metrics, ToDisplayableStartMetric.name, ToDisplayableEndMetric.name)
      val renderDuration = extractDuration(metrics, RenderStartMetric.name, RenderEndMetric.name)

      // Percentages
      val updatePercentage = frameDuration.flatMap(frame => updateDuration.map(t => as2DecimalPlacePercent(frame, t)))
      val updateModelPercentage = frameDuration.flatMap(frame => callUpdateModelDuration.map(t => as2DecimalPlacePercent(frame, t)))
      val callUpdateViewPercentage = frameDuration.flatMap(frame => callUpdateViewDuration.map(t => as2DecimalPlacePercent(frame, t)))
      val processViewPercentage = frameDuration.flatMap(frame => processViewDuration.map(t => as2DecimalPlacePercent(frame, t)))
      val toDisplayablePercentage = frameDuration.flatMap(frame => toDisplayableDuration.map(t => as2DecimalPlacePercent(frame, t)))
      val renderPercentage = frameDuration.flatMap(frame => renderDuration.map(t => as2DecimalPlacePercent(frame, t)))

      frameDuration.map { t =>
        FrameStats(t,
          updateDuration,
          callUpdateModelDuration,
          callUpdateViewDuration,
          processViewDuration,
          toDisplayableDuration,
          renderDuration,
          updatePercentage,
          updateModelPercentage,
          callUpdateViewPercentage,
          processViewPercentage,
          toDisplayablePercentage,
          renderPercentage
        )
      }
    }

    private def calcMeanDuration(l: List[Option[Long]]): Double =
      to2DecimalPlaces(l.collect { case Some(s) => s.toDouble }.sum / l.length.toDouble)

    private def calcMeanPercentage(l: List[Option[Double]]): Double =
      to2DecimalPlaces(l.collect { case Some(s) => s }.sum / l.length.toDouble)

    private def report(metrics: List[MetricWrapper]): Unit = {

      val frames: List[FrameStats] = splitIntoFrames(metrics).map(extractFrameStatistics).collect { case Some(s) => s}
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

      val modelUpdatesSkipped: Int = metrics.collect { case m @ MetricWrapper(SkippedModelUpdateMetric, _) => m }.length
      val modelSkipsPercent: Double = as2DecimalPlacePercent(frameCount, modelUpdatesSkipped)
      val viewUpdatesSkipped: Int = metrics.collect { case m @ MetricWrapper(SkippedViewUpdateMetric, _) => m }.length
      val viewSkipsPercent: Double = as2DecimalPlacePercent(frameCount, viewUpdatesSkipped)

      val meanFrameDuration: String =
        to2DecimalPlaces(frames.map(_.frameDuration.toDouble).sum / frameCount.toDouble).toString

      val meanUpdateModel: String = {
        val a = calcMeanDuration(frames.map(_.callUpdateModelDuration)).toString
        val b = calcMeanPercentage(frames.map(_.updateModelPercentage)).toString

        s"""$a\t($b%)"""
      }

      val meanUpdate: String = {
        val a = calcMeanDuration(frames.map(_.updateDuration)).toString
        val b = calcMeanPercentage(frames.map(_.updatePercentage)).toString

        s"""$a\t($b%),\tcalling model update: $meanUpdateModel"""
      }

      val meanCallViewUpdate: String = {
        val a = calcMeanDuration(frames.map(_.callUpdateViewDuration)).toString
        val b = calcMeanPercentage(frames.map(_.callUpdateViewPercentage)).toString

        s"""$a\t($b%)"""
      }

      val meanProcess: String = {
        val a = calcMeanDuration(frames.map(_.processViewDuration)).toString
        val b = calcMeanPercentage(frames.map(_.processViewPercentage)).toString

        s"""$a\t($b%)"""
      }

      val meanToDisplayable: String = {
        val a = calcMeanDuration(frames.map(_.toDisplayableDuration)).toString
        val b = calcMeanPercentage(frames.map(_.toDisplayablePercentage)).toString

        s"""$a\t($b%)"""
      }

      val meanRender: String = {
        val a = calcMeanDuration(frames.map(_.renderDuration)).toString
        val b = calcMeanPercentage(frames.map(_.renderPercentage)).toString

        s"""$a\t($b%)"""
      }

      Logger.info(
        s"""
          |**********************
          |Statistics:
          |Frames since last report:  $frameCount
          |Mean FPS:            $meanFps
          |
          |Model updates skipped:     $modelUpdatesSkipped\t($modelSkipsPercent%)
          |View updates skipped:      $viewUpdatesSkipped\t($viewSkipsPercent%)
          |
          |Mean frame length:         $meanFrameDuration
          |Mean model update:         $meanUpdate
          |Mean call view update:     $meanCallViewUpdate
          |Mean process view:         $meanProcess
          |Mean convert view:         $meanToDisplayable
          |Mean render view:          $meanRender
          |**********************
        """.stripMargin
      )
    }

    private def splitIntoFrames(metrics: List[MetricWrapper]): List[List[MetricWrapper]] = {
      def rec(remaining: List[MetricWrapper], accFrame: List[MetricWrapper], acc: List[List[MetricWrapper]]): List[List[MetricWrapper]] = {
        remaining match {
          case Nil => acc
          case MetricWrapper(FrameEndMetric, time) :: ms =>
            rec(ms, Nil, (MetricWrapper(FrameEndMetric, time) :: accFrame) :: acc)
          case m :: ms =>
            rec(ms, m :: accFrame, acc)
        }
      }

      rec(metrics, Nil, Nil)
    }

    def giveTime(): Long = System.currentTimeMillis()

  }

  private class NullMetricsInstance extends IMetrics {
    def record(m: Metric, time: Long = giveTime()): Unit = ()
    def giveTime(): Long = 1
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

}

case class MetricWrapper(metric: Metric, time: Long)

sealed trait Metric {
  val name: String
}

// In Order!
case object FrameStartMetric extends Metric { val name: String = "frame start" }

case object UpdateStartMetric extends Metric { val name: String = "update model start" }
case object CallUpdateGameModelStartMetric extends Metric { val name: String = "call update model start" } //nested
case object CallUpdateGameModelEndMetric extends Metric { val name: String = "call update model end" } //nested
case object UpdateEndMetric extends Metric { val name: String = "update model end" }

case object CallUpdateViewStartMetric extends Metric { val name: String = "call update view start" }
case object CallUpdateViewEndMetric extends Metric { val name: String = "call update view end" }
case object ProcessViewStartMetric extends Metric { val name: String = "process view start" }
case object ProcessViewEndMetric extends Metric { val name: String = "process view end" }
case object ToDisplayableStartMetric extends Metric { val name: String = "convert to displayable start" }
case object ToDisplayableEndMetric extends Metric { val name: String = "convert to displayable end" }
case object RenderStartMetric extends Metric { val name: String = "render start" }
case object RenderEndMetric extends Metric { val name: String = "render end" }

case object SkippedModelUpdateMetric extends Metric { val name: String = "skipped model update" }
case object SkippedViewUpdateMetric extends Metric { val name: String = "skipped view update" }

case object FrameEndMetric extends Metric { val name: String = "frame end" }