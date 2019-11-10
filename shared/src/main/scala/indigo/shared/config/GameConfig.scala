package indigo.shared.config

import indigo.shared.ClearColor
import indigo.shared.datatypes.Rectangle

import scala.scalajs.js.annotation._

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("GameConfig")
@JSExportAll
final case class GameConfig(viewport: GameViewport, frameRate: Int, clearColor: ClearColor, magnification: Int, advanced: AdvancedGameConfig) {
  val frameRateDeltaMillis: Int = 1000 / frameRate

  val haltViewUpdatesAt: Int  = frameRateDeltaMillis * 2
  val haltModelUpdatesAt: Int = frameRateDeltaMillis * 3

  def screenDimensions: Rectangle =
    viewport.giveDimensions(magnification)

  val asString: String =
    s"""
       |Viewpoint:      [${viewport.width}, ${viewport.height}]
       |FPS:            $frameRate
       |frameRateDelta: $frameRateDeltaMillis (view updates stop at: $haltViewUpdatesAt, model at: $haltModelUpdatesAt
       |Clear color:    {red: ${clearColor.r}, green: ${clearColor.g}, blue: ${clearColor.b}, alpha: ${clearColor.a}}
       |Magnification:  $magnification
       |""".stripMargin

  def withViewport(width: Int, height: Int): GameConfig   = this.copy(viewport = GameViewport(width, height))
  def withViewport(newViewport: GameViewport): GameConfig = this.copy(viewport = newViewport)
  def withFrameRate(frameRate: Int): GameConfig           = this.copy(frameRate = frameRate)
  def withClearColor(clearColor: ClearColor): GameConfig  = this.copy(clearColor = clearColor)
  def withMagnification(magnification: Int): GameConfig   = this.copy(magnification = magnification)

  def withAdvancedSettings(advanced: AdvancedGameConfig): GameConfig = this.copy(advanced = advanced)
  def metricsEnabled: GameConfig                                     = this.copy(advanced = advanced.copy(recordMetrics = true))
  def metricsDisabled: GameConfig                                    = this.copy(advanced = advanced.copy(recordMetrics = false))
  def withLogInterval(milliseconds: Int): GameConfig =
    this.copy(advanced = advanced.copy(logMetricsReportIntervalMs = milliseconds))
  def disableSkipModelUpdates: GameConfig = this.copy(advanced = advanced.copy(disableSkipModelUpdates = true))
  def enableSkipModelUpdates: GameConfig  = this.copy(advanced = advanced.copy(disableSkipModelUpdates = false))
  def disableSkipViewUpdates: GameConfig  = this.copy(advanced = advanced.copy(disableSkipViewUpdates = true))
  def enableSkipViewUpdates: GameConfig   = this.copy(advanced = advanced.copy(disableSkipViewUpdates = false))
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("GameConfigOps")
object GameConfig {

  @JSExport
  val default: GameConfig =
    GameConfig(GameViewport(550, 400), 60, ClearColor.Black, 1, AdvancedGameConfig.default)

  def apply(width: Int, height: Int, frameRate: Int): GameConfig =
    GameConfig(GameViewport(width, height), frameRate, ClearColor.Black, 1, AdvancedGameConfig.default)

  def apply(viewport: GameViewport, frameRate: Int, clearColor: ClearColor, magnification: Int): GameConfig =
    GameConfig(viewport, frameRate, clearColor, magnification, AdvancedGameConfig.default)

}
