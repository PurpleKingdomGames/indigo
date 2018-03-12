package snake

import com.purplekingdomgames.indigo.gameengine.assets.AssetCollection
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.Depth
import com.purplekingdomgames.indigo.gameengine.scenegraph.{Graphic, SceneGraphNodeBranch, TiledHelper}
import com.purplekingdomgames.indigo.gameengine.{Startup, StartupSuccess, ToReportable}
import com.purplekingdomgames.indigoat.grid.GridSize
import com.purplekingdomgames.indigoat.ui.ButtonAssets
import com.purplekingdomgames.shared.GameViewport

object SnakeInit {

  def initialise(viewport: GameViewport, gridSize: GridSize): AssetCollection => Startup[ErrorReport, SnakeStartupData] = assetCollection => {

    val background = for {
      json <- assetCollection.texts.find(p => p.name == SnakeAssets.snakeTiledMapData).map(_.contents)
      tiledMap <- TiledHelper.fromJson(json)
      sceneGraphNodeBranch <- Option(TiledHelper.toSceneGraphNodeBranch(tiledMap, Depth(3), SnakeAssets.snakeTexture, 8))
    } yield sceneGraphNodeBranch

    background match {
      case None =>
        ErrorReport("Failed to load the Tiled background map")

      case Some(bg) =>
        StartupSuccess(
          SnakeStartupData(
            viewport = viewport,
            gridSize = gridSize,
            staticAssets = StaticAssets(
              ui = UIAssets(
                leftAndRight = ButtonAssets(
                  up = Graphic(0, 0, 32, 32, 2, SnakeAssets.snakeTexture).withCrop(208, 0, 48, 32),
                  over = Graphic(0, 0, 32, 32, 2, SnakeAssets.snakeTexture).withCrop(208, 32, 48, 32),
                  down = Graphic(0, 0, 32, 32, 2, SnakeAssets.snakeTexture).withCrop(208, 64, 48, 32)
                )
              ),
              gameScreen = GameScreenAssets(
                apple = Graphic(0, 0, 32, 32, 2, SnakeAssets.snakeTexture).withCrop(32, 0, 32, 32),
                player1 = PlayerSnakeAssets(
                  alive = Graphic(0, 0, 32, 32, 2, SnakeAssets.snakeTexture).withCrop(64, 0, 32, 32),
                  dead = Graphic(0, 0, 32, 32, 2, SnakeAssets.snakeTexture).withCrop(96, 0, 32, 32)
                ),
                player2 = PlayerSnakeAssets(
                  alive = Graphic(0, 0, 32, 32, 2, SnakeAssets.snakeTexture).withCrop(64, 32, 32, 32),
                  dead = Graphic(0, 0, 32, 32, 2, SnakeAssets.snakeTexture).withCrop(96, 32, 32, 32)
                ),
                player3 = PlayerSnakeAssets(
                  alive = Graphic(0, 0, 32, 32, 2, SnakeAssets.snakeTexture).withCrop(64, 64, 32, 32),
                  dead = Graphic(0, 0, 32, 32, 2, SnakeAssets.snakeTexture).withCrop(96, 64, 32, 32)
                ),
                player4 = PlayerSnakeAssets(
                  alive = Graphic(0, 0, 32, 32, 2, SnakeAssets.snakeTexture).withCrop(64, 96, 32, 32),
                  dead = Graphic(0, 0, 32, 32, 2, SnakeAssets.snakeTexture).withCrop(96, 96, 32, 32)
                ),
                background = bg
              )
            )
          )
        )
    }

  }

}

case class SnakeStartupData(viewport: GameViewport, gridSize: GridSize, staticAssets: StaticAssets)

case class StaticAssets(ui: UIAssets, gameScreen: GameScreenAssets)

case class UIAssets(leftAndRight: ButtonAssets)

case class GameScreenAssets(apple: Graphic,
                            player1: PlayerSnakeAssets,
                            player2: PlayerSnakeAssets,
                            player3: PlayerSnakeAssets,
                            player4: PlayerSnakeAssets,
                            background: SceneGraphNodeBranch)

case class PlayerSnakeAssets(alive: Graphic, dead: Graphic)

case class ErrorReport(errors: List[String])

object ErrorReport {

  implicit val toErrorReport: ToReportable[ErrorReport] =
    ToReportable.createToReportable(r => r.errors.mkString("\n"))

  def apply(message: String*): ErrorReport = ErrorReport(message.toList)

}
