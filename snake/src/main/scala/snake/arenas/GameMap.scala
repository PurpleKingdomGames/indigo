package snake.arenas

import com.purplekingdomgames.indigoexts.grid.{GridPoint, GridSize}
import com.purplekingdomgames.indigoexts.quadtree.QuadTree
import snake.arenas.MapElement.{Apple, Wall}

/***
GameMap is a sparse tree of MapElements organised as a QuadTree where each
tree leaf may only contain a single element.
  */
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

  def findEmptySpace(not: List[GridPoint]): GridPoint =
    quadTree.findEmptySpace(gridSize, not)

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

  def renderAsString: String =
    s"""GameMap:
       |${quadTree.renderAsString}
     """.stripMargin

}

object GameMap {

  def apply(gridSize: GridSize): GameMap =
    GameMap(QuadTree.empty[MapElement](gridSize), gridSize)

}
