package snake

import scala.language.implicitConversions
import scala.util.Random

/***
GameMap is a sparse tree of MapElements organised as a QuadTree where each
tree leaf may only contain a single element.
 */
case class GameMap(quadTree: QuadTree, gridSize: GridSize) {

  def isEmpty: Boolean = quadTree.isEmpty

  def fetchElementAt(gridPoint: GridPoint): Option[MapElement] =
    quadTree.fetchElementAt(gridPoint)

  def insertElement(element: MapElement): GameMap =
    this.copy(quadTree = quadTree.insertElement(element))

  def removeElement(gridPoint: GridPoint): GameMap =
    this.copy(quadTree = quadTree.removeElement(gridPoint))

  def optimise: GameMap =
    this.copy(quadTree = quadTree.prune)

  def renderAsString: String =
    s"""GameMap:
       |${quadTree.renderAsString}
     """.stripMargin

}

object GameMap {

  def apply(gridSize: GridSize): GameMap =
    GameMap(QuadTree.empty(gridSize), gridSize)

}

sealed trait QuadTree {

  val bounds: QuadBounds

  def isEmpty: Boolean

  def fetchElementAt(gridPoint: GridPoint): Option[MapElement] =
    QuadTree.fetchElementAt(this, gridPoint)

  def insertElement(element: MapElement): QuadTree =
    QuadTree.insertElementAt(this, element)

  def removeElement(gridPoint: GridPoint): QuadTree =
    QuadTree.removeElement(this, gridPoint)

  def prune: QuadTree =
    QuadTree.prune(this)

  def renderAsString: String =
    QuadTree.renderAsString(this)

}
object QuadTree {

  def empty(sizeAsPowerOf2: Int): QuadTree =
    QuadEmpty(QuadBounds.apply(sizeAsPowerOf2))

  def empty(gridSize: GridSize): QuadTree =
    QuadEmpty(QuadBounds.apply(gridSize.asPowerOf2))

  //TODO: This needs to be recursive. if a is branch, then do another empty check etc.
  case class QuadBranch(bounds: QuadBounds, a: QuadTree, b: QuadTree, c: QuadTree, d: QuadTree) extends QuadTree {
    def isEmpty: Boolean =
      a.isEmpty && b.isEmpty && c.isEmpty && d.isEmpty
  }
  case class QuadLeaf(bounds: QuadBounds, value: MapElement) extends QuadTree {
    def isEmpty: Boolean = false
  }
  case class QuadEmpty(bounds: QuadBounds) extends QuadTree {
    def isEmpty: Boolean = true
  }

  object QuadBranch {
    def fromBounds(bounds: QuadBounds): QuadBranch =
      fromBoundsAndQuarters(bounds, bounds.subdivide)

    def fromBoundsAndQuarters(bounds: QuadBounds, quarters: (QuadBounds, QuadBounds, QuadBounds, QuadBounds)): QuadBranch ={
      QuadBranch(
        bounds,
        QuadEmpty(quarters._1),
        QuadEmpty(quarters._2),
        QuadEmpty(quarters._3),
        QuadEmpty(quarters._4)
      )
    }
  }

  def fetchElementAt(quadTree: QuadTree, gridPoint: GridPoint): Option[MapElement] =
    quadTree match {
      case QuadEmpty(bounds) if bounds.isPointWithinBounds(gridPoint) =>
        None

      case QuadBranch(bounds, a, b, c, d) if bounds.isPointWithinBounds(gridPoint) =>
        List(
          a.fetchElementAt(gridPoint),
          b.fetchElementAt(gridPoint),
          c.fetchElementAt(gridPoint),
          d.fetchElementAt(gridPoint)
        ).find(p => p.isDefined).flatten

      case QuadLeaf(bounds, value) if bounds.isPointWithinBounds(gridPoint) =>
        Some(value)

      case _ =>
        None
    }

  def insertElementAt(quadTree: QuadTree, element: MapElement): QuadTree =
    quadTree match {
      case l @ QuadLeaf(bounds, _) if bounds.isPointWithinBounds(element.gridPoint) =>
        l.copy(value = element)

      case l: QuadLeaf =>
        l

      case QuadBranch(bounds, a, b, c, d) if bounds.isPointWithinBounds(element.gridPoint) =>
        QuadBranch(
          bounds,
          a.insertElement(element),
          b.insertElement(element),
          c.insertElement(element),
          d.insertElement(element)
        )

      case b: QuadBranch =>
        b

      case QuadEmpty(bounds) if bounds.isPointWithinBounds(element.gridPoint) && bounds.isOneUnitSquare =>
        QuadLeaf(bounds, element)

      case QuadEmpty(bounds) if bounds.isPointWithinBounds(element.gridPoint) =>
        QuadBranch.fromBounds(bounds).insertElement(element)

      case e: QuadEmpty =>
        e
    }

  def removeElement(quadTree: QuadTree, gridPoint: GridPoint): QuadTree =
    quadTree match {
      case QuadLeaf(bounds, _) if bounds.isPointWithinBounds(gridPoint) =>
        QuadEmpty(bounds)

      case QuadBranch(bounds, a, b, c, d) if bounds.isPointWithinBounds(gridPoint) =>
        QuadBranch(
          bounds,
          a.removeElement(gridPoint),
          b.removeElement(gridPoint),
          c.removeElement(gridPoint),
          d.removeElement(gridPoint)
        )

      case tree =>
        tree
    }

  def prune(quadTree: QuadTree): QuadTree =
    quadTree match {
      case l: QuadLeaf =>
        l

      case e: QuadEmpty =>
        e

      case b: QuadBranch if b.isEmpty =>
        QuadEmpty(b.bounds)

      case QuadBranch(bounds, a, b, c, d) =>
        QuadBranch(bounds, a.prune, b.prune, c.prune, d.prune)
    }

  def renderAsString(quadTree: QuadTree): String =
    renderAsStringWithIndent(quadTree, "")

  def renderAsStringWithIndent(quadTree: QuadTree, indent: String): String = {
    quadTree match {
      case QuadEmpty(bounds) =>
        indent + s"Empty [${bounds.renderAsString}]"

      case QuadLeaf(bounds, value) =>
        indent + s"Leaf [${bounds.renderAsString}] - ${value.renderAsString}"

      case QuadBranch(bounds, a, b, c, d) =>
        s"""${indent}Branch [${bounds.renderAsString}]
           |${renderAsStringWithIndent(a, indent + "  ")}
           |${renderAsStringWithIndent(b, indent + "  ")}
           |${renderAsStringWithIndent(c, indent + "  ")}
           |${renderAsStringWithIndent(d, indent + "  ")}""".stripMargin
    }
  }

}

trait QuadBounds {
  val x: Int
  val y: Int
  val width: Int
  val height: Int

  def left: Int = x
  def top: Int = y
  def right: Int = x + width
  def bottom: Int = y + height

  def isOneUnitSquare: Boolean =
    width == 1 && height == 1

  def subdivide: (QuadBounds, QuadBounds, QuadBounds, QuadBounds) =
    QuadBounds.subdivide(this)

  def isPointWithinBounds(gridPoint: GridPoint): Boolean =
    QuadBounds.pointWithinBounds(this, gridPoint)

  def renderAsString: String =
    s"""($x, $y, $width, $height)"""

  def ===(other: QuadBounds): Boolean =
    QuadBounds.equals(this, other)

  override def toString: String =
    s"""QuadBounds($x, $y, $width, $height)"""

}

object QuadBounds {

  def apply(powerOf2: Int): QuadBounds =
    unsafeCreate(
      0,
      0,
      if(powerOf2 < 2) 2 else powerOf2,
      if(powerOf2 < 2) 2 else powerOf2
    )

  def apply(_x: Int, _y: Int, _width: Int, _height: Int): QuadBounds =
    unsafeCreate(
      if(_x < 0) 0 else _x,
      if(_y < 0) 0 else _y,
      if(_width < 2) 2 else _width,
      if(_height < 2) 2 else _height
    )

  def unsafeCreate(_x: Int, _y: Int, _width: Int, _height: Int): QuadBounds =
    new QuadBounds {
      val x: Int = _x
      val y: Int = _y
      val width: Int = if(_width < 1) 1 else _width
      val height: Int = if(_height < 1) 1 else _height
    }

  def pointWithinBounds(quadBounds: QuadBounds, gridPoint: GridPoint): Boolean =
    gridPoint.x >= quadBounds.left &&
      gridPoint.y >= quadBounds.top &&
      gridPoint.x < quadBounds.right &&
      gridPoint.y < quadBounds.bottom

  def subdivide(quadBounds: QuadBounds): (QuadBounds, QuadBounds, QuadBounds, QuadBounds) =
    (
      unsafeCreate(quadBounds.x, quadBounds.y, quadBounds.width / 2, quadBounds.height / 2),
      unsafeCreate(quadBounds.x + (quadBounds.width / 2), quadBounds.y, quadBounds.width - (quadBounds.width / 2), quadBounds.height / 2),
      unsafeCreate(quadBounds.x, quadBounds.y + (quadBounds.height / 2), quadBounds.width / 2, quadBounds.height - (quadBounds.height / 2)),
      unsafeCreate(quadBounds.x + (quadBounds.width / 2), quadBounds.y + (quadBounds.height / 2), quadBounds.width - (quadBounds.width / 2), quadBounds.height - (quadBounds.height / 2))
    )

  def combine(head: QuadBounds, tail: List[QuadBounds]): QuadBounds =
    tail.foldLeft(head)((a, b) => append(a, b))

  def append(a: QuadBounds, b: QuadBounds): QuadBounds = {
    val l = Math.min(a.left, b.left)
    val t = Math.min(a.top, b.top)
    val w = Math.max(a.right, b.right) - l
    val h = Math.max(a.bottom, b.bottom) - t

    unsafeCreate(l, t, w, h)
  }

  def equals(a: QuadBounds, b: QuadBounds): Boolean =
    a.x == b.x && a.y == b.y && a.width == b.width && a.height == b.height
}

case class GridPoint(x: Int, y: Int) {

  def ===(other: GridPoint): Boolean =
    GridPoint.equality(this, other)

  def +(other: GridPoint): GridPoint =
    GridPoint.append(this, other)

}
object GridPoint {

  implicit def tupleToGridPoint(t: (Int, Int)): GridPoint =
    GridPoint(t._1, t._2)

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

  def renderAsString: String

}
object MapElement {

  case class Wall(gridPoint: GridPoint) extends MapElement {
    def pure[A <: MapElement]: GridPoint => Wall = (gridPoint: GridPoint) => Wall.apply(gridPoint)
    def renderAsString: String = "Wall"
  }

  case class Apple(gridPoint: GridPoint) extends MapElement {
    def pure[A <: MapElement]: GridPoint => Apple = (gridPoint: GridPoint) => Apple.apply(gridPoint)
    def renderAsString: String = "Apple"
  }

  object Apple {
    def spawn(gridSize: GridSize): Apple = {
      def rand(max: Int, border: Int): Int =
        ((max - (border * 2)) * Random.nextFloat()).toInt + border

      Apple(GridPoint(rand(gridSize.columns, 1), rand(gridSize.rows, 1)))
    }
  }

  case class Player1Start(gridPoint: GridPoint) extends MapElement {
    def pure[A <: MapElement]: GridPoint => Player1Start = (gridPoint: GridPoint) => Player1Start.apply(gridPoint)
    def renderAsString: String = "Player 1 Start"
  }

  case class Player2Start(gridPoint: GridPoint) extends MapElement {
    def pure[A <: MapElement]: GridPoint => Player2Start = (gridPoint: GridPoint) => Player2Start.apply(gridPoint)
    def renderAsString: String = "Player 2 Start"
  }

  case class Player3Start(gridPoint: GridPoint) extends MapElement {
    def pure[A <: MapElement]: GridPoint => Player3Start = (gridPoint: GridPoint) => Player3Start.apply(gridPoint)
    def renderAsString: String = "Player 3 Start"
  }

  case class Player4Start(gridPoint: GridPoint) extends MapElement {
    def pure[A <: MapElement]: GridPoint => Player4Start = (gridPoint: GridPoint) => Player4Start.apply(gridPoint)
    def renderAsString: String = "Player 4 Start"
  }

}