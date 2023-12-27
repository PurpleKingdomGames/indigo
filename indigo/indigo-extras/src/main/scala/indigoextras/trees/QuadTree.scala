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
  case Leaf(bounds: BoundingBox, values: Batch[QuadTreeValue[S, T]])(using SpatialOps[S]) extends QuadTree[S, T](false)
  case Empty(bounds: BoundingBox)(using SpatialOps[S])                                    extends QuadTree[S, T](true)

  val bounds: BoundingBox

  def insert(location: S, value: T)(using opts: QuadTree.InsertOptions): QuadTree[S, T] =
    QuadTree.insert(
      this,
      QuadTreeValue(location, value),
      opts.idealCount,
      opts.minSize,
      opts.maxDepth,
      0
    )
  def insert(elements: (S, T)*)(using opts: QuadTree.InsertOptions): QuadTree[S, T] =
    QuadTree.insert(
      this,
      Batch.fromSeq(elements).map(QuadTreeValue.fromTuple),
      opts.idealCount,
      opts.minSize,
      opts.maxDepth
    )
  def insert(elements: Batch[(S, T)])(using opts: QuadTree.InsertOptions): QuadTree[S, T] =
    QuadTree.insert(
      this,
      elements.map(QuadTreeValue.fromTuple),
      opts.idealCount,
      opts.minSize,
      opts.maxDepth
    )

  def insert(location: S, value: T, idealCount: Int, minSize: Double, maxDepth: Int): QuadTree[S, T] =
    QuadTree.insert(this, QuadTreeValue(location, value), idealCount, minSize, maxDepth, 0)
  def insert(idealCount: Int, minSize: Double, maxDepth: Int)(elements: (S, T)*): QuadTree[S, T] =
    QuadTree.insert(this, Batch.fromSeq(elements).map(QuadTreeValue.fromTuple), idealCount, minSize, maxDepth)
  def insert(elements: Batch[(S, T)], idealCount: Int, minSize: Double, maxDepth: Int): QuadTree[S, T] =
    QuadTree.insert(this, elements.map(QuadTreeValue.fromTuple), idealCount, minSize, maxDepth)

//
  def toBatch(using CanEqual[T, T]): Batch[QuadTreeValue[S, T]] =
    QuadTree.toBatch(this, _ => true)
  def toBatch(p: T => Boolean)(using CanEqual[T, T]): Batch[QuadTreeValue[S, T]] =
    QuadTree.toBatch(this, p)

  def prune: QuadTree[S, T] =
    QuadTree.prune(this)

  def findClosestTo(vertex: Vertex)(using CanEqual[T, T], SpatialOps[S]): Option[QuadTreeValue[S, T]] =
    QuadTree.findClosestTo(vertex, this)
  def findClosestTo(vertex: Vertex, filter: T => Boolean)(using
      CanEqual[T, T],
      SpatialOps[S]
  ): Option[QuadTreeValue[S, T]] =
    QuadTree.findClosestTo(vertex, filter, this)

  def searchByLine(start: Vertex, end: Vertex)(using CanEqual[T, T]): Batch[QuadTreeValue[S, T]] =
    QuadTree.searchByLine(this, LineSegment(start, end))
  def searchByLine(start: Vertex, end: Vertex, filter: T => Boolean)(using CanEqual[T, T]): Batch[QuadTreeValue[S, T]] =
    QuadTree.searchByLine(this, LineSegment(start, end), filter)
  def searchByLine(line: LineSegment)(using CanEqual[T, T]): Batch[QuadTreeValue[S, T]] =
    QuadTree.searchByLine(this, line)
  def searchByLine(line: LineSegment, filter: T => Boolean)(using CanEqual[T, T]): Batch[QuadTreeValue[S, T]] =
    QuadTree.searchByLine(this, line, filter)

  def searchByBoundingBox(boundingBox: BoundingBox)(using CanEqual[T, T]): Batch[QuadTreeValue[S, T]] =
    QuadTree.searchByBoundingBox(this, boundingBox)
  def searchByBoundingBox(boundingBox: BoundingBox, filter: T => Boolean)(using
      CanEqual[T, T]
  ): Batch[QuadTreeValue[S, T]] =
    QuadTree.searchByBoundingBox(this, boundingBox, filter)

  def removeClosestTo(vertex: Vertex)(using CanEqual[T, T], SpatialOps[S]): QuadTree[S, T] =
    QuadTree.removeClosestTo(vertex, this)

  def removeByLine(start: Vertex, end: Vertex)(using CanEqual[T, T]): QuadTree[S, T] =
    QuadTree.removeByLine(this, LineSegment(start, end))
  def removeByLine(line: LineSegment)(using CanEqual[T, T]): QuadTree[S, T] =
    QuadTree.removeByLine(this, line)

  def removeByBoundingBox(boundingBox: BoundingBox)(using CanEqual[T, T]): QuadTree[S, T] =
    QuadTree.removeByBoundingBox(this, boundingBox)

  def prettyPrint: String =
    // Not tail recursive
    def rec(quadTree: QuadTree[S, T], indent: String): String =
      quadTree match
        case QuadTree.Empty(bounds) =>
          indent + s"Empty [${bounds.toString()}]"

        case QuadTree.Leaf(bounds, values) =>
          indent + s"Leaf [${bounds.toString()}] - ${values.map(v => v.location.toString() + " -> " + v.value.toString()).mkString("[", ", ", "]")}"

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

        case (QuadTree.Leaf(b1, v1) :: as, QuadTree.Leaf(b2, v2) :: bs)
            if (b1 ~== b2) && (v1.length == v2.length) && v1.zip(v2).forall { (a, b) =>
              s.equals(a.location, b.location) && a.value == b.value
            } =>
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

  trait InsertOptions:

    /** The ideal number of values in a quad bucket, defaults to 1. It is called "ideal" because you may have more or
      * less than this number depending on the circumstances of your tree. However, assuming some other limit hasn't
      * been hit, the ideal count is the point at which a leaf will split into a another quad branch.
      */
    val idealCount: Int

    /** The min size of a quad. */
    val minSize: Double

    /** The max depth to insert a value at. */
    val maxDepth: Int

  object InsertOptions:
    def apply(_idealCount: Int, _minSize: Double, _maxDepth: Int): InsertOptions =
      new InsertOptions:
        val idealCount: Int = _idealCount
        val minSize: Double = _minSize
        val maxDepth: Int   = _maxDepth

  def options(idealCount: Int, minSize: Double, maxDepth: Int): InsertOptions =
    InsertOptions(idealCount, minSize, maxDepth)

  val DefaultOptions: InsertOptions =
    InsertOptions(1, 1, 16)

  given [S, T](using CanEqual[T, T]): CanEqual[Option[QuadTree[S, T]], Option[QuadTree[S, T]]] = CanEqual.derived
  given [S, T](using CanEqual[T, T]): CanEqual[Batch[QuadTree[S, T]], Batch[QuadTree[S, T]]]   = CanEqual.derived

  def empty[S, T](width: Double, height: Double)(using SpatialOps[S]): QuadTree[S, T] =
    Empty(BoundingBox(0, 0, width, height))

  def empty[S, T](gridSize: Vertex)(using SpatialOps[S]): QuadTree[S, T] =
    Empty(BoundingBox(Vertex.zero, gridSize))

  def apply[S, T](elements: (S, T)*)(using SpatialOps[S], InsertOptions): QuadTree[S, T] =
    QuadTree(Batch.fromSeq(elements))
  def apply[S, T](elements: Batch[(S, T)])(using s: SpatialOps[S])(using InsertOptions): QuadTree[S, T] =
    val b =
      if elements.isEmpty then BoundingBox.zero
      else
        elements.tail.foldLeft(s.bounds(elements.head._1)) { case (acc, next) =>
          acc.expandToInclude(s.bounds(next._1))
        }

    Empty(b).insert(elements)

  object Leaf:
    def apply[S, T](bounds: BoundingBox, location: S, value: T)(using SpatialOps[S]): QuadTree.Leaf[S, T] =
      QuadTree.Leaf(bounds, Batch(QuadTreeValue(location, value)))

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

  def insert[S, T](
      quadTree: QuadTree[S, T],
      values: Batch[QuadTreeValue[S, T]],
      idealCount: Int,
      minSize: Double,
      maxDepth: Int
  )(using
      s: SpatialOps[S]
  ): QuadTree[S, T] =
    values.foldLeft(quadTree) { case (acc, next) => insert(acc, next, idealCount, minSize, maxDepth, 0) }

  def insert[S, T](
      quadTree: QuadTree[S, T],
      value: QuadTreeValue[S, T],
      idealCount: Int,
      minSize: Double,
      maxDepth: Int,
      depth: Int
  )(using
      s: SpatialOps[S]
  ): QuadTree[S, T] =
    quadTree match
      case Empty(bounds) if s.intersects(value.location, bounds) =>
        // Straight insert
        Leaf(bounds, Batch(value))

      case q: Empty[_, _] =>
        q

      case l @ Leaf(bounds, values) =>
        // Decide how to add the value to this leaf
        if s.surrounds(value.location, bounds) then l.copy(values = l.values :+ value)
        else if s.intersects(value.location, bounds) then
          // The pathological case here is when the leaf bounds left or top edge
          // is outside the value.bounds by some microscopic number, and so
          // halving the size doesn't make any difference, practically
          // infinitely, and certainly not before we stack overflow.
          // This should be dealt with by the max size / depth limits.

          if values.length < idealCount then l.copy(values = l.values :+ value)
          else if bounds.width / 2 < minSize || bounds.height / 2 < minSize then l.copy(values = l.values :+ value)
          else if depth + 1 > maxDepth then l.copy(values = l.values :+ value)
          else
            val x = insert(Branch.fromBounds(bounds), values, idealCount, minSize, maxDepth)
            insert(x, value, idealCount, minSize, maxDepth, depth + 1)
        else l

      case Branch(bounds, a, b, c, d) if s.intersects(value.location, bounds) =>
        // Delegate to sub-regions
        Branch[S, T](
          bounds,
          insert(a, value, idealCount, minSize, maxDepth, depth + 1),
          insert(b, value, idealCount, minSize, maxDepth, depth + 1),
          insert(c, value, idealCount, minSize, maxDepth, depth + 1),
          insert(d, value, idealCount, minSize, maxDepth, depth + 1)
        )

      case b: Branch[_, _] =>
        b

  def toBatch[S, T](quadTree: QuadTree[S, T], p: T => Boolean)(using CanEqual[T, T]): Batch[QuadTreeValue[S, T]] =
    @tailrec
    def rec(open: List[QuadTree[S, T]], acc: Batch[QuadTreeValue[S, T]]): Batch[QuadTreeValue[S, T]] =
      open match
        case Nil =>
          acc

        case x :: xs =>
          x match {
            case _: Empty[S, T] =>
              rec(xs, acc)

            case l: Leaf[S, T] =>
              val v = l.values.filter(v => p(v.value))
              rec(xs, v ++ acc)

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

  def findClosestTo[S, T](vertex: Vertex, p: T => Boolean, quadTree: QuadTree[S, T])(using CanEqual[T, T])(using
      s: SpatialOps[S]
  ): Option[QuadTreeValue[S, T]] =
    @tailrec
    def rec(
        remaining: List[QuadTree[S, T]],
        closestDistance: Double,
        acc: Option[QuadTreeValue[S, T]]
    ): Option[QuadTreeValue[S, T]] =
      remaining match
        case Nil =>
          acc

        case Leaf(_, values) :: rs =>
          val res: (Double, Option[QuadTreeValue[S, T]]) =
            values.foldLeft((closestDistance, acc)) { case (current, v) =>
              val dist = s.distance(v.location, vertex)
              if dist < current._1 && p(v.value) then dist -> Some(v) else current
            }

          rec(rs, res._1, res._2)

        case Branch(bounds, a, b, c, d) :: rs =>
          rec(a :: b :: c :: d :: rs, closestDistance, acc)

        case _ :: rs =>
          rec(rs, closestDistance, acc)

    rec(List(quadTree), Double.MaxValue, None)
  def findClosestTo[S, T](vertex: Vertex, quadTree: QuadTree[S, T])(using
      CanEqual[T, T],
      SpatialOps[S]
  ): Option[QuadTreeValue[S, T]] =
    findClosestTo(vertex, _ => true, quadTree)

  def searchByLine[S, T](quadTree: QuadTree[S, T], lineSegment: LineSegment, p: T => Boolean)(using
      CanEqual[T, T],
      SpatialOps[S]
  ): Batch[QuadTreeValue[S, T]] =
    @tailrec
    def rec(remaining: List[QuadTree[S, T]], acc: Batch[QuadTreeValue[S, T]]): Batch[QuadTreeValue[S, T]] =
      remaining match
        case Nil =>
          acc

        case Branch(bounds, a, b, c, d) :: rs =>
          if bounds.contains(lineSegment.start) || bounds.contains(lineSegment.end) ||
            bounds.lineIntersects(lineSegment)
          then rec(rs ++ List(a, b, c, d), acc)
          else rec(rs, acc)

        case Leaf(bounds, values) :: rs =>
          if bounds.contains(lineSegment.start) || bounds.contains(lineSegment.end) ||
            bounds.lineIntersects(lineSegment)
          then rec(rs, values.filter(v => p(v.value)) ++ acc)
          else rec(rs, acc)

        case _ :: rs =>
          rec(rs, acc)

    rec(List(quadTree), Batch.empty)
  def searchByLine[S, T](quadTree: QuadTree[S, T], lineSegment: LineSegment)(using
      CanEqual[T, T],
      SpatialOps[S]
  ): Batch[QuadTreeValue[S, T]] =
    searchByLine(quadTree, lineSegment, _ => true)

  def searchByBoundingBox[S, T](quadTree: QuadTree[S, T], boundingBox: BoundingBox, p: T => Boolean)(using
      CanEqual[T, T]
  )(using s: SpatialOps[S]): Batch[QuadTreeValue[S, T]] =
    @tailrec
    def rec(remaining: List[QuadTree[S, T]], acc: Batch[QuadTreeValue[S, T]]): Batch[QuadTreeValue[S, T]] =
      remaining match
        case Nil =>
          acc

        case Leaf(bounds, values) :: rs if boundingBox.overlaps(bounds) =>
          val res = values.filter(v => s.intersects(v.location, boundingBox) && p(v.value))
          rec(rs, res ++ acc)

        case Branch(bounds, a, b, c, d) :: rs if boundingBox.overlaps(bounds) =>
          rec(rs ++ List(a, b, c, d), acc)

        case _ :: rs =>
          rec(rs, acc)

    rec(List(quadTree), Batch.empty)
  def searchByBoundingBox[S, T](quadTree: QuadTree[S, T], boundingBox: BoundingBox)(using
      CanEqual[T, T],
      SpatialOps[S]
  ): Batch[QuadTreeValue[S, T]] =
    searchByBoundingBox(quadTree, boundingBox, _ => true)

  def removeClosestTo[S, T](vertex: Vertex, quadTree: QuadTree[S, T])(using CanEqual[T, T])(using
      s: SpatialOps[S]
  ): QuadTree[S, T] =
    def removeAt[T](quadTree: QuadTree[S, T], target: QuadTreeValue[S, T]): QuadTree[S, T] =
      quadTree match
        case l @ QuadTree.Leaf(bounds, values) if s.intersects(target.location, bounds) =>
          val newValues = values.filterNot(v => s.equals(v.location, target.location))
          if newValues.isEmpty then QuadTree.Empty(bounds) else l.copy(values = newValues)

        case QuadTree.Branch(bounds, a, b, c, d) if s.intersects(target.location, bounds) =>
          QuadTree.Branch[S, T](
            bounds,
            removeAt(a, target),
            removeAt(b, target),
            removeAt(c, target),
            removeAt(d, target)
          )

        case tree =>
          tree

    findClosestTo(vertex, quadTree) match
      case None =>
        quadTree

      case Some(target) =>
        removeAt(quadTree, target)

  def removeByLine[S, T](quadTree: QuadTree[S, T], lineSegment: LineSegment)(using
      CanEqual[T, T]
  )(using s: SpatialOps[S], ls: SpatialOps[LineSegment]): QuadTree[S, T] =
    def removeAt[T](quadTree: QuadTree[S, T]): QuadTree[S, T] =
      quadTree match
        case l @ QuadTree.Leaf(bounds, values) if ls.intersects(lineSegment, bounds) =>
          val newValues = values.filterNot { v =>
            s.intersects(v.location, lineSegment)
          }
          if newValues.isEmpty then QuadTree.Empty(bounds) else l.copy(values = newValues)

        case QuadTree.Branch(bounds, a, b, c, d) if ls.intersects(lineSegment, bounds) =>
          QuadTree.Branch[S, T](
            bounds,
            removeAt(a),
            removeAt(b),
            removeAt(c),
            removeAt(d)
          )

        case tree =>
          tree

    removeAt(quadTree)

  def removeByBoundingBox[S, T](quadTree: QuadTree[S, T], boundingBox: BoundingBox)(using
      CanEqual[T, T]
  )(using s: SpatialOps[S]): QuadTree[S, T] =
    def removeAt[T](quadTree: QuadTree[S, T]): QuadTree[S, T] =
      quadTree match
        case l @ QuadTree.Leaf(bounds, values) =>
          val newValues = values.filterNot { v =>
            s.intersects(v.location, boundingBox)
          }
          if newValues.isEmpty then QuadTree.Empty(bounds) else l.copy(values = newValues)

        case QuadTree.Branch(bounds, a, b, c, d) =>
          QuadTree.Branch[S, T](
            bounds,
            removeAt(a),
            removeAt(b),
            removeAt(c),
            removeAt(d)
          )

        case tree =>
          tree

    removeAt(quadTree)

final case class QuadTreeValue[S, T](location: S, value: T):
  def toTuple: (S, T) = location -> value
object QuadTreeValue:
  def fromTuple[S, T](t: (S, T)): QuadTreeValue[S, T] =
    QuadTreeValue(t._1, t._2)
