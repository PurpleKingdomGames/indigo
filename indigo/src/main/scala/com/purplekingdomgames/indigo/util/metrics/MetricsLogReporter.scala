package com.purplekingdomgames.indigo.util.metrics

import com.purplekingdomgames.indigo.util._

object MetricsLogReporter {

  private def to2DecimalPlaces(d: Double): Double =
    Math.round(d * 100d) / 100d

  private def as2DecimalPlacePercent(a: Long, b: Long): Double =
    to2DecimalPlaces(100d / a * b)
  private def as2DecimalPlacePercent(a: Int, b: Int): Double =
    to2DecimalPlaces(100d / a * b)

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
      val persistGlobalViewEventsDuration = extractDuration(metrics, PersistGlobalViewEventsStartMetric.name, PersistGlobalViewEventsEndMetric.name)
      val persistNodeViewEventsDuration = extractDuration(metrics, PersistNodeViewEventsStartMetric.name, PersistNodeViewEventsEndMetric.name)
      val applyAnimationMementosDuration = extractDuration(metrics, ApplyAnimationMementoStartMetric.name, ApplyAnimationMementoEndMetric.name)
      val runAnimationActionsDuration = extractDuration(metrics, RunAnimationActionsStartMetric.name, RunAnimationActionsEndMetric.name)
      val persistAnimationStatesDuration = extractDuration(metrics, PersistAnimationStatesStartMetric.name, PersistAnimationStatesEndMetric.name)

      // Percentages
      val persistGlobalViewEventsPercentage = asPercentOfFrameDuration(fd, persistGlobalViewEventsDuration)
      val persistNodeViewEventsPercentage = asPercentOfFrameDuration(fd, persistNodeViewEventsDuration)
      val applyAnimationMementosPercentage = asPercentOfFrameDuration(fd, applyAnimationMementosDuration)
      val runAnimationActionsPercentage = asPercentOfFrameDuration(fd, runAnimationActionsDuration)
      val persistAnimationStatesPercentage = asPercentOfFrameDuration(fd, persistAnimationStatesDuration)


      // Renderer
      // Durations
      val drawGameLayerDuration = extractDuration(metrics, DrawGameLayerStartMetric.name, DrawGameLayerEndMetric.name)
      val drawLightingLayerDuration = extractDuration(metrics, DrawLightingLayerStartMetric.name, DrawLightingLayerEndMetric.name)
      val drawUiLayerDuration = extractDuration(metrics, DrawUiLayerStartMetric.name, DrawUiLayerEndMetric.name)
      val renderToCanvasDuration = extractDuration(metrics, RenderToConvasStartMetric.name, RenderToConvasEndMetric.name)

      val normalDrawCallDuration = extractDuration(metrics, NormalDrawCallLengthStartMetric.name, NormalDrawCallLengthEndMetric.name)
      val lightingDrawCallDuration = extractDuration(metrics, LightingDrawCallLengthStartMetric.name, LightingDrawCallLengthEndMetric.name)
      val toCanvasDrawCallDuration = extractDuration(metrics, ToCanvasDrawCallLengthStartMetric.name, ToCanvasDrawCallLengthEndMetric.name)

      // Percentages
      val drawGameLayerPercentage = asPercentOfFrameDuration(fd, drawGameLayerDuration)
      val drawLightingLayerPercentage = asPercentOfFrameDuration(fd, drawLightingLayerDuration)
      val drawUiLayerPercentage = asPercentOfFrameDuration(fd, drawUiLayerDuration)
      val renderToCanvasPercentage = asPercentOfFrameDuration(fd, renderToCanvasDuration)


      // Draw Call Counts
      val lightingDrawCalls: Int = metrics.count(_.metric.name == LightingDrawCallMetric.name)
      val normalDrawCalls: Int = metrics.count(_.metric.name == NormalLayerDrawCallMetric.name)
      val toCanvasDrawCalls: Int = metrics.count(_.metric.name == ToCanvasDrawCallMetric.name)


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

      val processView = FrameStatsProcessView(
        persistGlobalViewEventsDuration,
        persistNodeViewEventsDuration,
        applyAnimationMementosDuration,
        runAnimationActionsDuration,
        persistAnimationStatesDuration,
        persistGlobalViewEventsPercentage,
        persistNodeViewEventsPercentage,
        applyAnimationMementosPercentage,
        runAnimationActionsPercentage,
        persistAnimationStatesPercentage
      )

      val renderer = FrameStatsRenderer(
        drawGameLayerDuration,
        drawLightingLayerDuration,
        drawUiLayerDuration,
        renderToCanvasDuration,
        drawGameLayerPercentage,
        drawLightingLayerPercentage,
        drawUiLayerPercentage,
        renderToCanvasPercentage,
        lightingDrawCalls,
        normalDrawCalls,
        toCanvasDrawCalls,
        normalDrawCallDuration,
        lightingDrawCallDuration,
        toCanvasDrawCallDuration
      )

      FrameStats(general, processView, renderer)
    }
  }

  private def calcMeanCount(l: List[Int]): Double =
    to2DecimalPlaces(l.sum / l.length.toDouble)

  private def calcMeanDuration(l: List[Option[Long]]): Double =
    to2DecimalPlaces(l.collect { case Some(s) => s.toDouble }.sum / l.length.toDouble)

  private def calcMeanPercentage(l: List[Option[Double]]): Double =
    to2DecimalPlaces(l.collect { case Some(s) => s }.sum / l.length.toDouble)


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

  def report(metrics: List[MetricWrapper]): Unit = {

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
    val meanPersistGlobalView: String = {
      val a = calcMeanDuration(frames.map(_.processView.persistGlobalViewEventsDuration)).toString
      val b = calcMeanPercentage(frames.map(_.processView.persistGlobalViewEventsPercentage)).toString

      s"""$a\t($b%)"""
    }
    val meanPersistNodeView: String = {
      val a = calcMeanDuration(frames.map(_.processView.persistNodeViewEventsDuration)).toString
      val b = calcMeanPercentage(frames.map(_.processView.persistNodeViewEventsPercentage)).toString

      s"""$a\t($b%)"""
    }
    val meanApplyAnimationMementos: String = {
      val a = calcMeanDuration(frames.map(_.processView.applyAnimationMementosDuration)).toString
      val b = calcMeanPercentage(frames.map(_.processView.applyAnimationMementosPercentage)).toString

      s"""$a\t($b%)"""
    }
    val meanRunAnimationActions: String = {
      val a = calcMeanDuration(frames.map(_.processView.runAnimationActionsDuration)).toString
      val b = calcMeanPercentage(frames.map(_.processView.runAnimationActionsPercentage)).toString

      s"""$a\t($b%)"""
    }
    val meanPersistAnimationStates: String = {
      val a = calcMeanDuration(frames.map(_.processView.persistAnimationStatesDuration)).toString
      val b = calcMeanPercentage(frames.map(_.processView.persistAnimationStatesPercentage)).toString

      s"""$a\t($b%)"""
    }


    // Renderer
    val meanDrawGameLayer: String = {
      val a = calcMeanDuration(frames.map(_.renderer.drawGameLayerDuration)).toString
      val b = calcMeanPercentage(frames.map(_.renderer.drawGameLayerPercentage)).toString

      s"""$a\t($b%)"""
    }

    val meanDrawLightingLayer: String = {
      val a = calcMeanDuration(frames.map(_.renderer.drawLightingLayerDuration)).toString
      val b = calcMeanPercentage(frames.map(_.renderer.drawLightingLayerPercentage)).toString

      s"""$a\t($b%)"""
    }

    val meanDrawUiLayer: String = {
      val a = calcMeanDuration(frames.map(_.renderer.drawUiLayerDuration)).toString
      val b = calcMeanPercentage(frames.map(_.renderer.drawUiLayerPercentage)).toString

      s"""$a\t($b%)"""
    }

    val meanRenderToCanvasLayer: String = {
      val a = calcMeanDuration(frames.map(_.renderer.renderToCanvasDuration)).toString
      val b = calcMeanPercentage(frames.map(_.renderer.renderToCanvasPercentage)).toString

      s"""$a\t($b%)"""
    }

    val meanLightingDrawCalls: String = {
      val a = calcMeanCount(frames.map(_.renderer.lightingDrawCalls)).toString

      s"""$a"""
    }
    val meanNoramlDrawCalls: String = {
      val a = calcMeanCount(frames.map(_.renderer.normalDrawCalls)).toString

      s"""$a"""
    }
    val meanToCanvasDrawCalls: String = {
      val a = calcMeanCount(frames.map(_.renderer.toCanvasDrawCalls)).toString

      s"""$a"""
    }

    val meanNormalDrawCallTime: String = {
      val a = calcMeanDuration(frames.map(_.renderer.normalDrawCallDuration)).toString

      s"""$a"""
    }

    val meanLightingDrawCallTime: String = {
      val a = calcMeanDuration(frames.map(_.renderer.normalDrawCallDuration)).toString

      s"""$a"""
    }

    val meanToCanvasDrawCallTime: String = {
      val a = calcMeanDuration(frames.map(_.renderer.normalDrawCallDuration)).toString

      s"""$a"""
    }

    // Log it!
    Logger.info(
      s"""
         |**********************
         |Statistics:
         |-----------
         |Frames since last report:  $frameCount
         |Mean FPS:                  $meanFps
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
         |Mean persist global view:  $meanPersistGlobalView
         |Mean persist node view:    $meanPersistNodeView
         |Mean apply animations:     $meanApplyAnimationMementos
         |Mean animation actions:    $meanRunAnimationActions
         |Mean persist animations:   $meanPersistAnimationStates
         |
         |Renderer:
         |---------
         |Mean draw game layer:      $meanDrawGameLayer
         |Mean draw lighting layer:  $meanDrawLightingLayer
         |Mean draw ui layer:        $meanDrawUiLayer
         |Mean render to canvas:     $meanRenderToCanvasLayer
         |
         |Mean lighting draw calls:  $meanLightingDrawCalls
         |Mean normal draw calls:    $meanNoramlDrawCalls
         |Mean to canvas draw calls: $meanToCanvasDrawCalls
         |
         |Mean lighting draw time:   $meanLightingDrawCallTime
         |Mean normal draw time:     $meanNormalDrawCallTime
         |Mean to canvas draw time:  $meanToCanvasDrawCallTime
         |**********************
        """.stripMargin
    )
  }

}

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

case class FrameStatsProcessView(persistGlobalViewEventsDuration: Option[Long],
                                 persistNodeViewEventsDuration: Option[Long],
                                 applyAnimationMementosDuration: Option[Long],
                                 runAnimationActionsDuration: Option[Long],
                                 persistAnimationStatesDuration: Option[Long],
                                 persistGlobalViewEventsPercentage: Option[Double],
                                 persistNodeViewEventsPercentage: Option[Double],
                                 applyAnimationMementosPercentage: Option[Double],
                                 runAnimationActionsPercentage: Option[Double],
                                 persistAnimationStatesPercentage: Option[Double]
                                )

case class FrameStatsRenderer(drawGameLayerDuration: Option[Long],
                              drawLightingLayerDuration: Option[Long],
                              drawUiLayerDuration: Option[Long],
                              renderToCanvasDuration: Option[Long],
                              drawGameLayerPercentage: Option[Double],
                              drawLightingLayerPercentage: Option[Double],
                              drawUiLayerPercentage: Option[Double],
                              renderToCanvasPercentage: Option[Double],
                              lightingDrawCalls: Int,
                              normalDrawCalls: Int,
                              toCanvasDrawCalls: Int,
                              normalDrawCallDuration: Option[Long],
                              lightingDrawCallDuration: Option[Long],
                              toCanvasDrawCallDuration: Option[Long]
                             )