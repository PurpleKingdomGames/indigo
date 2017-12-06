package snake

import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.Point

import scala.language.implicitConversions

object SnakeView {

  implicit def snakePointToPoint(snakePoint: SnakePoint): Point =
    Point(snakePoint.x, snakePoint.y)

  def updateView(model: SnakeModel): SceneGraphUpdate[SnakeEvent] =
    SceneGraphUpdate(
      SceneGraphRootNode(
        game = gameLayer(model),
        lighting = SceneGraphLightingLayer.empty,
        ui = SceneGraphUiLayer.empty
      ),
      Nil
    )

  def gameLayer(currentState: SnakeModel): SceneGraphGameLayer[SnakeEvent] =
    SceneGraphGameLayer(
      currentState.staticAssets.outerWalls,
      currentState.staticAssets.apple.moveTo(32, 32),
      currentState.staticAssets.snakeHead.moveTo(currentState.snake.start)
    )

}
