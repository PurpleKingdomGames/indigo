package snake.model.arena

import indigo.Dice
import snake.model.arena.MapElement.{Apple, Wall}
import indigoextras.geometry.BoundingBox
import indigoextras.trees.QuadTree
import indigoextras.geometry.Vertex
import scala.annotation.tailrec

case class GameMap(quadTree: QuadTree[MapElement], gridSize: BoundingBox) {

  def isEmpty: Boolean = quadTree.isEmpty

  def fetchElementAt(x: Int, y: Int): Option[MapElement] =
    quadTree.fetchElementAt(Vertex(x.toDouble, y.toDouble))

  def fetchElementAt(gridPoint: Vertex): Option[MapElement] =
    quadTree.fetchElementAt(gridPoint)

  def insertElement(element: MapElement): GameMap =
    this.copy(quadTree = quadTree.insertElement(element, element.gridPoint))

  def insertElements(elements: List[MapElement]): GameMap =
    elements.foldLeft(this)((map, elem) => map.insertElement(elem))

  def removeElement(gridPoint: Vertex): GameMap =
    this.copy(quadTree = quadTree.removeElement(gridPoint))

  def findEmptySpace(dice: Dice, not: List[Vertex]): Vertex =
    findEmptySpace(quadTree, dice, gridSize, not)

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

  def randomVertex(dice: Dice, maxX: Int, maxY: Int): Vertex =
    Vertex(
      x = dice.rollFromZero(maxX).toDouble,
      y = dice.rollFromZero(maxY).toDouble
    )

  def findEmptySpace[T](quadTree: QuadTree[T], dice: Dice, gridSize: BoundingBox, not: List[Vertex]): Vertex = {
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

}

object GameMap {

  def apply(gridSize: BoundingBox): GameMap =
    GameMap(QuadTree.empty[MapElement](gridSize.size), gridSize)

}
