package com.purplekingdomgames.indigo.gameengine

import com.purplekingdomgames.indigo.renderer.ClearColor

case class GameConfig(viewport: GameViewport, frameRate: Int, clearColor: ClearColor, magnification: Int, recordMetrics: Boolean) {
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
  def metricsEnabled: GameConfig = this.copy(recordMetrics = true)
  def metricsDisabled: GameConfig = this.copy(recordMetrics = false)
}

object GameConfig {

  val default: GameConfig =
    GameConfig(GameViewport(550, 400), 30, ClearColor.Black, 1, recordMetrics = false)

  def apply(width: Int, height: Int, frameRate: Int): GameConfig =
    GameConfig(GameViewport(width, height), frameRate, ClearColor.Black, 1, recordMetrics = false)

}

case class GameViewport(width: Int, height: Int)

//case class AdvancedGameConfig()