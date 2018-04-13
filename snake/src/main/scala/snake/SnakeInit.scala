package snake

import com.purplekingdomgames.indigo.gameengine.assets.AssetCollection
import com.purplekingdomgames.indigo.gameengine.scenegraph.{Graphic, Group}
import com.purplekingdomgames.indigo.gameengine.{Startup, StartupSuccess, ToReportable}
import com.purplekingdomgames.indigoexts.grid.GridSize
import com.purplekingdomgames.indigoexts.ui.ButtonAssets
import com.purplekingdomgames.shared.GameViewport

object SnakeInit {

  def initialise(viewport: GameViewport, gridSize: GridSize): AssetCollection => Startup[ErrorReport, SnakeStartupData] = assetCollection => {

    val blockSize: Int = 30

//    val background = for {
//      json <- assetCollection.texts.find(p => p.name == SnakeAssets.snakeTiledMapData).map(_.contents)
//      tiledMap <- TiledHelper.fromJson(json)
//      sceneGraphNodeBranch <- Option(TiledHelper.toSceneGraphNodeBranch(tiledMap, Depth(3), SnakeAssets.snakeTexture, 8))
//    } yield sceneGraphNodeBranch
//
//    background match {
//      case None =>
//        ErrorReport("Failed to load the Tiled background map")
//
//      case Some(bg) =>
        StartupSuccess(
          SnakeStartupData(
            viewport = viewport,
            gridSize = gridSize,
            staticAssets = StaticAssets(
              gameScreen = GameScreenAssets(
                apple = Graphic(0, 0, blockSize, blockSize, 2, SnakeAssets.snakeTexture).withCrop(blockSize, 0, blockSize, blockSize),
                player1 = PlayerSnakeAssets(
                  alive = Graphic(0, 0, blockSize, blockSize, 2, SnakeAssets.snakeTexture).withCrop(blockSize * 2, 0, blockSize, blockSize),
                  dead = Graphic(0, 0, blockSize, blockSize, 2, SnakeAssets.snakeTexture).withCrop(blockSize * 3, 0, blockSize, blockSize)
                ),
                player2 = PlayerSnakeAssets(
                  alive = Graphic(0, 0, blockSize, blockSize, 2, SnakeAssets.snakeTexture).withCrop(blockSize * 2, blockSize, blockSize, blockSize),
                  dead = Graphic(0, 0, blockSize, blockSize, 2, SnakeAssets.snakeTexture).withCrop(blockSize * 3, blockSize, blockSize, blockSize)
                ),
                player3 = PlayerSnakeAssets(
                  alive = Graphic(0, 0, blockSize, blockSize, 2, SnakeAssets.snakeTexture).withCrop(blockSize * 2, blockSize * 2, blockSize, blockSize),
                  dead = Graphic(0, 0, blockSize, blockSize, 2, SnakeAssets.snakeTexture).withCrop(blockSize * 3, blockSize * 2, blockSize, blockSize)
                ),
                player4 = PlayerSnakeAssets(
                  alive = Graphic(0, 0, blockSize, blockSize, 2, SnakeAssets.snakeTexture).withCrop(blockSize * 2, blockSize * 3, blockSize, blockSize),
                  dead = Graphic(0, 0, blockSize, blockSize, 2, SnakeAssets.snakeTexture).withCrop(blockSize * 3, blockSize * 3, blockSize, blockSize)
                ),
                background = Group(
                  Graphic(0, 0, 960, 540, 3, SnakeAssets.arenaBg)
                )
              )
            )
          )
        )
//    }

  }

}

case class SnakeStartupData(viewport: GameViewport, gridSize: GridSize, staticAssets: StaticAssets)

case class StaticAssets(gameScreen: GameScreenAssets)

case class UIAssets(leftAndRight: ButtonAssets)

case class GameScreenAssets(apple: Graphic,
                            player1: PlayerSnakeAssets,
                            player2: PlayerSnakeAssets,
                            player3: PlayerSnakeAssets,
                            player4: PlayerSnakeAssets,
                            background: Group)

case class PlayerSnakeAssets(alive: Graphic, dead: Graphic)

case class ErrorReport(errors: List[String])

object ErrorReport {

  implicit val toErrorReport: ToReportable[ErrorReport] =
    ToReportable.createToReportable(r => r.errors.mkString("\n"))

  def apply(message: String*): ErrorReport = ErrorReport(message.toList)

}
