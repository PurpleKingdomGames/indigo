package snake.model.arena

import indigo.Dice
import indigoexts.grid._
import indigoexts.quadtree._
import snake.model.arena.MapElement.{Apple, Wall}

case class GameMap(quadTree: QuadTree[MapElement], gridSize: GridSize) {

  def isEmpty: Boolean = quadTree.isEmpty

  def fetchElementAt(x: Int, y: Int): Option[MapElement] =
    quadTree.fetchElementAt(GridPoint(x, y))

  def fetchElementAt(gridPoint: GridPoint): Option[MapElement] =
    quadTree.fetchElementAt(gridPoint)

  def insertElement(element: MapElement): GameMap =
    this.copy(quadTree = quadTree.insertElement(element, element.gridPoint))

  def insertElements(elements: List[MapElement]): GameMap =
    elements.foldLeft(this)((map, elem) => map.insertElement(elem))

  def removeElement(gridPoint: GridPoint): GameMap =
    this.copy(quadTree = quadTree.removeElement(gridPoint))

  def findEmptySpace(dice: Dice, not: List[GridPoint]): GridPoint =
    quadTree.findEmptySpace(dice, gridSize, not)

  def asElementList: List[MapElement] =
    quadTree.asElementList

  def findWalls: List[Wall] =
    asElementList.flatMap {
      case w: Wall =>
        List(w)

      case _ =>
        Nil
    }

  def findApples: List[Apple] =
    asElementList.flatMap {
      case a: Apple =>
        List(a)

      case _ =>
        Nil
    }

  def optimise: GameMap =
    this.copy(quadTree = quadTree.prune)

}

object GameMap {

  def apply(gridSize: GridSize): GameMap =
    GameMap(QuadTree.empty[MapElement](gridSize), gridSize)

}
