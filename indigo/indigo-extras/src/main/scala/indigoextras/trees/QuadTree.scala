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

  def insert(location: S, value: T): QuadTree[S, T] =
    QuadTree.insert(this, QuadTreeValue(location, value))
  def insert(elements: (S, T)*): QuadTree[S, T] =
    QuadTree.insert(this, Batch.fromSeq(elements).map(QuadTreeValue.fromTuple))
  def insert(elements: Batch[(S, T)]): QuadTree[S, T] =
    QuadTree.insert(this, elements.map(QuadTreeValue.fromTuple))

  def toBatch(using CanEqual[T, T]): Batch[QuadTreeValue[S, T]] =
    QuadTree.toBatch(this, _ => true)
  def toBatch(p: T => Boolean)(using CanEqual[T, T]): Batch[QuadTreeValue[S, T]] =
    QuadTree.toBatch(this, p)

  def prune: QuadTree[S, T] =
    QuadTree.prune(this)

  def findClosestTo(vertex: Vertex)(using CanEqual[T, T], SpatialOps[S]): Option[QuadTreeValue[S, T]] =
    QuadTree.findClosestTo(vertex, this)

  def searchByLine(start: Vertex, end: Vertex)(using CanEqual[T, T]): Batch[QuadTreeValue[S, T]] =
    QuadTree.searchByLine(this, start, end)
  def searchByLine(line: LineSegment)(using CanEqual[T, T]): Batch[QuadTreeValue[S, T]] =
    QuadTree.searchByLine(this, line)

  def searchByBoundingBox(boundingBox: BoundingBox)(using CanEqual[T, T]): Batch[QuadTreeValue[S, T]] =
    QuadTree.searchByBoundingBox(this, boundingBox)

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

  def insert[S, T](quadTree: QuadTree[S, T], values: Batch[QuadTreeValue[S, T]])(using
      s: SpatialOps[S]
  ): QuadTree[S, T] =
    values.foldLeft(quadTree) { case (acc, next) => insert(acc, next) }

  def insert[S, T](quadTree: QuadTree[S, T], value: QuadTreeValue[S, T])(using
      s: SpatialOps[S]
  ): QuadTree[S, T] =
    quadTree match
      case Empty(bounds) if s.within(value.location, bounds) =>
        // Straight insert
        Leaf(bounds, Batch(value))

      case Leaf(bounds, values) if s.within(value.location, bounds) =>
        // Both elements in the same region,
        // subdivide and insert both.
        insert(
          insert(Branch.fromBounds(bounds), values), // originals
          value                                      // new
        )

      case Branch(bounds, a, b, c, d) if s.within(value.location, bounds) =>
        // Delegate to sub-regions
        Branch[S, T](
          bounds,
          insert(a, value),
          insert(b, value),
          insert(c, value),
          insert(d, value)
        )

      case _ =>
        quadTree

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

  def findClosestTo[S, T](vertex: Vertex, quadTree: QuadTree[S, T], p: T => Boolean)(using CanEqual[T, T])(using
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

        case Branch(bounds, a, b, c, d) :: rs if vertex.distanceTo(bounds.center) < closestDistance =>
          rec(a :: b :: c :: d :: rs, closestDistance, acc)

        case _ :: rs =>
          rec(rs, closestDistance, acc)

    rec(List(quadTree), Double.MaxValue, None)
  def findClosestTo[S, T](vertex: Vertex, quadTree: QuadTree[S, T])(using
      CanEqual[T, T],
      SpatialOps[S]
  ): Option[QuadTreeValue[S, T]] =
    findClosestTo(vertex, quadTree, _ => true)

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
  def searchByLine[S, T](quadTree: QuadTree[S, T], start: Vertex, end: Vertex, p: T => Boolean)(using
      CanEqual[T, T],
      SpatialOps[S]
  ): Batch[QuadTreeValue[S, T]] =
    searchByLine(quadTree, LineSegment(start, end), p)
  def searchByLine[S, T](quadTree: QuadTree[S, T], start: Vertex, end: Vertex)(using
      CanEqual[T, T],
      SpatialOps[S]
  ): Batch[QuadTreeValue[S, T]] =
    searchByLine(quadTree, start, end, _ => true)

  def searchByBoundingBox[S, T](quadTree: QuadTree[S, T], boundingBox: BoundingBox, p: T => Boolean)(using
      CanEqual[T, T]
  )(using s: SpatialOps[S]): Batch[QuadTreeValue[S, T]] =
    @tailrec
    def rec(remaining: List[QuadTree[S, T]], acc: Batch[QuadTreeValue[S, T]]): Batch[QuadTreeValue[S, T]] =
      remaining match
        case Nil =>
          acc

        case Leaf(bounds, values) :: rs if boundingBox.overlaps(bounds) =>
          val res = values.filter(v => s.within(v.location, boundingBox) && p(v.value))
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

final case class QuadTreeValue[S, T](location: S, value: T):
  def toTuple: (S, T) = location -> value
object QuadTreeValue:
  def fromTuple[S, T](t: (S, T)): QuadTreeValue[S, T] =
    QuadTreeValue(t._1, t._2)
