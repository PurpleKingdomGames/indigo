package indigoextras.trees

import indigo.shared.collections.Batch
import indigo.shared.geometry.BoundingBox
import indigo.shared.geometry.LineSegment
import indigo.shared.geometry.Vertex

import scala.annotation.tailrec

enum QuadTree[S, T](val isEmpty: Boolean)(using s: SpatialOps[S]) derives CanEqual:
  case Branch(
      bounds: BoundingBox,
      a: QuadTree[S, T],
      b: QuadTree[S, T],
      c: QuadTree[S, T],
      d: QuadTree[S, T]
  )(using SpatialOps[S]) extends QuadTree[S, T](a.isEmpty && b.isEmpty && c.isEmpty && d.isEmpty)
  case Leaf(bounds: BoundingBox, ref: S, value: T)(using SpatialOps[S]) extends QuadTree[S, T](false)
  case Empty(bounds: BoundingBox)(using SpatialOps[S])                  extends QuadTree[S, T](true)

  val bounds: BoundingBox

  def fetchElement(ref: S)(using CanEqual[T, T]): Option[T] =
    QuadTree.fetchElement(ref, this)

  def insertElement(ref: S, element: T): QuadTree[S, T] =
    QuadTree.insertElement(ref, this, element)

  def insertElements(elements: (S, T)*): QuadTree[S, T] =
    insertElements(Batch.fromSeq(elements))
  def insertElements(elements: Batch[(S, T)]): QuadTree[S, T] =
    elements.foldLeft(this)((acc, item) => acc.insertElement(item._1, item._2))

  def toBatch(using CanEqual[T, T]): Batch[T] =
    QuadTree.toBatch(this, _ => true)
  def toBatch(p: T => Boolean)(using CanEqual[T, T]): Batch[T] =
    QuadTree.toBatch(this, p)

  def toBatchWithPosition(using CanEqual[T, T]): Batch[(S, T)] =
    QuadTree.toBatchWithPosition(this, _ => true)
  def toBatchWithPosition(p: T => Boolean)(using CanEqual[T, T]): Batch[(S, T)] =
    QuadTree.toBatchWithPosition(this, p)

  def prune: QuadTree[S, T] =
    QuadTree.prune(this)

  def findClosestTo(vertex: Vertex)(using CanEqual[T, T], SpatialOps[S]): Option[T] =
    QuadTree.findClosestTo(vertex, this)
  def findClosestToWithPosition(vertex: Vertex)(using CanEqual[T, T], SpatialOps[S]): Option[(S, T)] =
    QuadTree.findClosestToWithPosition(vertex, this)

  def searchByLine(start: Vertex, end: Vertex)(using CanEqual[T, T]): Batch[T] =
    QuadTree.searchByLine(this, start, end)
  def searchByLine(line: LineSegment)(using CanEqual[T, T]): Batch[T] =
    QuadTree.searchByLine(this, line)

  def searchByLineWithPosition(start: Vertex, end: Vertex)(using CanEqual[T, T]): Batch[(S, T)] =
    QuadTree.searchByLineWithPosition(this, start, end)
  def searchByLineWithPosition(line: LineSegment)(using CanEqual[T, T]): Batch[(S, T)] =
    QuadTree.searchByLineWithPosition(this, line)

  def searchByBoundingBox(boundingBox: BoundingBox)(using CanEqual[T, T]): Batch[T] =
    QuadTree.searchByBoundingBox(this, boundingBox)
  def searchByBoundingBoxWithPosition(boundingBox: BoundingBox)(using CanEqual[T, T]): Batch[(S, T)] =
    QuadTree.searchByBoundingBoxWithPosition(this, boundingBox)

  def prettyPrint: String =
    // Not tail recursive
    def rec(quadTree: QuadTree[S, T], indent: String): String =
      quadTree match
        case QuadTree.Empty(bounds) =>
          indent + s"Empty [${bounds.toString()}]"

        case QuadTree.Leaf(bounds, ref, value) =>
          indent + s"Leaf [${bounds.toString()}] - ${ref.toString} - ${value.toString()}"

        case QuadTree.Branch(bounds, a, b, c, d) =>
          s"""${indent}Branch [${bounds.toString()}]
             |${rec(a, indent + "  ")}
             |${rec(b, indent + "  ")}
             |${rec(c, indent + "  ")}
             |${rec(d, indent + "  ")}""".stripMargin

    rec(this, "")

  def ===(other: QuadTree[S, T])(using CanEqual[T, T]): Boolean =
    @tailrec
    def rec(a: List[QuadTree[S, T]], b: List[QuadTree[S, T]]): Boolean =
      (a, b) match
        case (Nil, Nil) =>
          true

        case (Nil, _) =>
          false

        case (_, Nil) =>
          false

        case (QuadTree.Empty(b1) :: as, QuadTree.Empty(b2) :: bs) if b1 ~== b2 =>
          rec(as, bs)

        case (QuadTree.Leaf(b1, p1, v1) :: as, QuadTree.Leaf(b2, p2, v2) :: bs)
            if (b1 ~== b2) && s.equals(p1, p2) && v1 == v2 =>
          rec(as, bs)

        case (QuadTree.Branch(bounds1, a1, b1, c1, d1) :: as, QuadTree.Branch(bounds2, a2, b2, c2, d2) :: bs)
            if bounds1 ~== bounds2 =>
          rec(a1 :: b1 :: c1 :: d1 :: as, a2 :: b2 :: c2 :: d2 :: bs)

        case _ =>
          false

    rec(List(this), List(other))

  def !==(other: QuadTree[S, T])(using CanEqual[T, T]): Boolean =
    !(this === other)

object QuadTree:

  given [S, T](using CanEqual[T, T]): CanEqual[Option[QuadTree[S, T]], Option[QuadTree[S, T]]] = CanEqual.derived
  given [S, T](using CanEqual[T, T]): CanEqual[Batch[QuadTree[S, T]], Batch[QuadTree[S, T]]]   = CanEqual.derived

  def empty[S, T](width: Double, height: Double)(using SpatialOps[S]): QuadTree[S, T] =
    Empty(BoundingBox(0, 0, width, height))

  def empty[S, T](gridSize: Vertex)(using SpatialOps[S]): QuadTree[S, T] =
    Empty(BoundingBox(Vertex.zero, gridSize))

  def apply[S, T](elements: (S, T)*)(using SpatialOps[S]): QuadTree[S, T] =
    QuadTree(Batch.fromSeq(elements))
  def apply[S, T](elements: Batch[(S, T)])(using s: SpatialOps[S]): QuadTree[S, T] =
    val b =
      if elements.isEmpty then BoundingBox.zero
      else
        elements.tail.foldLeft(s.bounds(elements.head._1)) { case (acc, next) =>
          acc.expandToInclude(s.bounds(next._1))
        }

    Empty(b).insertElements(elements)

  object Branch:

    def fromBounds[S, T](bounds: BoundingBox)(using SpatialOps[S]): Branch[S, T] =
      fromBoundsAndQuads(bounds, subdivide(bounds))

    def subdivide(quadBounds: BoundingBox): (BoundingBox, BoundingBox, BoundingBox, BoundingBox) =
      val newWidth  = quadBounds.width / 2
      val newHeight = quadBounds.height / 2
      (
        BoundingBox(quadBounds.x, quadBounds.y, newWidth, newHeight),
        BoundingBox(
          quadBounds.x + newWidth,
          quadBounds.y,
          newWidth,
          newHeight
        ),
        BoundingBox(
          quadBounds.x,
          quadBounds.y + newHeight,
          newWidth,
          newHeight
        ),
        BoundingBox(
          quadBounds.x + newWidth,
          quadBounds.y + newHeight,
          newWidth,
          newHeight
        )
      )

    def fromBoundsAndQuads[S, T](
        bounds: BoundingBox,
        quads: (BoundingBox, BoundingBox, BoundingBox, BoundingBox)
    )(using SpatialOps[S]): Branch[S, T] =
      Branch(
        bounds,
        Empty(quads._1),
        Empty(quads._2),
        Empty(quads._3),
        Empty(quads._4)
      )

  def fetchElement[S, T](ref: S, quadTree: QuadTree[S, T])(using CanEqual[T, T])(using s: SpatialOps[S]): Option[T] =
    @tailrec
    def rec(remaining: List[QuadTree[S, T]]): Option[T] =
      remaining match
        case Nil =>
          None

        case Leaf(_, otherRef, value) :: xs if s.equals(otherRef, ref) =>
          Some(value)

        case Branch(bounds, a, b, c, d) :: xs if s.within(ref, bounds) =>
          rec(xs ++ List(a, b, c, d))

        case _ :: xs =>
          rec(xs)

    rec(List(quadTree))

  def insertElement[S, T](ref: S, quadTree: QuadTree[S, T], element: T)(using s: SpatialOps[S]): QuadTree[S, T] =
    quadTree match
      case Empty(bounds) if s.within(ref, bounds) =>
        // Straight insert
        Leaf(bounds, ref, element)

      case Leaf(bounds, otherRef, _) if s.equals(otherRef, ref) =>
        // Replace
        Leaf(bounds, ref, element)

      case Leaf(bounds, otherRef, value) if s.within(ref, bounds) =>
        // Both elements in the same region but not overlapping,
        // subdivide and insert both.
        Branch
          .fromBounds(bounds)
          .insertElement(otherRef, value) // original
          .insertElement(ref, element)    // new

      case Branch(bounds, a, b, c, d) if s.within(ref, bounds) =>
        // Delegate to sub-regions
        Branch[S, T](
          bounds,
          insertElement(ref, a, element),
          insertElement(ref, b, element),
          insertElement(ref, c, element),
          insertElement(ref, d, element)
        )

      case _ =>
        quadTree

  def toBatch[S, T](quadTree: QuadTree[S, T], p: T => Boolean)(using CanEqual[T, T]): Batch[T] =
    @tailrec
    def rec(open: List[QuadTree[S, T]], acc: Batch[T]): Batch[T] =
      open match
        case Nil =>
          acc

        case x :: xs =>
          x match {
            case _: Empty[S, T] =>
              rec(xs, acc)

            case l: Leaf[S, T] =>
              val v = l.value
              if p(v) then rec(xs, v :: acc)
              else rec(xs, acc)

            case b: Branch[S, T] if b.isEmpty =>
              rec(xs, acc)

            case Branch(_, a, b, c, d) =>
              val next =
                (if a.isEmpty then Nil else List(a)) ++
                  (if b.isEmpty then Nil else List(b)) ++
                  (if c.isEmpty then Nil else List(c)) ++
                  (if d.isEmpty then Nil else List(d))

              rec(xs ++ next, acc)
          }

    rec(List(quadTree), Batch.empty)

  def toBatchWithPosition[S, T](quadTree: QuadTree[S, T], p: T => Boolean)(using CanEqual[T, T]): Batch[(S, T)] =
    @tailrec
    def rec(open: List[QuadTree[S, T]], acc: Batch[(S, T)]): Batch[(S, T)] =
      open match
        case Nil =>
          acc

        case x :: xs =>
          x match {
            case _: Empty[S, T] =>
              rec(xs, acc)

            case l: Leaf[S, T] =>
              val v = l.value
              if p(v) then rec(xs, (l.ref, v) :: acc)
              else rec(xs, acc)

            case b: Branch[S, T] if b.isEmpty =>
              rec(xs, acc)

            case Branch(_, a, b, c, d) =>
              val next =
                (if a.isEmpty then Nil else List(a)) ++
                  (if b.isEmpty then Nil else List(b)) ++
                  (if c.isEmpty then Nil else List(c)) ++
                  (if d.isEmpty then Nil else List(d))

              rec(xs ++ next, acc)
          }

    rec(List(quadTree), Batch.empty)

  def prune[S, T](quadTree: QuadTree[S, T])(using SpatialOps[S]): QuadTree[S, T] =
    quadTree match
      case l: Leaf[S, T] =>
        l

      case e: Empty[S, T] =>
        e

      case b: Branch[S, T] if b.isEmpty =>
        Empty(b.bounds)

      case Branch(bounds, a, b, c, d) =>
        Branch[S, T](bounds, a.prune, b.prune, c.prune, d.prune)

  def findClosestToWithPosition[S, T](vertex: Vertex, quadTree: QuadTree[S, T], p: T => Boolean)(using CanEqual[T, T])(
      using s: SpatialOps[S]
  ): Option[(S, T)] =
    @tailrec
    def rec(remaining: List[QuadTree[S, T]], closestDistance: Double, acc: Option[(S, T)]): Option[(S, T)] =
      remaining match
        case Nil =>
          acc

        case Leaf(_, ref, value) :: rs =>
          val dist = s.distance(ref, vertex)
          if dist < closestDistance && p(value) then rec(rs, dist, Some((ref, value)))
          else rec(rs, closestDistance, acc)

        case Branch(bounds, a, b, c, d) :: rs if vertex.distanceTo(bounds.center) < closestDistance =>
          rec(a :: b :: c :: d :: rs, closestDistance, acc)

        case _ :: rs =>
          rec(rs, closestDistance, acc)

    rec(List(quadTree), Double.MaxValue, None)
  def findClosestToWithPosition[S, T](vertex: Vertex, quadTree: QuadTree[S, T])(using
      CanEqual[T, T],
      SpatialOps[S]
  ): Option[(S, T)] =
    findClosestToWithPosition(vertex, quadTree, _ => true)

  def findClosestTo[S, T](vertex: Vertex, quadTree: QuadTree[S, T], p: T => Boolean)(using CanEqual[T, T])(using
      s: SpatialOps[S]
  ): Option[T] =
    @tailrec
    def rec(remaining: List[QuadTree[S, T]], closestDistance: Double, acc: Option[T]): Option[T] =
      remaining match
        case Nil =>
          acc

        case Leaf(_, ref, value) :: rs =>
          val dist = s.distance(ref, vertex)
          if dist < closestDistance && p(value) then rec(rs, dist, Some(value))
          else rec(rs, closestDistance, acc)

        case Branch(bounds, a, b, c, d) :: rs if vertex.distanceTo(bounds.center) < closestDistance =>
          rec(a :: b :: c :: d :: rs, closestDistance, acc)

        case _ :: rs =>
          rec(rs, closestDistance, acc)

    rec(List(quadTree), Double.MaxValue, None)
  def findClosestTo[S, T](vertex: Vertex, quadTree: QuadTree[S, T])(using CanEqual[T, T], SpatialOps[S]): Option[T] =
    findClosestTo(vertex, quadTree, _ => true)

  def searchByLineWithPosition[S, T](quadTree: QuadTree[S, T], lineSegment: LineSegment, p: T => Boolean)(using
      CanEqual[T, T],
      SpatialOps[S]
  ): Batch[(S, T)] =
    @tailrec
    def rec(remaining: List[QuadTree[S, T]], acc: Batch[(S, T)]): Batch[(S, T)] =
      remaining match
        case Nil =>
          acc

        case Branch(bounds, a, b, c, d) :: rs =>
          if bounds.contains(lineSegment.start) || bounds.contains(lineSegment.end) ||
            bounds.lineIntersects(lineSegment)
          then rec(rs ++ List(a, b, c, d), acc)
          else rec(rs, acc)

        case Leaf(bounds, pos, value) :: rs =>
          if (bounds.contains(lineSegment.start) || bounds.contains(lineSegment.end) ||
              bounds.lineIntersects(lineSegment)) && p(value)
          then rec(rs, (pos, value) :: acc)
          else rec(rs, acc)

        case _ :: rs =>
          rec(rs, acc)

    rec(List(quadTree), Batch.empty)
  def searchByLineWithPosition[S, T](quadTree: QuadTree[S, T], lineSegment: LineSegment)(using
      CanEqual[T, T],
      SpatialOps[S]
  ): Batch[(S, T)] =
    searchByLineWithPosition(quadTree, lineSegment, _ => true)
  def searchByLineWithPosition[S, T](quadTree: QuadTree[S, T], start: Vertex, end: Vertex, p: T => Boolean)(using
      CanEqual[T, T],
      SpatialOps[S]
  ): Batch[(S, T)] =
    searchByLineWithPosition(quadTree, LineSegment(start, end), p)
  def searchByLineWithPosition[S, T](quadTree: QuadTree[S, T], start: Vertex, end: Vertex)(using
      CanEqual[T, T],
      SpatialOps[S]
  ): Batch[(S, T)] =
    searchByLineWithPosition(quadTree, start, end, _ => true)

  def searchByLine[S, T](quadTree: QuadTree[S, T], lineSegment: LineSegment, p: T => Boolean)(using
      CanEqual[T, T]
  ): Batch[T] =
    @tailrec
    def rec(remaining: List[QuadTree[S, T]], acc: Batch[T]): Batch[T] =
      remaining match
        case Nil =>
          acc

        case Branch(bounds, a, b, c, d) :: rs =>
          if bounds.contains(lineSegment.start) || bounds.contains(lineSegment.end) ||
            bounds.lineIntersects(lineSegment)
          then rec(rs ++ List(a, b, c, d), acc)
          else rec(rs, acc)

        case Leaf(bounds, pos, value) :: rs =>
          if (bounds.contains(lineSegment.start) || bounds.contains(lineSegment.end) ||
              bounds.lineIntersects(lineSegment)) && p(value)
          then rec(rs, value :: acc)
          else rec(rs, acc)

        case _ :: rs =>
          rec(rs, acc)

    rec(List(quadTree), Batch.empty)
  def searchByLine[S, T](quadTree: QuadTree[S, T], lineSegment: LineSegment)(using CanEqual[T, T]): Batch[T] =
    searchByLine(quadTree, lineSegment, _ => true)
  def searchByLine[S, T](quadTree: QuadTree[S, T], start: Vertex, end: Vertex, p: T => Boolean)(using
      CanEqual[T, T]
  ): Batch[T] =
    searchByLine(quadTree, LineSegment(start, end), p)
  def searchByLine[S, T](quadTree: QuadTree[S, T], start: Vertex, end: Vertex)(using CanEqual[T, T]): Batch[T] =
    searchByLine(quadTree, LineSegment(start, end), _ => true)

  def searchByBoundingBoxWithPosition[S, T](quadTree: QuadTree[S, T], boundingBox: BoundingBox, p: T => Boolean)(using
      CanEqual[T, T]
  )(using s: SpatialOps[S]): Batch[(S, T)] =
    @tailrec
    def rec(remaining: List[QuadTree[S, T]], acc: Batch[(S, T)]): Batch[(S, T)] =
      remaining match
        case Nil =>
          acc

        case Leaf(_, ref, value) :: rs =>
          if s.within(ref, boundingBox) && p(value) then rec(rs, (ref, value) :: acc)
          else rec(rs, acc)

        case Branch(bounds, a, b, c, d) :: rs if boundingBox.overlaps(bounds) =>
          rec(rs ++ List(a, b, c, d), acc)

        case _ :: rs =>
          rec(rs, acc)

    rec(List(quadTree), Batch.empty)
  def searchByBoundingBoxWithPosition[S, T](quadTree: QuadTree[S, T], boundingBox: BoundingBox)(using
      CanEqual[T, T],
      SpatialOps[S]
  ): Batch[(S, T)] =
    searchByBoundingBoxWithPosition(quadTree, boundingBox, _ => true)

  def searchByBoundingBox[S, T](quadTree: QuadTree[S, T], boundingBox: BoundingBox, p: T => Boolean)(using
      CanEqual[T, T]
  )(using s: SpatialOps[S]): Batch[T] =
    @tailrec
    def rec(remaining: List[QuadTree[S, T]], acc: Batch[T]): Batch[T] =
      remaining match
        case Nil =>
          acc

        case Leaf(_, ref, value) :: rs =>
          if s.within(ref, boundingBox) && p(value) then rec(rs, value :: acc)
          else rec(rs, acc)

        case Branch(bounds, a, b, c, d) :: rs if boundingBox.overlaps(bounds) =>
          rec(rs ++ List(a, b, c, d), acc)

        case _ :: rs =>
          rec(rs, acc)

    rec(List(quadTree), Batch.empty)
  def searchByBoundingBox[S, T](quadTree: QuadTree[S, T], boundingBox: BoundingBox)(using
      CanEqual[T, T],
      SpatialOps[S]
  ): Batch[T] =
    searchByBoundingBox(quadTree, boundingBox, _ => true)
