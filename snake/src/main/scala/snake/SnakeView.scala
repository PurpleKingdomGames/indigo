package snake

import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.Point

object SnakeView {

  def coordsToGridPoint(x: Int, y: Int, gridSize: GridSize): Point =
    Point(x * gridSize.gridSquareSize, ((gridSize.rows - 1) - y) * gridSize.gridSquareSize)

  def updateView(model: SnakeModel): SceneGraphUpdate[SnakeEvent] =
    SceneGraphUpdate(
      SceneGraphRootNode(
        game = gameLayer(model, if(model.running) model.staticAssets.snakeAlive else model.staticAssets.snakeDead),
        lighting = SceneGraphLightingLayer.empty,
        ui = SceneGraphUiLayer.empty
      ),
      Nil
    )

  def gameLayer(currentState: SnakeModel, snakeAsset: Graphic[SnakeEvent]): SceneGraphGameLayer[SnakeEvent] =
    SceneGraphGameLayer()
//      .addChild(currentState.staticAssets.outerWalls)
//      .addChild(currentState.staticAssets.apple.moveTo(coordsToGridPoint(currentState.apple.x, currentState.apple.y, currentState.gridSize)))
      .addChildren(currentState.player1.snake.givePath.map(pt => snakeAsset.moveTo(coordsToGridPoint(pt.x, pt.y, currentState.gameMap.gridSize))))

}
