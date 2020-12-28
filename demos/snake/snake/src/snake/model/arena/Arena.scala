package snake.model.arena

import snake.model.arena.MapElement.{Apple, Wall}
import indigoextras.geometry.Vertex
import scala.annotation.tailrec
import indigoextras.geometry.BoundingBox

object Arena {

  def genLevel(gridSize: BoundingBox): GameMap =
    GameMap(gridSize)
      .insertElement(Apple(gridSize.center + Vertex(3, 2)))
      .insertElements(topEdgeWall(gridSize))
      .insertElements(rightEdgeWall(gridSize))
      .insertElements(bottomEdgeWall(gridSize))
      .insertElements(leftEdgeWall(gridSize))
      .optimise

  private def topEdgeWall(gridSize: BoundingBox): List[Wall] =
    fillIncrementally(gridSize.topLeft, gridSize.topRight).map(Wall.apply)

  private def rightEdgeWall(gridSize: BoundingBox): List[Wall] =
    fillIncrementally(gridSize.topRight, gridSize.bottomRight).map(Wall.apply)

  private def bottomEdgeWall(gridSize: BoundingBox): List[Wall] =
    fillIncrementally(gridSize.bottomLeft, gridSize.bottomRight).map(Wall.apply)

  private def leftEdgeWall(gridSize: BoundingBox): List[Wall] =
    fillIncrementally(gridSize.topLeft, gridSize.bottomLeft).map(Wall.apply)

  private def fillIncrementally(start: Vertex, end: Vertex): List[Vertex] = {
    @tailrec
    def rec(last: Vertex, dest: Vertex, p: Vertex => Boolean, acc: List[Vertex]): List[Vertex] =
      if (p(last)) acc
      else {
        val nextX: Double = if (last.x + 1 <= end.x) last.x + 1 else last.x
        val nextY: Double = if (last.y + 1 <= end.y) last.y + 1 else last.y
        val next: Vertex  = Vertex(nextX, nextY)
        rec(next, dest, p, acc :+ next)
      }

    if (lessThanOrEqual(start, end)) rec(start, end, (gp: Vertex) => gp == end, List(start))
    else rec(end, start, (gp: Vertex) => gp == start, List(end))
  }

  private def lessThanOrEqual(a: Vertex, b: Vertex): Boolean =
    a.x <= b.x && a.y <= b.y

}
