package com.purplekingdomgames.shared

import io.circe.generic.auto._
import io.circe.parser._

case class GameConfig(viewport: GameViewport, frameRate: Int, clearColor: ClearColor, magnification: Int, advanced: AdvancedGameConfig) {
  val frameRateDeltaMillis: Int = 1000 / frameRate

  val haltViewUpdatesAt: Int = frameRateDeltaMillis * 2
  val haltModelUpdatesAt: Int = frameRateDeltaMillis * 3

  val asString: String =
    s"""
       |Viewpoint:      [${viewport.width}, ${viewport.height}]
       |FPS:            $frameRate
       |frameRateDelta: $frameRateDeltaMillis (view updates stop at: $haltViewUpdatesAt, model at: $haltModelUpdatesAt
       |Clear color:    {red: ${clearColor.r}, green: ${clearColor.g}, blue: ${clearColor.b}, alpha: ${clearColor.a}}
       |Magnification:  $magnification
       |""".stripMargin

  def withViewport(width: Int, height: Int): GameConfig = this.copy(viewport = GameViewport(width, height))
  def withFrameRate(frameRate: Int): GameConfig = this.copy(frameRate = frameRate)
  def withClearColor(clearColor: ClearColor): GameConfig = this.copy(clearColor = clearColor)
  def withMagnification(magnification: Int): GameConfig = this.copy(magnification = magnification)

  def withAdvancedSettings(advanced: AdvancedGameConfig): GameConfig = this.copy(advanced = advanced)
  def metricsEnabled: GameConfig = this.copy(advanced = advanced.copy(recordMetrics = true))
  def metricsDisabled: GameConfig = this.copy(advanced = advanced.copy(recordMetrics = false))
  def withLogInterval(milliseconds: Int): GameConfig = this.copy(advanced = advanced.copy(logMetricsReportIntervalMs = milliseconds))
  def disableSkipModelUpdates: GameConfig = this.copy(advanced = advanced.copy(disableSkipModelUpdates = true))
  def enableSkipModelUpdates: GameConfig = this.copy(advanced = advanced.copy(disableSkipModelUpdates = false))
  def disableSkipViewUpdates: GameConfig = this.copy(advanced = advanced.copy(disableSkipViewUpdates = true))
  def enableSkipViewUpdates: GameConfig = this.copy(advanced = advanced.copy(disableSkipViewUpdates = false))
}

object GameConfig {

  val default: GameConfig =
    GameConfig(GameViewport(550, 400), 30, ClearColor.Black, 1, AdvancedGameConfig.default)

  def apply(width: Int, height: Int, frameRate: Int): GameConfig =
    GameConfig(GameViewport(width, height), frameRate, ClearColor.Black, 1, AdvancedGameConfig.default)

  def apply(viewport: GameViewport, frameRate: Int, clearColor: ClearColor, magnification: Int): GameConfig =
    GameConfig(viewport, frameRate, clearColor, magnification, AdvancedGameConfig.default)

  def fromJson(json: String): Either[String, GameConfig] =
    decode[GameConfig](json) match {
      case Right(c) =>
        Right(c)

      case Left(e) =>
        Left("Failed to deserialise json into GameConfig: " + e.getMessage)
    }

}

case class GameViewport(width: Int, height: Int) {
  val horizontalMiddle: Int = width / 2
  val verticalMiddle: Int = height / 2
  val center: (Int, Int) = (horizontalMiddle, verticalMiddle)
}

case class AdvancedGameConfig(recordMetrics: Boolean, logMetricsReportIntervalMs: Int, disableSkipModelUpdates: Boolean, disableSkipViewUpdates: Boolean)
object AdvancedGameConfig {
  val default: AdvancedGameConfig = AdvancedGameConfig(
    recordMetrics = false,
    logMetricsReportIntervalMs = 10000,
    disableSkipModelUpdates = false,
    disableSkipViewUpdates = false
  )
}
