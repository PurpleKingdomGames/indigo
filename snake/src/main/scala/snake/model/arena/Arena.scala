package snake.model.arena

import indigoexts.grids._
import snake.model.arena.MapElement.{Apple, Wall}

object Arena {

  def genLevel(gridSize: GridSize): GameMap =
    GameMap(gridSize)
      .insertElement(Apple(gridSize.centre + GridPoint(3, 2)))
      .insertElements(topEdgeWall(gridSize))
      .insertElements(rightEdgeWall(gridSize))
      .insertElements(bottomEdgeWall(gridSize))
      .insertElements(leftEdgeWall(gridSize))
      .optimise

  private def topEdgeWall(gridSize: GridSize): List[Wall] =
    GridPoint.fillIncrementally(gridSize.topLeft, gridSize.topRight).map(Wall.apply)

  private def rightEdgeWall(gridSize: GridSize): List[Wall] =
    GridPoint.fillIncrementally(gridSize.topRight, gridSize.bottomRight).map(Wall.apply)

  private def bottomEdgeWall(gridSize: GridSize): List[Wall] =
    GridPoint.fillIncrementally(gridSize.bottomLeft, gridSize.bottomRight).map(Wall.apply)

  private def leftEdgeWall(gridSize: GridSize): List[Wall] =
    GridPoint.fillIncrementally(gridSize.topLeft, gridSize.bottomLeft).map(Wall.apply)

}
