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

    private def extractDuration[S <: Metric, E <: Metric](metrics: List[Metric]): Option[Long] =
      metrics.find(_.isInstanceOf[S]).map(_.time).flatMap { start =>
        metrics.find(_.isInstanceOf[E]).map(_.time - start)
      }

    private def extractFrameStatistics(metrics: List[Metric]): Option[FrameStats] = {

      // Durations
      val frameDuration = extractDuration[FrameStartMetric, FrameEndMetric](metrics)
      val updateDuration = extractDuration[UpdateStartMetric, UpdateEndMetric](metrics)
      val callUpdateModelDuration = extractDuration[CallUpdateGameModelStartMetric, CallUpdateGameModelEndMetric](metrics)
      val callUpdateViewDuration = extractDuration[CallUpdateViewStartMetric, CallUpdateViewEndMetric](metrics)
      val processViewDuration = extractDuration[ProcessViewStartMetric, ProcessViewEndMetric](metrics)
      val toDisplayableDuration = extractDuration[ToDisplayableStartMetric, ToDisplayableEndMetric](metrics)
      val renderDuration = extractDuration[RenderStartMetric, RenderEndMetric](metrics)

      // Percentages
      val updatePercentage = frameDuration.flatMap(frame => updateDuration.map(t => as2DecimalPlacePercent(frame, t - frame)))
      val updateModelPercentage = frameDuration.flatMap(frame => callUpdateModelDuration.map(t => as2DecimalPlacePercent(frame, t - frame)))
      val callUpdateViewPercentage = frameDuration.flatMap(frame => callUpdateViewDuration.map(t => as2DecimalPlacePercent(frame, t - frame)))
      val processViewPercentage = frameDuration.flatMap(frame => processViewDuration.map(t => as2DecimalPlacePercent(frame, t - frame)))
      val toDisplayablePercentage = frameDuration.flatMap(frame => toDisplayableDuration.map(t => as2DecimalPlacePercent(frame, t - frame)))
      val renderPercentage = frameDuration.flatMap(frame => renderDuration.map(t => as2DecimalPlacePercent(frame, t - frame)))

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

    private def report(metrics: List[Metric]): Unit = {

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

      val modelUpdatesSkipped: Int = metrics.collect { case m @ SkippedModelUpdateMetric(_) => m }.length
      val modelSkipsPercent: Double = as2DecimalPlacePercent(frameCount, modelUpdatesSkipped)
      val viewUpdatesSkipped: Int = metrics.collect { case m @ SkippedViewUpdateMetric(_) => m }.length
      val viewSkipsPercent: Double = as2DecimalPlacePercent(frameCount, viewUpdatesSkipped)

      val meanFrameDuration: String =
        to2DecimalPlaces(frames.map(_.frameDuration.toDouble).sum / frameCount.toDouble).toString

      val meanUpdateModel: String = {
        val a = to2DecimalPlaces(frames.map(_.callUpdateModelDuration).collect { case Some(s) => s.toDouble }.sum / frameCount.toDouble).toString
        val b = to2DecimalPlaces(frames.map(_.updateModelPercentage).collect { case Some(s) => s }.sum / frameCount.toDouble).toString

        s"""$a\t($b%)"""
      }

      val meanUpdate: String = {
        val a = to2DecimalPlaces(frames.map(_.updateDuration).collect { case Some(s) => s.toDouble }.sum / frameCount.toDouble).toString
        val b = to2DecimalPlaces(frames.map(_.updatePercentage).collect { case Some(s) => s }.sum / frameCount.toDouble).toString

        s"""$a\t($b%),\tcalling model update: $meanUpdateModel"""
      }

      val meanCallViewUpdate: String = {
        val a = to2DecimalPlaces(frames.map(_.callUpdateViewDuration).collect { case Some(s) => s.toDouble }.sum / frameCount.toDouble).toString
        val b = to2DecimalPlaces(frames.map(_.callUpdateViewPercentage).collect { case Some(s) => s }.sum / frameCount.toDouble).toString

        s"""$a\t($b%)"""
      }

      val meanProcess: String = {
        val a = to2DecimalPlaces(frames.map(_.processViewDuration).collect { case Some(s) => s.toDouble }.sum / frameCount.toDouble).toString
        val b = to2DecimalPlaces(frames.map(_.processViewPercentage).collect { case Some(s) => s }.sum / frameCount.toDouble).toString

        s"""$a\t($b%)"""
      }

      val meanToDisplayable: String = {
        val a = to2DecimalPlaces(frames.map(_.toDisplayableDuration).collect { case Some(s) => s.toDouble }.sum / frameCount.toDouble).toString
        val b = to2DecimalPlaces(frames.map(_.toDisplayablePercentage).collect { case Some(s) => s }.sum / frameCount.toDouble).toString

        s"""$a\t($b%)"""
      }

      val meanRender: String = {
        val a = to2DecimalPlaces(frames.map(_.renderDuration).collect { case Some(s) => s.toDouble }.sum / frameCount.toDouble).toString
        val b = to2DecimalPlaces(frames.map(_.renderPercentage).collect { case Some(s) => s }.sum / frameCount.toDouble).toString

        s"""$a\t($b%)"""
      }

      Logger.info(
        s"""
          |**********************
          |Statistics:
          |Frames since last report:  $frameCount
          |Mean (avg) FPS:            $meanFps
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

// In Order!
case class FrameStartMetric(time: Long = System.currentTimeMillis()) extends Metric

case class UpdateStartMetric(time: Long = System.currentTimeMillis()) extends Metric
case class CallUpdateGameModelStartMetric(time: Long = System.currentTimeMillis()) extends Metric //nested
case class CallUpdateGameModelEndMetric(time: Long = System.currentTimeMillis()) extends Metric //nested
case class UpdateEndMetric(time: Long = System.currentTimeMillis()) extends Metric

case class CallUpdateViewStartMetric(time: Long = System.currentTimeMillis()) extends Metric
case class CallUpdateViewEndMetric(time: Long = System.currentTimeMillis()) extends Metric
case class ProcessViewStartMetric(time: Long = System.currentTimeMillis()) extends Metric
case class ProcessViewEndMetric(time: Long = System.currentTimeMillis()) extends Metric
case class ToDisplayableStartMetric(time: Long = System.currentTimeMillis()) extends Metric
case class ToDisplayableEndMetric(time: Long = System.currentTimeMillis()) extends Metric
case class RenderStartMetric(time: Long = System.currentTimeMillis()) extends Metric
case class RenderEndMetric(time: Long = System.currentTimeMillis()) extends Metric

case class SkippedModelUpdateMetric(time: Long = System.currentTimeMillis()) extends Metric
case class SkippedViewUpdateMetric(time: Long = System.currentTimeMillis()) extends Metric

case class FrameEndMetric(time: Long = System.currentTimeMillis()) extends Metric