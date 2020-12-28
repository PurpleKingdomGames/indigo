package indigoextras.trees

import indigoextras.geometry.Vertex
import indigoextras.geometry.BoundingBox
import indigoextras.geometry.LineSegment

sealed trait QuadTree[T] {

  val bounds: BoundingBox

  def isEmpty: Boolean

  def fetchElementAt(vertex: Vertex): Option[T] =
    QuadTree.fetchElementAt(this, vertex)

  def insertElement(element: T, vertex: Vertex): QuadTree[T] =
    QuadTree.insertElementAt(vertex, this, element)

  def insertElements(elements: (T, Vertex)*): QuadTree[T] =
    insertElements(elements.toList)
  def insertElements(elements: List[(T, Vertex)]): QuadTree[T] =
    elements.foldLeft(this)((acc, item) => acc.insertElement(item._1, item._2))

  def removeElement(vertex: Vertex): QuadTree[T] =
    QuadTree.removeElement(this, vertex)

  def asElementList: List[T] =
    QuadTree.asElementList(this)

  def prune: QuadTree[T] =
    QuadTree.prune(this)

  def searchByPoint(point: Vertex): Option[T] =
    QuadTree.searchByPoint(this, point)

  def searchByLine(start: Vertex, end: Vertex): List[T] =
    QuadTree.searchByLine(this, start, end)

  def searchByRectangle(boundingBox: BoundingBox): List[T] =
    QuadTree.searchByBoundingBox(this, boundingBox)

  def prettyPrint: String = {
    // @SuppressWarnings(Array("org.wartremover.warts.Recursion", "org.wartremover.warts.ToString"))
    def rec(quadTree: QuadTree[T], indent: String): String =
      quadTree match {
        case QuadTree.QuadEmpty(bounds) =>
          indent + s"Empty [${bounds.toString()}]"

        case QuadTree.QuadLeaf(bounds, exactPosition, value) =>
          indent + s"Leaf [${bounds.toString()}] - ${exactPosition.toString} - ${value.toString()}"

        case QuadTree.QuadBranch(bounds, a, b, c, d) =>
          s"""${indent}Branch [${bounds.toString()}]
             |${rec(a, indent + "  ")}
             |${rec(b, indent + "  ")}
             |${rec(c, indent + "  ")}
             |${rec(d, indent + "  ")}""".stripMargin
      }

    rec(this, "")
  }

  def ===(other: QuadTree[T]): Boolean = {
    // @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
    def rec(a: QuadTree[T], b: QuadTree[T]): Boolean =
      (a, b) match {
        case (QuadTree.QuadEmpty(b1), QuadTree.QuadEmpty(b2)) if b1 ~== b2 =>
          true

        case (QuadTree.QuadLeaf(b1, p1, v1), QuadTree.QuadLeaf(b2, p2, v2)) if b1 ~== b2 =>
          (p1 ~== p2) && v1 == v2

        case (QuadTree.QuadBranch(bounds1, a1, b1, c1, d1), QuadTree.QuadBranch(bounds2, a2, b2, c2, d2)) =>
          (bounds1 ~== bounds2) && rec(a1, a2) && rec(b1, b2) && rec(c1, c2) && rec(d1, d2)

        case _ =>
          false
      }

    rec(this, other)
  }

}
object QuadTree {

  def empty[T](width: Double, height: Double): QuadTree[T] =
    QuadEmpty(BoundingBox(0, 0, width, height))

  def empty[T](gridSize: Vertex): QuadTree[T] =
    QuadEmpty(BoundingBox(Vertex.zero, gridSize))

  def apply[T](elements: (T, Vertex)*): QuadTree[T] =
    QuadTree(elements.toList)
  def apply[T](elements: List[(T, Vertex)]): QuadTree[T] =
    QuadEmpty(BoundingBox.fromVertexCloud(elements.map(_._2))).insertElements(elements)

  final case class QuadBranch[T](bounds: BoundingBox, a: QuadTree[T], b: QuadTree[T], c: QuadTree[T], d: QuadTree[T]) extends QuadTree[T] {
    def isEmpty: Boolean =
      a.isEmpty && b.isEmpty && c.isEmpty && d.isEmpty
  }
  final case class QuadLeaf[T](bounds: BoundingBox, exactPosition: Vertex, value: T) extends QuadTree[T] {
    def isEmpty: Boolean = false
  }
  final case class QuadEmpty[T](bounds: BoundingBox) extends QuadTree[T] {
    def isEmpty: Boolean = true
  }

  object QuadBranch {

    def fromBounds[T](bounds: BoundingBox): QuadBranch[T] =
      fromBoundsAndQuads(bounds, subdivide(bounds))

    def subdivide(quadBounds: BoundingBox): (BoundingBox, BoundingBox, BoundingBox, BoundingBox) =
      (
        BoundingBox(quadBounds.x, quadBounds.y, quadBounds.width / 2, quadBounds.height / 2),
        BoundingBox(
          quadBounds.x + (quadBounds.width / 2),
          quadBounds.y,
          quadBounds.width - (quadBounds.width / 2),
          quadBounds.height / 2
        ),
        BoundingBox(
          quadBounds.x,
          quadBounds.y + (quadBounds.height / 2),
          quadBounds.width / 2,
          quadBounds.height - (quadBounds.height / 2)
        ),
        BoundingBox(
          quadBounds.x + (quadBounds.width / 2),
          quadBounds.y + (quadBounds.height / 2),
          quadBounds.width - (quadBounds.width / 2),
          quadBounds.height - (quadBounds.height / 2)
        )
      )

    def fromBoundsAndQuads[T](bounds: BoundingBox, quads: (BoundingBox, BoundingBox, BoundingBox, BoundingBox)): QuadBranch[T] =
      QuadBranch(
        bounds,
        QuadEmpty(quads._1),
        QuadEmpty(quads._2),
        QuadEmpty(quads._3),
        QuadEmpty(quads._4)
      )
  }

  def fetchElementAt[T](quadTree: QuadTree[T], vertex: Vertex): Option[T] =
    quadTree match {
      case QuadEmpty(bounds) if bounds.contains(vertex) =>
        None

      case QuadBranch(bounds, a, b, c, d) if bounds.contains(vertex) =>
        List(
          a.fetchElementAt(vertex),
          b.fetchElementAt(vertex),
          c.fetchElementAt(vertex),
          d.fetchElementAt(vertex)
        ).find(p => p.isDefined).flatten

      case QuadLeaf(_, position, value) if position ~== vertex =>
        Some(value)

      case _ =>
        None
    }

  def insertElementAt[T](vertex: Vertex, quadTree: QuadTree[T], element: T): QuadTree[T] =
    quadTree match {
      case QuadEmpty(bounds) if bounds.contains(vertex) =>
        // Straight insert
        QuadLeaf(bounds, vertex, element)

      case QuadLeaf(bounds, position, _) if position ~== vertex =>
        // Replace
        QuadLeaf(bounds, vertex, element)

      case QuadLeaf(bounds, position, value) if bounds.contains(vertex) =>
        // Both elements in the same region but not overlapping,
        // subdivide and insert both.
        QuadBranch
          .fromBounds(bounds)
          .insertElement(value, position) // original
          .insertElement(element, vertex) // new

      case QuadBranch(bounds, a, b, c, d) if bounds.contains(vertex) =>
        // Delegate to sub-regions
        QuadBranch[T](
          bounds,
          insertElementAt(vertex, a, element),
          insertElementAt(vertex, b, element),
          insertElementAt(vertex, c, element),
          insertElementAt(vertex, d, element)
        )

      case _ =>
        quadTree
    }

  def removeElement[T](quadTree: QuadTree[T], vertex: Vertex): QuadTree[T] =
    quadTree match {
      case QuadLeaf(bounds, p, _) if bounds.contains(vertex) && (p ~== vertex) =>
        QuadEmpty(bounds)

      case QuadBranch(bounds, a, b, c, d) if bounds.contains(vertex) =>
        QuadBranch[T](
          bounds,
          a.removeElement(vertex),
          b.removeElement(vertex),
          c.removeElement(vertex),
          d.removeElement(vertex)
        )

      case tree =>
        tree
    }

  // @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
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

  // @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  def searchByPoint[T](quadTree: QuadTree[T], point: Vertex): Option[T] =
    quadTree match {
      case QuadBranch(bounds, a, b, c, d) if bounds.contains(point) =>
        searchByPoint(a, point)
          .orElse(
            searchByPoint(b, point)
              .orElse(
                searchByPoint(c, point)
                  .orElse(searchByPoint(d, point))
              )
          )

      case QuadLeaf(bounds, _, value) if bounds.contains(point) =>
        Some(value)

      case _ =>
        None
    }

  def searchByLine[T](quadTree: QuadTree[T], start: Vertex, end: Vertex): List[T] =
    searchByLine(quadTree, LineSegment(start, end))

  // @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  def searchByLine[T](quadTree: QuadTree[T], lineSegment: LineSegment): List[T] =
    quadTree match {
      case QuadBranch(bounds, a, b, c, d) if bounds.contains(lineSegment.start) =>
        searchByLine(a, lineSegment) ++
          searchByLine(b, lineSegment) ++
          searchByLine(c, lineSegment) ++
          searchByLine(d, lineSegment)

      case QuadBranch(bounds, a, b, c, d) if bounds.contains(lineSegment.end) =>
        searchByLine(a, lineSegment) ++
          searchByLine(b, lineSegment) ++
          searchByLine(c, lineSegment) ++
          searchByLine(d, lineSegment)

      case QuadBranch(bounds, a, b, c, d) if bounds.lineIntersects(lineSegment) =>
        searchByLine(a, lineSegment) ++
          searchByLine(b, lineSegment) ++
          searchByLine(c, lineSegment) ++
          searchByLine(d, lineSegment)

      case QuadLeaf(bounds, _, value) if bounds.contains(lineSegment.start) =>
        List(value)

      case QuadLeaf(bounds, _, value) if bounds.contains(lineSegment.end) =>
        List(value)

      case QuadLeaf(bounds, _, value) if bounds.lineIntersects(lineSegment) =>
        List(value)

      case _ =>
        Nil
    }

  // @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  def searchByBoundingBox[T](quadTree: QuadTree[T], boundingBox: BoundingBox): List[T] =
    quadTree match {
      case QuadBranch(bounds, a, b, c, d) if boundingBox.overlaps(bounds) =>
        searchByBoundingBox(a, boundingBox) ++
          searchByBoundingBox(b, boundingBox) ++
          searchByBoundingBox(c, boundingBox) ++
          searchByBoundingBox(d, boundingBox)

      case QuadLeaf(_, exactPosition, value) if boundingBox.contains(exactPosition) =>
        List(value)

      case _ =>
        Nil
    }

}
