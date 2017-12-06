package snake

import com.purplekingdomgames.indigo.gameengine._
import com.purplekingdomgames.indigo.gameengine.assets.AssetCollection
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.{Indigo, _}
import com.purplekingdomgames.shared.{AssetType, ClearColor, GameConfig, GameViewport}

import scala.scalajs.js.annotation.JSExportTopLevel

object SnakeGame {

  private val gridSize: GridSize = GridSize(16, 16)

  private val viewportWidth: Int = 16 * gridSize.x
  private val viewportHeight: Int = 16 * gridSize.y
  private val magnificationLevel: Int = 1

  implicit val config: GameConfig = GameConfig(
    viewport = GameViewport(viewportWidth, viewportHeight),
    frameRate = 10,
    clearColor = ClearColor.fromHexString("7cb941"),
    magnification = magnificationLevel
  )

  implicit val assets: Set[AssetType] = SnakeAssets.assets

  implicit val initialise: AssetCollection => Startup[ErrorReport, SnakeStartupData] = ac =>
    SnakeInit.initialise(gridSize)(ac)

  implicit val initialModel: SnakeStartupData => SnakeModel = startupData =>
    SnakeModel.initialModel(startupData)

  implicit val updateModel: (GameTime, SnakeModel) => GameEvent => SnakeModel = (_, gameModel) =>
    SnakeModel.updateModel(gameModel)

  implicit val updateView: (GameTime, SnakeModel, FrameInputEvents) => SceneGraphUpdate[SnakeEvent] = (_, gameModel, _) =>
    SnakeView.updateView(gameModel)

  @JSExportTopLevel("Snake.main")
  def main(args: Array[String]): Unit =
    Indigo.start[SnakeStartupData, ErrorReport, SnakeModel, SnakeEvent]

}

case class SnakeEvent()