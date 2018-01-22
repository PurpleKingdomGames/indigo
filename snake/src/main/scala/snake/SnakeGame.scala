package snake

import com.purplekingdomgames.indigo.gameengine._
import com.purplekingdomgames.indigo.gameengine.assets.AssetCollection
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.{Indigo, _}
import com.purplekingdomgames.shared.{AssetType, ClearColor, GameConfig, GameViewport}

import scala.scalajs.js.annotation.JSExportTopLevel

object SnakeGame {

  private val gridSize: GridSize = GridSize(32, 50, 16)

  private val viewportWidth: Int = gridSize.gridSquareSize * gridSize.columns
  private val viewportHeight: Int = gridSize.gridSquareSize * gridSize.rows
  private val magnificationLevel: Int = 1

  implicit val config: GameConfig = GameConfig(
    viewport = GameViewport(viewportWidth, viewportHeight),
    frameRate = 30,
    clearColor = ClearColor.fromHexString("7cb941"),
    magnification = magnificationLevel
  )

  implicit val assets: Set[AssetType] = SnakeAssets.assets

  implicit val initialise: AssetCollection => Startup[ErrorReport, SnakeStartupData] = ac =>
    SnakeInit.initialise(gridSize)(ac)

  implicit val initialModel: SnakeStartupData => SnakeModel = startupData =>
    SnakeModel.initialModel(startupData)

  implicit val updateModel: (GameTime, SnakeModel) => GameEvent => SnakeModel = (gameTime, gameModel) =>
    if(gameModel.running) SnakeModel.updateModel(gameTime, gameModel) else _ => gameModel

  implicit val updateView: (GameTime, SnakeModel, FrameInputEvents) => SceneGraphUpdate[SnakeEvent] = (_, gameModel, _) =>
    SnakeView.updateView(gameModel)

  @JSExportTopLevel("Snake.main")
  def main(args: Array[String]): Unit =
    Indigo.start[SnakeStartupData, ErrorReport, SnakeModel, SnakeEvent]

}

case class SnakeEvent()