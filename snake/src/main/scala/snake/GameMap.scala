package snake

import snake.QuadTree.QuadLeaf

case class GameMap(quadTree: QuadTree, gridSize: GridSize)

sealed trait QuadTree {

  val bounds: QuadBounds

  def +(other: QuadTree): QuadLeaf =
    QuadTree.append(this, other)

  def fetchElementAt(gridPoint: GridPoint): Option[MapElement] =
    QuadTree.fetchElementAt(this, gridPoint)

  def insertElement(element: MapElement): QuadTree =
    QuadTree.insertElementAt(this, element)

  def flatten: QuadLeaf =
    QuadTree.join(this)

}
object QuadTree {

  implicit class ListSequence[A](l: List[Option[A]]) {

    def sequence: Option[List[A]] =
      Option(
        l.flatMap {
          case None =>
            Nil

          case Some(a) =>
            List(a)
        }
      )

  }

  case class QuadBranch(bounds: QuadBounds, a: QuadTree, b: QuadTree, c: QuadTree, d: QuadTree) extends QuadTree
  case class QuadLeaf(bounds: QuadBounds, values: List[MapElement]) extends QuadTree

  def identity: QuadTree =
    QuadLeaf(QuadBounds.identity, Nil)

  def join(quadTree: QuadTree): QuadLeaf =
    quadTree match {
      case l @ QuadLeaf(_, _) =>
        l

      case QuadBranch(_, a, b, c, d) =>
        join(a) + join(b) + join(c) + join(d)
    }

  def append(a: QuadTree, b: QuadTree): QuadLeaf =
    (a, b) match {
      case (QuadLeaf(b1, vs1), QuadLeaf(b2, vs2)) =>
        QuadLeaf(b1 + b2, vs1 ++ vs2)

      case (x @ QuadLeaf(_, _), y @ QuadBranch(_, _, _, _, _)) =>
        x + join(y)

      case (x @ QuadBranch(_, _, _ , _, _), y @ QuadLeaf(_, _)) =>
        join(x) + y

      case (x @ QuadBranch(_, _, _ , _, _), y @ QuadBranch(_, _, _ , _, _)) =>
        join(x) + join(y)
    }

  def fetchElementAt(quadTree: QuadTree, gridPoint: GridPoint): Option[MapElement] =
    if(quadTree.bounds.isPointWithinBounds(gridPoint)) {
      quadTree match {
        case QuadLeaf(_, values) =>
          values.find(p => p.gridPoint === gridPoint)

        case QuadBranch(_, a, b, c, d) =>
          List(
            a.fetchElementAt(gridPoint),
            b.fetchElementAt(gridPoint),
            c.fetchElementAt(gridPoint),
            d.fetchElementAt(gridPoint)
          ).sequence
            .map(l => l.foldLeft(MapElement.identity)(_ + _))
      }
    } else None

  def insertElementAt(quadTree: QuadTree, element: MapElement): QuadTree = {
    quadTree match {
      case l @ QuadLeaf(_, _) if l.bounds.isPointWithinBounds(element.gridPoint) =>
        l.copy(values = element :: l.values)

      case l @ QuadLeaf(_, _) =>
        l

      case QuadBranch(bounds, a, b, c, d) =>
        QuadBranch(bounds, a.insertElement(element), b.insertElement(element), c.insertElement(element), d.insertElement(element))
    }
  }

}

case class QuadBounds(x: Int, y: Int, width: Int, height: Int) {

  def +(other: QuadBounds): QuadBounds =
    QuadBounds.append(this, other)

  def isPointWithinBounds(gridPoint: GridPoint): Boolean =
    QuadBounds.pointWithinBounds(this, gridPoint)

}
object QuadBounds {

  def identity: QuadBounds =
    QuadBounds(0, 0, 0, 0)

  def append(a: QuadBounds, b: QuadBounds): QuadBounds =
    QuadBounds(
      if(a.x < b.x) a.x else b.x,
      if(a.y < b.y) a.y else b.y,
      if(a.width > b.width) a.width else b.width,
      if(a.height > b.height) a.height else b.height
    )

  def pointWithinBounds(quadBounds: QuadBounds, gridPoint: GridPoint): Boolean =
    gridPoint.x >= quadBounds.x &&
      gridPoint.y >= quadBounds.y &&
      gridPoint.x <= quadBounds.x + quadBounds.width &&
      gridPoint.y <= quadBounds.y + quadBounds.height

}

case class GridPoint(x: Int, y: Int) {

  def ===(other: GridPoint): Boolean =
    GridPoint.equality(this, other)

  def +(other: GridPoint): GridPoint =
    GridPoint.append(this, other)

}
object GridPoint {

  def apply: GridPoint =
    identity

  def identity: GridPoint =
    GridPoint(0, 0)

  def equality(a: GridPoint, b: GridPoint): Boolean =
    a.x == b.x && a.y == b.y

  def append(a: GridPoint, b: GridPoint): GridPoint =
    GridPoint(a.x + b.x, a.y + b.y)

}

sealed trait MapElement {
  val gridPoint: GridPoint

  def +(other: MapElement): MapElement =
    MapElement.append(this, other)

}
object MapElement {
  case class Empty(gridPoint: GridPoint) extends MapElement

  case class Wall(gridPoint: GridPoint) extends MapElement

  case class Apple(gridPoint: GridPoint) extends MapElement

  def identity: MapElement =
    Empty(GridPoint.identity)

  def append(a: MapElement, b: MapElement): MapElement =
    (a, b) match {
      case (Empty(_), Empty(_)) =>
        a

      case (_, Empty(_)) =>
        a

      case (Empty(_), _) =>
        b

      case _ =>
        a
    }

}