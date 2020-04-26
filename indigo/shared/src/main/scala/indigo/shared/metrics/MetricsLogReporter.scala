package indigo.shared.metrics

import indigo.shared.IndigoLogger

import indigo.shared.EqualTo._

import scala.annotation.tailrec

object MetricsLogReporter {

  private def to2DecimalPlaces(d: Double): Double =
    Math.round(d * 100d) / 100d

  private def as2DecimalPlacePercent(a: Long, b: Long): Double =
    to2DecimalPlaces(100d / a * b)
  private def as2DecimalPlacePercent(a: Int, b: Int): Double =
    to2DecimalPlaces(100d / a * b)

  private def extractDuration(metrics: List[MetricWrapper], startName: String, endName: String): Option[Long] =
    metrics.find(_.metric.name === startName).map(_.time).flatMap { start =>
      metrics.find(_.metric.name === endName).map(_.time - start)
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
      val callFrameProcessorDuration =
        extractDuration(metrics, CallFrameProcessorStartMetric.name, CallFrameProcessorEndMetric.name)
      val callUpdateViewDuration = extractDuration(metrics, CallUpdateViewStartMetric.name, CallUpdateViewEndMetric.name)
      val processViewDuration    = extractDuration(metrics, ProcessViewStartMetric.name, ProcessViewEndMetric.name)
      val toDisplayableDuration  = extractDuration(metrics, ToDisplayableStartMetric.name, ToDisplayableEndMetric.name)
      val renderDuration         = extractDuration(metrics, RenderStartMetric.name, RenderEndMetric.name)
      val audioDuration          = extractDuration(metrics, AudioStartMetric.name, AudioEndMetric.name)

      // Percentages
      val updatePercentage         = asPercentOfFrameDuration(fd, updateDuration)
      val frameProcessorPercentage = asPercentOfFrameDuration(fd, callFrameProcessorDuration)
      val callUpdateViewPercentage = asPercentOfFrameDuration(fd, callUpdateViewDuration)
      val processViewPercentage    = asPercentOfFrameDuration(fd, processViewDuration)
      val toDisplayablePercentage  = asPercentOfFrameDuration(fd, toDisplayableDuration)
      val renderPercentage         = asPercentOfFrameDuration(fd, renderDuration)
      val audioPercentage          = asPercentOfFrameDuration(fd, audioDuration)

      // Process view
      // Durations
      val persistGlobalViewEventsDuration =
        extractDuration(metrics, PersistGlobalViewEventsStartMetric.name, PersistGlobalViewEventsEndMetric.name)
      val persistNodeViewEventsDuration =
        extractDuration(metrics, PersistNodeViewEventsStartMetric.name, PersistNodeViewEventsEndMetric.name)
      val applyAnimationMementosDuration =
        extractDuration(metrics, ApplyAnimationMementoStartMetric.name, ApplyAnimationMementoEndMetric.name)
      val runAnimationActionsDuration =
        extractDuration(metrics, RunAnimationActionsStartMetric.name, RunAnimationActionsEndMetric.name)
      val persistAnimationStatesDuration =
        extractDuration(metrics, PersistAnimationStatesStartMetric.name, PersistAnimationStatesEndMetric.name)

      // Percentages
      val persistGlobalViewEventsPercentage = asPercentOfFrameDuration(fd, persistGlobalViewEventsDuration)
      val persistNodeViewEventsPercentage   = asPercentOfFrameDuration(fd, persistNodeViewEventsDuration)
      val applyAnimationMementosPercentage  = asPercentOfFrameDuration(fd, applyAnimationMementosDuration)
      val runAnimationActionsPercentage     = asPercentOfFrameDuration(fd, runAnimationActionsDuration)
      val persistAnimationStatesPercentage  = asPercentOfFrameDuration(fd, persistAnimationStatesDuration)

      // Renderer
      // Durations
      val drawGameLayerDuration       = extractDuration(metrics, DrawGameLayerStartMetric.name, DrawGameLayerEndMetric.name)
      val drawLightsLayerDuration     = extractDuration(metrics, DrawLightsLayerStartMetric.name, DrawLightsLayerEndMetric.name)
      val drawLightingLayerDuration   = extractDuration(metrics, DrawLightingLayerStartMetric.name, DrawLightingLayerEndMetric.name)
      val drawDistortionLayerDuration = extractDuration(metrics, DrawDistortionLayerStartMetric.name, DrawDistortionLayerEndMetric.name)
      val drawUiLayerDuration         = extractDuration(metrics, DrawUiLayerStartMetric.name, DrawUiLayerEndMetric.name)
      val renderToWindowDuration      = extractDuration(metrics, RenderToWindowStartMetric.name, RenderToWindowEndMetric.name)

      val normalDrawCallDuration =
        extractDuration(metrics, NormalDrawCallLengthStartMetric.name, NormalDrawCallLengthEndMetric.name)
      val lightsDrawCallDuration =
        extractDuration(metrics, LightsDrawCallLengthStartMetric.name, LightsDrawCallLengthEndMetric.name)
      val lightingDrawCallDuration =
        extractDuration(metrics, LightingDrawCallLengthStartMetric.name, LightingDrawCallLengthEndMetric.name)
      val distortionDrawCallDuration =
        extractDuration(metrics, DistortionDrawCallLengthStartMetric.name, DistortionDrawCallLengthEndMetric.name)
      val uiDrawCallDuration =
        extractDuration(metrics, UiDrawCallLengthStartMetric.name, UiDrawCallLengthEndMetric.name)
      val toWindowDrawCallDuration =
        extractDuration(metrics, ToWindowDrawCallLengthStartMetric.name, ToWindowDrawCallLengthEndMetric.name)

      // Percentages
      val drawGameLayerPercentage       = asPercentOfFrameDuration(fd, drawGameLayerDuration)
      val drawLightsLayerPercentage     = asPercentOfFrameDuration(fd, drawLightsLayerDuration)
      val drawLightingLayerPercentage   = asPercentOfFrameDuration(fd, drawLightingLayerDuration)
      val drawDistortionLayerPercentage = asPercentOfFrameDuration(fd, drawDistortionLayerDuration)
      val drawUiLayerPercentage         = asPercentOfFrameDuration(fd, drawUiLayerDuration)
      val renderToWindowPercentage      = asPercentOfFrameDuration(fd, renderToWindowDuration)

      // Draw Call Counts
      val lightingDrawCalls: Int   = metrics.count(_.metric.name === LightingDrawCallMetric.name)
      val distortionDrawCalls: Int = metrics.count(_.metric.name === DistortionDrawCallMetric.name)
      val normalDrawCalls: Int     = metrics.count(_.metric.name === NormalLayerDrawCallMetric.name)
      val lightsDrawCalls: Int     = metrics.count(_.metric.name === LightsLayerDrawCallMetric.name)
      val uiDrawCalls: Int         = metrics.count(_.metric.name === UiLayerDrawCallMetric.name)
      val toWindowDrawCalls: Int   = metrics.count(_.metric.name === ToWindowDrawCallMetric.name)

      // Build results
      val general = FrameStatsGeneral(
        fd,
        updateDuration,
        callFrameProcessorDuration,
        callUpdateViewDuration,
        processViewDuration,
        toDisplayableDuration,
        renderDuration,
        audioDuration,
        updatePercentage,
        frameProcessorPercentage,
        callUpdateViewPercentage,
        processViewPercentage,
        toDisplayablePercentage,
        renderPercentage,
        audioPercentage
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
        drawLightsLayerDuration,
        drawLightingLayerDuration,
        drawDistortionLayerDuration,
        drawUiLayerDuration,
        renderToWindowDuration,
        drawGameLayerPercentage,
        drawLightsLayerPercentage,
        drawLightingLayerPercentage,
        drawDistortionLayerPercentage,
        drawUiLayerPercentage,
        renderToWindowPercentage,
        lightingDrawCalls,
        distortionDrawCalls,
        normalDrawCalls,
        uiDrawCalls,
        lightsDrawCalls,
        toWindowDrawCalls,
        normalDrawCallDuration,
        uiDrawCallDuration,
        lightsDrawCallDuration,
        lightingDrawCallDuration,
        distortionDrawCallDuration,
        toWindowDrawCallDuration
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
    @tailrec
    def rec(remaining: List[MetricWrapper], accFrame: List[MetricWrapper], acc: List[List[MetricWrapper]]): List[List[MetricWrapper]] =
      remaining match {
        case Nil => acc
        case MetricWrapper(FrameEndMetric, time) :: ms =>
          rec(ms, Nil, (MetricWrapper(FrameEndMetric, time) :: accFrame) :: acc)
        case m :: ms =>
          rec(ms, m :: accFrame, acc)
      }

    rec(metrics, Nil, Nil)
  }

  def report(metrics: List[MetricWrapper]): Unit = {

    // General Stats
    val frames: List[FrameStats] = splitIntoFrames(metrics).map(extractFrameStatistics).collect { case Some(s) => s }
    val frameCount: Int          = frames.length
    val period: Option[Long] =
      metrics.headOption
        .map(_.time)
        .flatMap { start =>
          metrics.reverse.headOption.map(_.time - start)
        }
    val meanFps: String =
      period.map(p => frameCount / (p / 1000).toInt).map(_.toString()).getOrElse("<missing>")

    val modelUpdatesSkipped: Int  = metrics.collect { case m @ MetricWrapper(SkippedModelUpdateMetric, _) => m }.length
    val modelSkipsPercent: Double = as2DecimalPlacePercent(frameCount, modelUpdatesSkipped)
    val viewUpdatesSkipped: Int   = metrics.collect { case m @ MetricWrapper(SkippedViewUpdateMetric, _) => m }.length
    val viewSkipsPercent: Double  = as2DecimalPlacePercent(frameCount, viewUpdatesSkipped)

    // Game Engine High Level
    val meanFrameDuration: String =
      to2DecimalPlaces(frames.map(_.general.frameDuration.toDouble).sum / frameCount.toDouble).toString

    val meanFrameProcessor: String = {
      val a = calcMeanDuration(frames.map(_.general.frameProcessorDuration)).toString
      val b = calcMeanPercentage(frames.map(_.general.frameProcessorPercentage)).toString

      s"""$a\t($b%)"""
    }

    val meanUpdate: String = {
      val a = calcMeanDuration(frames.map(_.general.updateDuration)).toString
      val b = calcMeanPercentage(frames.map(_.general.updatePercentage)).toString

      s"""$a\t($b%),\tcalling frame processor: $meanFrameProcessor"""
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

    val meanAudio: String = {
      val a = calcMeanDuration(frames.map(_.general.audioDuration)).toString
      val b = calcMeanPercentage(frames.map(_.general.audioPercentage)).toString

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
    val meanDrawLightsLayer: String = {
      val a = calcMeanDuration(frames.map(_.renderer.drawLightsLayerDuration)).toString
      val b = calcMeanPercentage(frames.map(_.renderer.drawLightsLayerPercentage)).toString

      s"""$a\t($b%)"""
    }

    val meanDrawLightingLayer: String = {
      val a = calcMeanDuration(frames.map(_.renderer.drawLightingLayerDuration)).toString
      val b = calcMeanPercentage(frames.map(_.renderer.drawLightingLayerPercentage)).toString

      s"""$a\t($b%)"""
    }

    val meanDrawDistortionLayer: String = {
      val a = calcMeanDuration(frames.map(_.renderer.drawDistortionLayerDuration)).toString
      val b = calcMeanPercentage(frames.map(_.renderer.drawDistortionLayerPercentage)).toString

      s"""$a\t($b%)"""
    }

    val meanDrawUiLayer: String = {
      val a = calcMeanDuration(frames.map(_.renderer.drawUiLayerDuration)).toString
      val b = calcMeanPercentage(frames.map(_.renderer.drawUiLayerPercentage)).toString

      s"""$a\t($b%)"""
    }

    val meanRenderToWindowLayer: String = {
      val a = calcMeanDuration(frames.map(_.renderer.renderToWindowDuration)).toString
      val b = calcMeanPercentage(frames.map(_.renderer.renderToWindowPercentage)).toString

      s"""$a\t($b%)"""
    }

    val meanLightingDrawCalls: String = {
      val a = calcMeanCount(frames.map(_.renderer.lightingDrawCalls)).toString

      s"""$a"""
    }

    val meanDistortionDrawCalls: String = {
      val a = calcMeanCount(frames.map(_.renderer.distortionDrawCalls)).toString

      s"""$a"""
    }
    val meanNormalDrawCalls: String = {
      val a = calcMeanCount(frames.map(_.renderer.normalDrawCalls)).toString

      s"""$a"""
    }
    val meanUiDrawCalls: String = {
      val a = calcMeanCount(frames.map(_.renderer.uiDrawCalls)).toString

      s"""$a"""
    }
    val meanLightsDrawCalls: String = {
      val a = calcMeanCount(frames.map(_.renderer.lightsDrawCalls)).toString

      s"""$a"""
    }
    val meanToWindowDrawCalls: String = {
      val a = calcMeanCount(frames.map(_.renderer.toWindowDrawCalls)).toString

      s"""$a"""
    }

    val meanNormalDrawCallTime: String = {
      val a = calcMeanDuration(frames.map(_.renderer.normalDrawCallDuration)).toString

      s"""$a"""
    }

    val meanUiDrawCallTime: String = {
      val a = calcMeanDuration(frames.map(_.renderer.normalDrawCallDuration)).toString

      s"""$a"""
    }

    val meanLightsDrawCallTime: String = {
      val a = calcMeanDuration(frames.map(_.renderer.normalDrawCallDuration)).toString

      s"""$a"""
    }

    val meanLightingDrawCallTime: String = {
      val a = calcMeanDuration(frames.map(_.renderer.lightingDrawCallDuration)).toString

      s"""$a"""
    }

    val meanDistortionDrawCallTime: String = {
      val a = calcMeanDuration(frames.map(_.renderer.distortionDrawCallDuration)).toString

      s"""$a"""
    }

    val meanToWindowDrawCallTime: String = {
      val a = calcMeanDuration(frames.map(_.renderer.normalDrawCallDuration)).toString

      s"""$a"""
    }

    // Log it!
    IndigoLogger.info(
      s"""
         |**********************
         |Statistics:
         |-----------
         |Frames since last report:  ${frameCount.toString()}
         |Mean FPS:                  ${meanFps.toString()}
         |Model updates skipped:     ${modelUpdatesSkipped.toString()}\t(${modelSkipsPercent.toString()}%)
         |View updates skipped:      ${viewUpdatesSkipped.toString()}\t(${viewSkipsPercent.toString()}%)
         |
         |Engine timings:
         |---------------
         |Mean frame length:         ${meanFrameDuration.toString()}
         |Mean update:               ${meanUpdate.toString()}
         |Mean call view update:     ${meanCallViewUpdate.toString()}
         |Mean process view:         ${meanProcess.toString()}
         |Mean convert view:         ${meanToDisplayable.toString()}
         |Mean render view:          ${meanRender.toString()}
         |Mean play audio:           ${meanAudio.toString()}
         |
         |View processing:
         |----------------
         |Mean persist global view:  ${meanPersistGlobalView.toString()}
         |Mean persist node view:    ${meanPersistNodeView.toString()}
         |Mean apply animations:     ${meanApplyAnimationMementos.toString()}
         |Mean animation actions:    ${meanRunAnimationActions.toString()}
         |Mean persist animations:   ${meanPersistAnimationStates.toString()}
         |
         |Renderer:
         |---------
         |Mean draw game layer:       ${meanDrawGameLayer.toString()}
         |Mean draw lights layer:     ${meanDrawLightsLayer.toString()}
         |Mean draw lighting layer:   ${meanDrawLightingLayer.toString()}
         |Mean draw distortion layer: ${meanDrawDistortionLayer.toString()}
         |Mean draw ui layer:         ${meanDrawUiLayer.toString()}
         |Mean render to window:      ${meanRenderToWindowLayer.toString()}
         |
         |Mean lighting draw calls:   ${meanLightingDrawCalls.toString()}
         |Mean distortion draw calls: ${meanDistortionDrawCalls.toString()}
         |Mean game draw calls:       ${meanNormalDrawCalls.toString()}
         |Mean ui draw calls:         ${meanUiDrawCalls.toString()}
         |Mean lights draw calls:     ${meanLightsDrawCalls.toString()}
         |Mean to window draw calls:  ${meanToWindowDrawCalls.toString()}
         |
         |Mean lighting draw time:    ${meanLightingDrawCallTime.toString()}
         |Mean distortion draw time:  ${meanDistortionDrawCallTime.toString()}
         |Mean game draw time:        ${meanNormalDrawCallTime.toString()}
         |Mean ui draw time:          ${meanUiDrawCallTime.toString()}
         |Mean lights draw time:      ${meanLightsDrawCallTime.toString()}
         |Mean to window draw time:   ${meanToWindowDrawCallTime.toString()}
         |**********************
        """.stripMargin
    )
  }

}

final case class FrameStats(general: FrameStatsGeneral, processView: FrameStatsProcessView, renderer: FrameStatsRenderer)

final case class FrameStatsGeneral(
    frameDuration: Long,
    updateDuration: Option[Long],
    frameProcessorDuration: Option[Long],
    callUpdateViewDuration: Option[Long],
    processViewDuration: Option[Long],
    toDisplayableDuration: Option[Long],
    renderDuration: Option[Long],
    audioDuration: Option[Long],
    updatePercentage: Option[Double],
    frameProcessorPercentage: Option[Double],
    callUpdateViewPercentage: Option[Double],
    processViewPercentage: Option[Double],
    toDisplayablePercentage: Option[Double],
    renderPercentage: Option[Double],
    audioPercentage: Option[Double]
)

final case class FrameStatsProcessView(
    persistGlobalViewEventsDuration: Option[Long],
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

final case class FrameStatsRenderer(
    drawGameLayerDuration: Option[Long],
    drawLightsLayerDuration: Option[Long],
    drawLightingLayerDuration: Option[Long],
    drawDistortionLayerDuration: Option[Long],
    drawUiLayerDuration: Option[Long],
    renderToWindowDuration: Option[Long],
    drawGameLayerPercentage: Option[Double],
    drawLightsLayerPercentage: Option[Double],
    drawLightingLayerPercentage: Option[Double],
    drawDistortionLayerPercentage: Option[Double],
    drawUiLayerPercentage: Option[Double],
    renderToWindowPercentage: Option[Double],
    lightingDrawCalls: Int,
    distortionDrawCalls: Int,
    normalDrawCalls: Int,
    uiDrawCalls: Int,
    lightsDrawCalls: Int,
    toWindowDrawCalls: Int,
    normalDrawCallDuration: Option[Long],
    uiDrawCallDuration: Option[Long],
    lightsDrawCallDuration: Option[Long],
    lightingDrawCallDuration: Option[Long],
    distortionDrawCallDuration: Option[Long],
    toWindowDrawCallDuration: Option[Long]
)
