package snake

import com.purplekingdomgames.indigo.Indigo
import com.purplekingdomgames.indigoexts.grid.GridSize
import com.purplekingdomgames.shared.{AdvancedGameConfig, ClearColor, GameConfig, GameViewport}

import scala.scalajs.js.annotation.JSExportTopLevel

object SnakeGame {

  private val gridSize: GridSize = GridSize(
    columns = 32,
    rows = 18,
    gridSquareSize = 30
  )

  private val magnificationLevel: Int = 1
  private val viewportWidth: Int      = (gridSize.gridSquareSize * gridSize.columns) * magnificationLevel
  private val viewportHeight: Int     = (gridSize.gridSquareSize * gridSize.rows) * magnificationLevel

  val config: GameConfig = GameConfig(
    viewport = GameViewport(viewportWidth, viewportHeight),
    frameRate = 30,
    clearColor = ClearColor.Black,
    magnification = magnificationLevel
  ).withAdvancedSettings(
    AdvancedGameConfig(
      recordMetrics = false,
      logMetricsReportIntervalMs = 5000,
      disableSkipModelUpdates = true,
      disableSkipViewUpdates = false
    )
  )

  @JSExportTopLevel("Snake.main")
  def main(args: Array[String]): Unit =
    Indigo.game
      .withConfig(config)
      .withAssets(SnakeAssets.assets)
      .withFonts(Set(SnakeAssets.fontInfo))
      .withAnimations(Set())
      .startUpGameWith(SnakeInit.initialise(config.viewport, gridSize))
      .usingInitialModel(SnakeModel.initialModel)
      .updateModelUsing(SnakeModel.modelUpdate)
      .presentUsing(SnakeView.viewUpdate)
      .start()

}
