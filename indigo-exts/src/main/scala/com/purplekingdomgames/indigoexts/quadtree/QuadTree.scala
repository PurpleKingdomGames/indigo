package com.purplekingdomgames.indigoexts.quadtree

import com.purplekingdomgames.indigoexts.grid.{GridPoint, GridSize}

sealed trait QuadTree[T] {

  val bounds: QuadBounds

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

  def search(p: QuadBounds => Boolean): List[T] =
    QuadTree.search(this, p)

  def renderAsString: String =
    QuadTree.renderAsString(this)

}
object QuadTree {

  def empty[T](sizeAsPowerOf2: Int): QuadTree[T] =
    QuadEmpty(QuadBounds.apply(sizeAsPowerOf2))

  def empty[T](gridSize: GridSize): QuadTree[T] =
    QuadEmpty(QuadBounds.apply(gridSize.asPowerOf2))

  //TODO: This needs to be recursive. if a is branch, then do another empty check etc.
  case class QuadBranch[T](bounds: QuadBounds, a: QuadTree[T], b: QuadTree[T], c: QuadTree[T], d: QuadTree[T])
      extends QuadTree[T] {
    def isEmpty: Boolean =
      a.isEmpty && b.isEmpty && c.isEmpty && d.isEmpty
  }
  case class QuadLeaf[T](bounds: QuadBounds, value: T) extends QuadTree[T] {
    def isEmpty: Boolean = false
  }
  case class QuadEmpty[T](bounds: QuadBounds) extends QuadTree[T] {
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

  def search[T](quadTree: QuadTree[T], p: QuadBounds => Boolean): List[T] =
    quadTree match {
      case q: QuadLeaf[T] if p(q.bounds) =>
        List(q.value)

      case q @ QuadBranch(_, a, b, c, d) if p(q.bounds) =>
        search(a, p) ++ search(b, p) ++ search(c, p) ++ search(d, p)

      case _ =>
        Nil
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

}
