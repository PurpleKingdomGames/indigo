package com.purplekingdomgames.indigo.util

import scala.collection.mutable

trait IMetrics {
  def record(m: Metric, time: Long = giveTime()): Unit
  def giveTime(): Long
}

object Metrics {

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

    case class FrameStats(general: FrameStatsGeneral, processView: FrameStatsProcessView, renderer: FrameStatsRenderer)

    case class FrameStatsGeneral(frameDuration: Long,
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

    case class FrameStatsProcessView()

    case class FrameStatsRenderer()

    private def extractDuration(metrics: List[MetricWrapper], startName: String, endName: String): Option[Long] =
      metrics.find(_.metric.name == startName).map(_.time).flatMap { start =>
        metrics.find(_.metric.name == endName).map(_.time - start)
      }

    private def asPercentOfFrameDuration(fd: Long, l: Option[Long]): Option[Double] =
      l.map(t => as2DecimalPlacePercent(fd, t))

    private def extractFrameStatistics(metrics: List[MetricWrapper]): Option[FrameStats] = {

      val frameDuration = extractDuration(metrics, FrameStartMetric.name, FrameEndMetric.name)

      //
      frameDuration.map { fd =>

        // General
        // Durations
        val updateDuration = extractDuration(metrics, UpdateStartMetric.name, UpdateEndMetric.name)
        val callUpdateModelDuration = extractDuration(metrics, CallUpdateGameModelStartMetric.name, CallUpdateGameModelEndMetric.name)
        val callUpdateViewDuration = extractDuration(metrics, CallUpdateViewStartMetric.name, CallUpdateViewEndMetric.name)
        val processViewDuration = extractDuration(metrics, ProcessViewStartMetric.name, ProcessViewEndMetric.name)
        val toDisplayableDuration = extractDuration(metrics, ToDisplayableStartMetric.name, ToDisplayableEndMetric.name)
        val renderDuration = extractDuration(metrics, RenderStartMetric.name, RenderEndMetric.name)

        // Percentages
        val updatePercentage = asPercentOfFrameDuration(fd, updateDuration)
        val updateModelPercentage = asPercentOfFrameDuration(fd, callUpdateModelDuration)
        val callUpdateViewPercentage = asPercentOfFrameDuration(fd, callUpdateViewDuration)
        val processViewPercentage = asPercentOfFrameDuration(fd, processViewDuration)
        val toDisplayablePercentage = asPercentOfFrameDuration(fd, toDisplayableDuration)
        val renderPercentage = asPercentOfFrameDuration(fd, renderDuration)

        // Process view
        // Durations

        // Percentages

        // Renderer
        // Durations

        // Percentages


        // Build results
        val general = FrameStatsGeneral(fd,
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

        val processView = FrameStatsProcessView()

        val renderer = FrameStatsRenderer()

        FrameStats(general, processView, renderer)
      }
    }

    private def calcMeanDuration(l: List[Option[Long]]): Double =
      to2DecimalPlaces(l.collect { case Some(s) => s.toDouble }.sum / l.length.toDouble)

    private def calcMeanPercentage(l: List[Option[Double]]): Double =
      to2DecimalPlaces(l.collect { case Some(s) => s }.sum / l.length.toDouble)

    private def report(metrics: List[MetricWrapper]): Unit = {

      // General Stats
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

      // Game Engine High Level
      val meanFrameDuration: String =
        to2DecimalPlaces(frames.map(_.general.frameDuration.toDouble).sum / frameCount.toDouble).toString

      val meanUpdateModel: String = {
        val a = calcMeanDuration(frames.map(_.general.callUpdateModelDuration)).toString
        val b = calcMeanPercentage(frames.map(_.general.updateModelPercentage)).toString

        s"""$a\t($b%)"""
      }

      val meanUpdate: String = {
        val a = calcMeanDuration(frames.map(_.general.updateDuration)).toString
        val b = calcMeanPercentage(frames.map(_.general.updatePercentage)).toString

        s"""$a\t($b%),\tcalling model update: $meanUpdateModel"""
      }

      val meanCallViewUpdate: String = {
        val a = calcMeanDuration(frames.map(_.general.callUpdateViewDuration)).toString
        val b = calcMeanPercentage(frames.map(_.general.callUpdateViewPercentage)).toString

        s"""$a\t($b%)"""
      }

      val meanProcess: String = {
        val a = calcMeanDuration(frames.map(_.general.processViewDuration)).toString
        val b = calcMeanPercentage(frames.map(_.general.processViewPercentage)).toString

        s"""$a\t($b%)"""
      }

      val meanToDisplayable: String = {
        val a = calcMeanDuration(frames.map(_.general.toDisplayableDuration)).toString
        val b = calcMeanPercentage(frames.map(_.general.toDisplayablePercentage)).toString

        s"""$a\t($b%)"""
      }

      val meanRender: String = {
        val a = calcMeanDuration(frames.map(_.general.renderDuration)).toString
        val b = calcMeanPercentage(frames.map(_.general.renderPercentage)).toString

        s"""$a\t($b%)"""
      }

      // Processing view


      // Renderer


      // Log it!
      Logger.info(
        s"""
          |**********************
          |Statistics:
          |-----------
          |Frames since last report:  $frameCount
          |Mean FPS:            $meanFps
          |Model updates skipped:     $modelUpdatesSkipped\t($modelSkipsPercent%)
          |View updates skipped:      $viewUpdatesSkipped\t($viewSkipsPercent%)
          |
          |Engine timings:
          |---------------
          |Mean frame length:         $meanFrameDuration
          |Mean model update:         $meanUpdate
          |Mean call view update:     $meanCallViewUpdate
          |Mean process view:         $meanProcess
          |Mean convert view:         $meanToDisplayable
          |Mean render view:          $meanRender
          |
          |View processing:
          |----------------
          |
          |Renderer:
          |---------
          |
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

// In Order (unless otherwise stated)!
case object FrameStartMetric extends Metric { val name: String = "frame start" }

case object UpdateStartMetric extends Metric { val name: String = "update model start" }
case object CallUpdateGameModelStartMetric extends Metric { val name: String = "call update model start" } //nested
case object CallUpdateGameModelEndMetric extends Metric { val name: String = "call update model end" } //nested
case object UpdateEndMetric extends Metric { val name: String = "update model end" }

case object CallUpdateViewStartMetric extends Metric { val name: String = "call update view start" }
case object CallUpdateViewEndMetric extends Metric { val name: String = "call update view end" }
case object ProcessViewStartMetric extends Metric { val name: String = "process view start" }
// Process metrics (below) go here.
case object ProcessViewEndMetric extends Metric { val name: String = "process view end" }
case object ToDisplayableStartMetric extends Metric { val name: String = "convert to displayable start" }
case object ToDisplayableEndMetric extends Metric { val name: String = "convert to displayable end" }
case object RenderStartMetric extends Metric { val name: String = "render start" }
// Renderer metrics (below) go here
case object RenderEndMetric extends Metric { val name: String = "render end" }

case object SkippedModelUpdateMetric extends Metric { val name: String = "skipped model update" }
case object SkippedViewUpdateMetric extends Metric { val name: String = "skipped view update" }

case object FrameEndMetric extends Metric { val name: String = "frame end" }

// Process view metrics
case object PersistGlobalViewEventsStartMetric extends Metric { val name: String = "persist global view events start" }
case object PersistGlobalViewEventsEndMetric extends Metric { val name: String = "persist global view events end" }

case object ConvertToInternalStartMetric extends Metric { val name: String = "convert to internal start" }
case object ConvertToInternalEndMetric extends Metric { val name: String = "convert to internal end" }

case object PersistNodeViewEventsStartMetric extends Metric { val name: String = "persist node view events start" }
case object PersistNodeViewEventsEndMetric extends Metric { val name: String = "persist node view events end" }

case object ApplyAnimationMementoStartMetric extends Metric { val name: String = "apply animation mementos start" }
case object ApplyAnimationMementoEndMetric extends Metric { val name: String = "apply animation mementos end" }

case object RunAnimationActionsStartMetric extends Metric { val name: String = "run animation actions start" }
case object RunAnimationActionsEndMetric extends Metric { val name: String = "run animation actions end" }

case object PersistAnimationStatesStartMetric extends Metric { val name: String = "persist animation states start" }
case object PersistAnimationStatesEndMetric extends Metric { val name: String = "persist animation states end" }

// Renderer metrics
case object DrawGameLayerStartMetric extends Metric { val name: String = " start" }
case object DrawGameLayerEndMetric extends Metric { val name: String = " end" }

case object DrawLightingLayerStartMetric extends Metric { val name: String = " start" }
case object DrawLightingLayerEndMetric extends Metric { val name: String = " end" }

case object DrawUiLayerStartMetric extends Metric { val name: String = " start" }
case object DrawUiLayerEndMetric extends Metric { val name: String = " end" }

case object RenderToConvasStartMetric extends Metric { val name: String = " start" }
case object RenderToConvasEndMetric extends Metric { val name: String = " end" }

case object LightingDrawCallMetric extends Metric { val name: String = "draw call: lighting" }
case object NormalLayerDrawCallMetric extends Metric { val name: String = "draw call: normal" }
case object ToCanvasDrawCallMetric extends Metric { val name: String = "draw call: to canvas" }
