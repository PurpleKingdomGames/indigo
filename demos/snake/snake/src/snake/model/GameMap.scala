package snake.model

import indigo.Dice
import indigoextras.geometry.BoundingBox
import indigoextras.trees.QuadTree
import indigoextras.geometry.Vertex
import scala.annotation.tailrec

final case class GameMap(quadTree: QuadTree[MapElement], gridSize: BoundingBox) {

  def fetchElementAt(gridPoint: Vertex): Option[MapElement] =
    quadTree.fetchElementAt(gridPoint)

  def insertApple(element: MapElement.Apple): GameMap =
    this.copy(quadTree = quadTree.insertElement(element, element.gridPoint).prune)

  def removeApple(gridPoint: Vertex): GameMap =
    this.copy(quadTree = quadTree.removeElement(gridPoint).prune)

  def insertElements(elements: List[MapElement]): GameMap =
    this.copy(
      quadTree = quadTree.insertElements(elements.map(me => (me, me.gridPoint))).prune
    )

  def findEmptySpace(dice: Dice, not: List[Vertex]): Vertex =
    GameMap.findEmptySpace(quadTree, dice, gridSize, not)

  def asElementList: List[MapElement] =
    quadTree.asElementList

  lazy val findWalls: List[MapElement.Wall] =
    asElementList.flatMap {
      case w: MapElement.Wall =>
        List(w)

      case _ =>
        Nil
    }

  def findApples: List[MapElement.Apple] =
    asElementList.flatMap {
      case a: MapElement.Apple =>
        List(a)

      case _ =>
        Nil
    }

}

object GameMap {

  def apply(gridSize: BoundingBox): GameMap =
    GameMap(QuadTree.empty[MapElement](gridSize.size), gridSize)

  def findEmptySpace[T](quadTree: QuadTree[T], dice: Dice, gridSize: BoundingBox, not: List[Vertex]): Vertex = {

    def randomVertex(dice: Dice, maxX: Int, maxY: Int): Vertex =
      Vertex(
        x = dice.rollFromZero(maxX).toDouble,
        y = dice.rollFromZero(maxY).toDouble
      )

    def makeRandom: () => Vertex =
      () => randomVertex(dice, (gridSize.width - 2).toInt, (gridSize.height - 2).toInt) + Vertex(1, 1)

    @tailrec
    def rec(pt: Vertex): Vertex =
      quadTree.fetchElementAt(pt) match {
        case None if !not.contains(pt) =>
          pt

        case None =>
          rec(makeRandom())

        case Some(_) =>
          rec(makeRandom())
      }

    rec(makeRandom())
  }

  def genLevel(gridSize: BoundingBox): GameMap = {
    val adjustedGridSize = gridSize.resize(gridSize.size - Vertex(1, 1))

    GameMap(gridSize)
      .insertApple(MapElement.Apple(adjustedGridSize.center.round + Vertex(3, 2)))
      .insertElements(topEdgeWall(adjustedGridSize))
      .insertElements(rightEdgeWall(adjustedGridSize))
      .insertElements(bottomEdgeWall(adjustedGridSize))
      .insertElements(leftEdgeWall(adjustedGridSize))
  }

  private def topEdgeWall(gridSize: BoundingBox): List[MapElement.Wall] =
    fillIncrementally(gridSize.topLeft, gridSize.topRight).map(MapElement.Wall.apply)

  private def rightEdgeWall(gridSize: BoundingBox): List[MapElement.Wall] =
    fillIncrementally(gridSize.topRight, gridSize.bottomRight).map(MapElement.Wall.apply)

  private def bottomEdgeWall(gridSize: BoundingBox): List[MapElement.Wall] =
    fillIncrementally(gridSize.bottomLeft, gridSize.bottomRight).map(MapElement.Wall.apply)

  private def leftEdgeWall(gridSize: BoundingBox): List[MapElement.Wall] =
    fillIncrementally(gridSize.topLeft, gridSize.bottomLeft).map(MapElement.Wall.apply)

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

sealed trait MapElement {
  val gridPoint: Vertex
}

object MapElement {
  final case class Wall(gridPoint: Vertex)  extends MapElement
  final case class Apple(gridPoint: Vertex) extends MapElement
}
