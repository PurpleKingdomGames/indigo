package snake

import com.purplekingdomgames.indigo.gameengine.assets.AssetCollection
import com.purplekingdomgames.indigo.gameengine.scenegraph.Graphic
import com.purplekingdomgames.indigo.gameengine.{Startup, StartupSuccess, ToReportable}
import com.purplekingdomgames.indigoat.grid.GridSize
import com.purplekingdomgames.indigoat.ui.ButtonAssets
import com.purplekingdomgames.shared.GameViewport

object SnakeInit {

  def initialise(viewport: GameViewport, gridSize: GridSize): AssetCollection => Startup[ErrorReport, SnakeStartupData] = _ =>
    StartupSuccess(
      SnakeStartupData(
        viewport = viewport,
        gridSize = gridSize,
        staticAssets = StaticAssets(
          ui = UIAssets(
            leftAndRight = ButtonAssets(
              up = Graphic(0, 0, 32, 32, 1, SnakeAssets.snakeTexture).withCrop(208, 0, 48, 32),
              over = Graphic(0, 0, 32, 32, 1, SnakeAssets.snakeTexture).withCrop(208, 32, 48, 32),
              down = Graphic(0, 0, 32, 32, 1, SnakeAssets.snakeTexture).withCrop(208, 64, 48, 32)
            )
          ),
          gameScreen = GameScreenAssets(
            wallTopLeft = Graphic(0, 0, 32, 32, 1, SnakeAssets.snakeTexture).withCrop(128, 0, 32, 32),
            wallTop = Graphic(0, 0, 32, 32, 1, SnakeAssets.snakeTexture).withCrop(160, 0, 32, 32),
            wallTopRight = Graphic(0, 0, 32, 32, 1, SnakeAssets.snakeTexture).withCrop(192, 0, 32, 32),
            wallLeft = Graphic(0, 0, 32, 32, 1, SnakeAssets.snakeTexture).withCrop(128, 32, 32, 32),
            wallRight = Graphic(0, 0, 32, 32, 1, SnakeAssets.snakeTexture).withCrop(192, 32, 32, 32),
            wallBottomLeft = Graphic(0, 0, 32, 32, 1, SnakeAssets.snakeTexture).withCrop(128, 64, 32, 32),
            wallBottom = Graphic(0, 0, 32, 32, 1, SnakeAssets.snakeTexture).withCrop(160, 64, 32, 32),
            wallBottomRight = Graphic(0, 0, 32, 32, 1, SnakeAssets.snakeTexture).withCrop(192, 64, 32, 32),
            grassTopLeft = Graphic(0, 0, 32, 32, 1, SnakeAssets.snakeTexture).withCrop(128, 96, 32, 32),
            grassTop = Graphic(0, 0, 32, 32, 1, SnakeAssets.snakeTexture).withCrop(160, 96, 32, 32),
            grassTopRight = Graphic(0, 0, 32, 32, 1, SnakeAssets.snakeTexture).withCrop(192, 96, 32, 32),
            grassLeft = Graphic(0, 0, 32, 32, 1, SnakeAssets.snakeTexture).withCrop(128, 128, 32, 32),
            grassMiddle = Graphic(0, 0, 32, 32, 1, SnakeAssets.snakeTexture).withCrop(160, 128, 32, 32),
            grassRight = Graphic(0, 0, 32, 32, 1, SnakeAssets.snakeTexture).withCrop(192, 128, 32, 32),
            grassBottomLeft = Graphic(0, 0, 32, 32, 1, SnakeAssets.snakeTexture).withCrop(128, 160, 32, 32),
            grassBottom = Graphic(0, 0, 32, 32, 1, SnakeAssets.snakeTexture).withCrop(160, 160, 32, 32),
            grassBottomRight = Graphic(0, 0, 32, 32, 1, SnakeAssets.snakeTexture).withCrop(192, 160, 32, 32),
            apple = Graphic(0, 0, 32, 32, 1, SnakeAssets.snakeTexture).withCrop(32, 0, 32, 32),
            player1 = PlayerSnakeAssets(
              alive = Graphic(0, 0, 32, 32, 1, SnakeAssets.snakeTexture).withCrop(64, 0, 32, 32),
              dead = Graphic(0, 0, 32, 32, 1, SnakeAssets.snakeTexture).withCrop(96, 0, 32, 32)
            ),
            player2 = PlayerSnakeAssets(
              alive = Graphic(0, 0, 32, 32, 1, SnakeAssets.snakeTexture).withCrop(64, 32, 32, 32),
              dead = Graphic(0, 0, 32, 32, 1, SnakeAssets.snakeTexture).withCrop(96, 32, 32, 32)
            ),
            player3 = PlayerSnakeAssets(
              alive = Graphic(0, 0, 32, 32, 1, SnakeAssets.snakeTexture).withCrop(64, 64, 32, 32),
              dead = Graphic(0, 0, 32, 32, 1, SnakeAssets.snakeTexture).withCrop(96, 64, 32, 32)
            ),
            player4 = PlayerSnakeAssets(
              alive = Graphic(0, 0, 32, 32, 1, SnakeAssets.snakeTexture).withCrop(64, 96, 32, 32),
              dead = Graphic(0, 0, 32, 32, 1, SnakeAssets.snakeTexture).withCrop(96, 96, 32, 32)
            )
          )
        )
      )
    )

}

case class SnakeStartupData(viewport: GameViewport, gridSize: GridSize, staticAssets: StaticAssets)

case class StaticAssets(ui: UIAssets, gameScreen: GameScreenAssets)

case class UIAssets(leftAndRight: ButtonAssets)

case class GameScreenAssets(wallTopLeft: Graphic,
                            wallTop: Graphic,
                            wallTopRight: Graphic,
                            wallLeft: Graphic,
                            wallRight: Graphic,
                            wallBottomLeft: Graphic,
                            wallBottom: Graphic,
                            wallBottomRight: Graphic,
                            grassTopLeft: Graphic,
                            grassTop: Graphic,
                            grassTopRight: Graphic,
                            grassLeft: Graphic,
                            grassMiddle: Graphic,
                            grassRight: Graphic,
                            grassBottomLeft: Graphic,
                            grassBottom: Graphic,
                            grassBottomRight: Graphic,
                            apple: Graphic,
                            player1: PlayerSnakeAssets,
                            player2: PlayerSnakeAssets,
                            player3: PlayerSnakeAssets,
                            player4: PlayerSnakeAssets)

case class PlayerSnakeAssets(alive: Graphic, dead: Graphic)

case class ErrorReport(errors: List[String])

object ErrorReport {

  implicit val toErrorReport: ToReportable[ErrorReport] =
    ToReportable.createToReportable(r => r.errors.mkString("\n"))

  def apply(message: String*): ErrorReport = ErrorReport(message.toList)

}
