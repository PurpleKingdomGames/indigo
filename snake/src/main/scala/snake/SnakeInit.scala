package snake

import com.purplekingdomgames.indigo.gameengine.assets.AssetCollection
import com.purplekingdomgames.indigo.gameengine.scenegraph.Graphic
import com.purplekingdomgames.indigo.gameengine.{Startup, StartupSuccess, ToReportable}
import com.purplekingdomgames.indigoat.grid.GridSize

object SnakeInit {

  def initialise(gridSize: GridSize): AssetCollection => Startup[ErrorReport, SnakeStartupData] = _ =>
    StartupSuccess(
      SnakeStartupData(
        gridSize = gridSize,
        staticAssets = StaticAssets(
          ui = UIAssets(
            leftAndRight = ButtonAssets(
              up = Graphic(0, 0, 16, 16, 1, SnakeAssets.snakeTexture).withCrop(208, 0, 48, 32),
              over = Graphic(0, 0, 16, 16, 1, SnakeAssets.snakeTexture).withCrop(208, 32, 48, 32),
              down = Graphic(0, 0, 16, 16, 1, SnakeAssets.snakeTexture).withCrop(208, 64, 48, 32)
            )
          ),
          gameScreen = GameScreenAssets(
            wall = Graphic(0, 0, 16, 16, 1, SnakeAssets.snakeTexture),
            apple = Graphic(0, 0, 16, 16, 1, SnakeAssets.snakeTexture).withCrop(16, 0, 16, 16),
            player1 = PlayerSnakeAssets(
              alive = Graphic(0, 0, 16, 16, 1, SnakeAssets.snakeTexture).withCrop(32, 0, 16, 16),
              dead = Graphic(0, 0, 16, 16, 1, SnakeAssets.snakeTexture).withCrop(48, 0, 16, 16)
            ),
            player2 = PlayerSnakeAssets(
              alive = Graphic(0, 0, 16, 16, 1, SnakeAssets.snakeTexture).withCrop(32, 16, 16, 16),
              dead = Graphic(0, 0, 16, 16, 1, SnakeAssets.snakeTexture).withCrop(48, 16, 16, 16)
            ),
            player3 = PlayerSnakeAssets(
              alive = Graphic(0, 0, 16, 16, 1, SnakeAssets.snakeTexture).withCrop(32, 32, 16, 16),
              dead = Graphic(0, 0, 16, 16, 1, SnakeAssets.snakeTexture).withCrop(48, 32, 16, 16)
            ),
            player4 = PlayerSnakeAssets(
              alive = Graphic(0, 0, 16, 16, 1, SnakeAssets.snakeTexture).withCrop(32, 48, 16, 16),
              dead = Graphic(0, 0, 16, 16, 1, SnakeAssets.snakeTexture).withCrop(48, 48, 16, 16)
            )
          )
        )
      )
    )

}

case class SnakeStartupData(gridSize: GridSize, staticAssets: StaticAssets)

case class StaticAssets(ui: UIAssets, gameScreen: GameScreenAssets)

case class UIAssets(leftAndRight: ButtonAssets)

case class ButtonAssets(up: Graphic[SnakeEvent], over: Graphic[SnakeEvent], down: Graphic[SnakeEvent])

case class GameScreenAssets(wall: Graphic[SnakeEvent],
                            apple: Graphic[SnakeEvent],
                            player1: PlayerSnakeAssets,
                            player2: PlayerSnakeAssets,
                            player3: PlayerSnakeAssets,
                            player4: PlayerSnakeAssets)

case class PlayerSnakeAssets(alive: Graphic[SnakeEvent], dead: Graphic[SnakeEvent])

case class ErrorReport(errors: List[String])

object ErrorReport {

  implicit val toErrorReport: ToReportable[ErrorReport] =
    ToReportable.createToReportable(r => r.errors.mkString("\n"))

  def apply(message: String*): ErrorReport = ErrorReport(message.toList)

}
