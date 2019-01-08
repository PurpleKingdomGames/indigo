package indigoexts.quadtrees

import indigo.gameengine.PowerOfTwo
import indigo.gameengine.scenegraph.datatypes.{Point, Rectangle}
import indigo.runtime.Show
import indigoexts.grid.{GridPoint, GridSize}
import indigoexts.line.LineSegment

sealed trait QuadTree[T] {

  val bounds: QuadBounds

  def ===(other: QuadTree[T]): Boolean =
    QuadTree.equalTo(this, other)

  def isEmpty: Boolean

  def fetchElementAt(gridPoint: GridPoint): Option[T] =
    QuadTree.fetchElementAt(this, gridPoint)

  def insertElement(element: T, gridPoint: GridPoint): QuadTree[T] =
    QuadTree.insertElementAt(gridPoint, this, element)

  def removeElement(gridPoint: GridPoint): QuadTree[T] =
    QuadTree.removeElement(this, gridPoint)

  def findEmptySpace(gridSize: GridSize, not: List[GridPoint]): GridPoint =
    QuadTree.findEmptySpace(this, gridSize, not)

  def asElementList: List[T] =
    QuadTree.asElementList(this)

  def prune: QuadTree[T] =
    QuadTree.prune(this)

  def searchByPoint(point: Point): List[T] =
    QuadTree.searchByPoint(this, point)

  def searchByLine(start: Point, end: Point): List[T] =
    QuadTree.searchByLine(this, start, end)

  def searchByRectangle(rectangle: Rectangle): List[T] =
    QuadTree.searchByRectangle(this, rectangle)

  def renderAsString: String =
    QuadTree.renderAsString(this)

}
object QuadTree {

  implicit def showQuadTree[T]: Show[QuadTree[T]] =
    Show.create(t => t.renderAsString)

  def empty[T](size: PowerOfTwo): QuadTree[T] =
    QuadEmpty(QuadBounds.apply(size.value))

  def empty[T](gridSize: GridSize): QuadTree[T] =
    QuadEmpty(QuadBounds.apply(gridSize.asPowerOf2.value))

  final case class QuadBranch[T](bounds: QuadBounds, a: QuadTree[T], b: QuadTree[T], c: QuadTree[T], d: QuadTree[T]) extends QuadTree[T] {
    def isEmpty: Boolean =
      a.isEmpty && b.isEmpty && c.isEmpty && d.isEmpty
  }
  final case class QuadLeaf[T](bounds: QuadBounds, value: T) extends QuadTree[T] {
    def isEmpty: Boolean = false
  }
  final case class QuadEmpty[T](bounds: QuadBounds) extends QuadTree[T] {
    def isEmpty: Boolean = true
  }

  object QuadBranch {
    def fromBounds[T](bounds: QuadBounds): QuadBranch[T] =
      fromBoundsAndQuarters(bounds, bounds.subdivide)

    def fromBoundsAndQuarters[T](bounds: QuadBounds, quarters: (QuadBounds, QuadBounds, QuadBounds, QuadBounds)): QuadBranch[T] =
      QuadBranch(
        bounds,
        QuadEmpty(quarters._1),
        QuadEmpty(quarters._2),
        QuadEmpty(quarters._3),
        QuadEmpty(quarters._4)
      )
  }

  def fetchElementAt[T](quadTree: QuadTree[T], gridPoint: GridPoint): Option[T] =
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

  def insertElementAt[T](gridPoint: GridPoint, quadTree: QuadTree[T], element: T): QuadTree[T] =
    quadTree match {
      case l @ QuadLeaf(bounds, _) if bounds.isPointWithinBounds(gridPoint) =>
        l.copy(value = element)

      case l: QuadLeaf[T] =>
        l

      case QuadBranch(bounds, a, b, c, d) if bounds.isPointWithinBounds(gridPoint) =>
        QuadBranch[T](
          bounds,
          a.insertElement(element, gridPoint),
          b.insertElement(element, gridPoint),
          c.insertElement(element, gridPoint),
          d.insertElement(element, gridPoint)
        )

      case b: QuadBranch[T] =>
        b

      case QuadEmpty(bounds) if bounds.isPointWithinBounds(gridPoint) && bounds.isOneUnitSquare =>
        QuadLeaf(bounds, element)

      case QuadEmpty(bounds) if bounds.isPointWithinBounds(gridPoint) =>
        QuadBranch.fromBounds(bounds).insertElement(element, gridPoint)

      case e: QuadEmpty[T] =>
        e
    }

  def removeElement[T](quadTree: QuadTree[T], gridPoint: GridPoint): QuadTree[T] =
    quadTree match {
      case QuadLeaf(bounds, _) if bounds.isPointWithinBounds(gridPoint) =>
        QuadEmpty(bounds)

      case QuadBranch(bounds, a, b, c, d) if bounds.isPointWithinBounds(gridPoint) =>
        QuadBranch[T](
          bounds,
          a.removeElement(gridPoint),
          b.removeElement(gridPoint),
          c.removeElement(gridPoint),
          d.removeElement(gridPoint)
        )

      case tree =>
        tree
    }

  def findEmptySpace[T](quadTree: QuadTree[T], gridSize: GridSize, not: List[GridPoint]): GridPoint = {
    def makeRandom: () => GridPoint = () => GridPoint.random(gridSize.width - 2, gridSize.height - 2) + GridPoint(1, 1)

    def rec(pt: GridPoint): GridPoint =
      fetchElementAt(quadTree, pt) match {
        case None if !not.contains(pt) =>
          pt

        case None =>
          rec(makeRandom())

        case Some(_) =>
          rec(makeRandom())
      }

    rec(makeRandom())
  }

  def asElementList[T](quadTree: QuadTree[T]): List[T] =
    quadTree match {
      case l: QuadLeaf[T] =>
        List(l.value)

      case _: QuadEmpty[T] =>
        Nil

      case b: QuadBranch[T] if b.isEmpty =>
        Nil

      case QuadBranch(_, a, b, c, d) =>
        asElementList(a) ++ asElementList(b) ++ asElementList(c) ++ asElementList(d)
    }

  def prune[T](quadTree: QuadTree[T]): QuadTree[T] =
    quadTree match {
      case l: QuadLeaf[T] =>
        l

      case e: QuadEmpty[T] =>
        e

      case b: QuadBranch[T] if b.isEmpty =>
        QuadEmpty(b.bounds)

      case QuadBranch(bounds, a, b, c, d) =>
        QuadBranch[T](bounds, a.prune, b.prune, c.prune, d.prune)
    }

  def searchByPoint[T](quadTree: QuadTree[T], point: Point): List[T] =
    quadTree match {
      case QuadBranch(bounds, a, b, c, d) if bounds.isPointWithinBounds(GridPoint.fromPoint(point)) =>
        List(
          searchByPoint(a, point),
          searchByPoint(b, point),
          searchByPoint(c, point),
          searchByPoint(d, point)
        ).flatten

      case QuadLeaf(bounds, value) if bounds.isPointWithinBounds(GridPoint.fromPoint(point)) =>
        List(value)

      case _ =>
        Nil
    }

  def searchByLine[T](quadTree: QuadTree[T], start: Point, end: Point): List[T] =
    searchByLine(quadTree, LineSegment(start, end))

  def searchByLine[T](quadTree: QuadTree[T], lineSegment: LineSegment): List[T] =
    if (lineSegment.start === lineSegment.end) searchByPoint(quadTree, lineSegment.start)
    else {
      quadTree match {
        case QuadBranch(bounds, a, b, c, d) if bounds.toRectangle.isPointWithin(lineSegment.start) =>
          searchByLine(a, lineSegment) ++
            searchByLine(b, lineSegment) ++
            searchByLine(c, lineSegment) ++
            searchByLine(d, lineSegment)

        case QuadBranch(bounds, a, b, c, d) if bounds.toRectangle.isPointWithin(lineSegment.end) =>
          searchByLine(a, lineSegment) ++
            searchByLine(b, lineSegment) ++
            searchByLine(c, lineSegment) ++
            searchByLine(d, lineSegment)

        case QuadBranch(bounds, a, b, c, d) if bounds.collidesWithRay(lineSegment) =>
          searchByLine(a, lineSegment) ++
            searchByLine(b, lineSegment) ++
            searchByLine(c, lineSegment) ++
            searchByLine(d, lineSegment)

        case QuadLeaf(bounds, value) if lineSegment.start === bounds.position.toPoint =>
          List(value)

        case QuadLeaf(bounds, value) if lineSegment.end === bounds.position.toPoint =>
          List(value)

        case QuadLeaf(bounds, value) if LineSegment.lineContainsPoint(lineSegment, bounds.position.toPoint, 0.35f) =>
          List(value)

        case _ =>
          Nil
      }
    }

  def searchByRectangle[T](quadTree: QuadTree[T], rectangle: Rectangle): List[T] =
    if (rectangle.width <= 1 && rectangle.height <= 1) searchByPoint(quadTree, rectangle.position)
    else {
      quadTree match {
        case QuadBranch(bounds, a, b, c, d) if rectangle.overlaps(bounds.toRectangle) =>
          searchByRectangle(a, rectangle) ++
            searchByRectangle(b, rectangle) ++
            searchByRectangle(c, rectangle) ++
            searchByRectangle(d, rectangle)

        case QuadLeaf(bounds, value) if rectangle.isPointWithin(Point(bounds.x, bounds.y)) =>
          List(value)

        case _ =>
          Nil
      }
    }

  def renderAsString[T](quadTree: QuadTree[T]): String =
    renderAsStringWithIndent(quadTree, "")

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  def renderAsStringWithIndent[T](quadTree: QuadTree[T], indent: String): String =
    quadTree match {
      case QuadEmpty(bounds) =>
        indent + s"Empty [${bounds.renderAsString}]"

      case QuadLeaf(bounds, value) =>
        indent + s"Leaf [${bounds.renderAsString}] - ${value.toString}"

      case QuadBranch(bounds, a, b, c, d) =>
        s"""${indent}Branch [${bounds.renderAsString}]
           |${renderAsStringWithIndent(a, indent + "  ")}
           |${renderAsStringWithIndent(b, indent + "  ")}
           |${renderAsStringWithIndent(c, indent + "  ")}
           |${renderAsStringWithIndent(d, indent + "  ")}""".stripMargin
    }

  def equalTo[T](a: QuadTree[T], b: QuadTree[T]): Boolean =
    (a, b) match {
      case (QuadEmpty(b1), QuadEmpty(b2)) if b1 === b2 =>
        true

      case (QuadLeaf(b1, v1), QuadLeaf(b2, v2)) if b1 === b2 && v1 == v2 =>
        true

      case (QuadBranch(bounds1, a1, b1, c1, d1), QuadBranch(bounds2, a2, b2, c2, d2)) =>
        bounds1 === bounds2 && equalTo(a1, a2) && equalTo(b1, b2) && equalTo(c1, c2) && equalTo(d1, d2)

      case _ =>
        false
    }

}
