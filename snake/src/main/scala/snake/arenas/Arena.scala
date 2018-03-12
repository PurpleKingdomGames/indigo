package snake.arenas

import com.purplekingdomgames.indigoat.grid.{GridPoint, GridSize}
import snake.arenas.MapElement.{Apple, Player1Start, Wall}

object Arena {

  def genLevel(gridSize: GridSize): GameMap =
    GameMap(gridSize)
      .insertElement(Player1Start(gridSize.centre))
      .insertElement(Apple(gridSize.centre + ((3, 2))))
      .insertElements(topEdgeWall(gridSize))
      .insertElements(rightEdgeWall(gridSize))
      .insertElements(bottomEdgeWall(gridSize))
      .insertElements(leftEdgeWall(gridSize))
//      .removeElement(GridPoint(gridSize.width / 2, 0))
//      .removeElement(GridPoint(gridSize.width / 2, gridSize.height - 1))
//      .removeElement(GridPoint(0, gridSize.height / 2))
//      .removeElement(GridPoint(gridSize.width - 1, gridSize.height / 2))
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
