package indigo.shared.trees

import indigo.shared.collections.Batch
import indigo.shared.datatypes.Size
import indigo.shared.geometry.BoundingBox
import indigo.shared.geometry.LineSegment
import indigo.shared.geometry.Vertex

import scala.annotation.tailrec

/** QuadTree is a data structure that allows you to store any value that exists in some space, e.g. a String at a Point
  * or a class instance in a BoundingBox, e.g. The space station occupies a large `BoundingCircle`. The only restriction
  * on what defines where something can be stored, is that it must have an implicit/given SpatialOps instance in scope.
  *
  * You will also need an implicit `QuadTree.InsertOptions` in scope. You can use the defaults by doing `given opts =
  * QuadTree.DefaultOptions`, however, the author's suspicion is that these are next to useless since the values are
  * highly context specific. To make your own, do `give opts = QuadTree.options(...)` and fill in the blanks,
  * considering carefully the size and nature of the data you plan to store. Try and set sensible limits to avoid
  * degenerate cases of, for instance, very deep trees.
  *
  * Remember, the point of a QuadTree is to make spatial search more efficient by allowing you to quickly cull areas
  * that don't contain anything interesting.
  *
  * Some points of interest about this implementation:
  *
  *   - It is valid for the same entry to exist in many parts of the tree. For example, if a LineSegment cuts across
  *     several quads, then all Quads it touches must contain that line segment's value at the leaf level.
  *   - The 'insert' and 'search' functions should be effient since they can cull/ignore uninteresting nodes.
  *   - The 'find' and the 'remove' functions are slower since they need to visit the whole tree to ensure all entries
  *     are removed.
  *   - Leaf nodes are buckets that can (and will, probably) store more than one value at any given time, how many they
  *     should aim to store is configured in the options previously mentioned. The idea is to allow you to quickly find
  *     the value in the general area you care about cheaply, before you do some more expensive operation on them.
  */
enum QuadTree[S, T](val isEmpty: Boolean)(using s: SpatialOps[S]) derives CanEqual:
  case Branch(
      bounds: BoundingBox,
      a: QuadTree[S, T],
      b: QuadTree[S, T],
      c: QuadTree[S, T],
      d: QuadTree[S, T]
  )(using SpatialOps[S]) extends QuadTree[S, T](a.isEmpty && b.isEmpty && c.isEmpty && d.isEmpty)

  /** Represents a quad probably containing values. */
  case Leaf(bounds: BoundingBox, values: Batch[QuadTreeValue[S, T]])(using SpatialOps[S])
      extends QuadTree[S, T](values.isEmpty)

  /** Represents four quads that may or may not contain values. */
  case Empty(bounds: BoundingBox)(using SpatialOps[S]) extends QuadTree[S, T](true)

  val bounds: BoundingBox

  /** Insert value T (e.g. a String) with spatial value S (e.g. a BoundingBox or a Point) into the QuadTree.
    * `QuadTree.InsertOptions` are supplied via implicit / given; construct using `given opts = QuadTree.options(..)`.
    */
  def insert(location: S, value: T)(using opts: QuadTree.InsertOptions): QuadTree[S, T] =
    QuadTree.insert(
      this,
      QuadTreeValue(location, value),
      opts.idealCount,
      opts.minSize,
      opts.maxDepth
    )

  /** Insert values T (e.g. a String) with corresponding spatial values S (e.g. a BoundingBox or a Point) into the
    * QuadTree. `QuadTree.InsertOptions` are supplied via implicit / given; construct using `given opts =
    * QuadTree.options(..)`.
    */
  def insert(values: (S, T)*)(using opts: QuadTree.InsertOptions): QuadTree[S, T] =
    QuadTree.insert(
      this,
      Batch.fromSeq(values).map(QuadTreeValue.fromTuple),
      opts.idealCount,
      opts.minSize,
      opts.maxDepth
    )

  /** Insert a Batch of values of T (e.g. a String) with corresponding spatial values S (e.g. a BoundingBox or a Point)
    * into the QuadTree. `QuadTree.InsertOptions` are supplied via implicit / given; construct using `given opts =
    * QuadTree.options(..)`.
    */
  def insert(values: Batch[(S, T)])(using opts: QuadTree.InsertOptions): QuadTree[S, T] =
    QuadTree.insert(
      this,
      values.map(QuadTreeValue.fromTuple),
      opts.idealCount,
      opts.minSize,
      opts.maxDepth
    )

  /** Insert value T (e.g. a String) with spatial value S (e.g. a BoundingBox or a Point) into the QuadTree with
    * explicit option values.
    */
  def insert(location: S, value: T, idealCount: Int, minSize: Double, maxDepth: Int): QuadTree[S, T] =
    QuadTree.insert(this, QuadTreeValue(location, value), idealCount, minSize, maxDepth)

  /** Insert values T (e.g. a String) with corresponding spatial values S (e.g. a BoundingBox or a Point) into the
    * QuadTree with explicit option values.
    */
  def insert(idealCount: Int, minSize: Double, maxDepth: Int)(values: (S, T)*): QuadTree[S, T] =
    QuadTree.insert(this, Batch.fromSeq(values).map(QuadTreeValue.fromTuple), idealCount, minSize, maxDepth)

  /** Insert a Batch of values of T (e.g. a String) with corresponding spatial values S (e.g. a BoundingBox or a Point)
    * into the QuadTree with explicit option values.
    */
  def insert(values: Batch[(S, T)], idealCount: Int, minSize: Double, maxDepth: Int): QuadTree[S, T] =
    QuadTree.insert(this, values.map(QuadTreeValue.fromTuple), idealCount, minSize, maxDepth)

  /** Return a `Batch` containing all the values in the `QuadTree`, please be aware that there will probably be
    * duplicate entries.
    */
  def toBatch(using CanEqual[T, T]): Batch[QuadTreeValue[S, T]] =
    QuadTree.toBatch(this)

  /** Simplifies the `QuadTree` by removing unused leaves and branches. */
  def prune: QuadTree[S, T] =
    QuadTree.prune(this)

  /** Traverses the whole tree to find the element that is closest to the vertex specified. Slower than `searchAt`, but
    * it will find a value assuming the tree has values in it.
    */
  def findClosestTo(vertex: Vertex)(using CanEqual[T, T], SpatialOps[S]): Option[QuadTreeValue[S, T]] =
    QuadTree.findClosestTo(this, vertex)

  /** Dives down to the leaf nodes directly under the vertex and returns it's values. Faster than `findClosestTo` as it
    * does not traverse the tree, but only returns what is in the final quad, and won't look outside it for values.
    */
  def searchAt(vertex: Vertex): Batch[QuadTreeValue[S, T]] =
    QuadTree.searchAt(this, vertex)

  /** Searches the tree for any values who's spatial value interset with the LineSegment. */
  def searchByLine(start: Vertex, end: Vertex)(using CanEqual[T, T]): Batch[QuadTreeValue[S, T]] =
    QuadTree.searchByLine(this, LineSegment(start, end))

  /** Searches the tree for any values who's spatial value interset with the LineSegment. */
  def searchByLine(line: LineSegment)(using CanEqual[T, T]): Batch[QuadTreeValue[S, T]] =
    QuadTree.searchByLine(this, line)

  /** Searches the tree for any values who's spatial value interset with the BoundingBox. */
  def searchByBoundingBox(boundingBox: BoundingBox)(using CanEqual[T, T]): Batch[QuadTreeValue[S, T]] =
    QuadTree.searchByBoundingBox(this, boundingBox)

  /** Traverses the whole tree to find the element that is closest to the vertex specified, and removes it. Slower than
    * `removeAt`, but it will find a value to remove, assuming the tree has values in it. On removal, all instances of
    * that value are deleted from the tree.
    */
  def removeClosestTo(vertex: Vertex)(using CanEqual[T, T], SpatialOps[S]): QuadTree[S, T] =
    QuadTree.removeClosestTo(this, vertex)

  /** Removes the values at the quad directly under the vertex, if there is any. Note that while this is fast, it only
    * works well for simple cases, or trees of vertices / points. This is because it does not visit the whole tree
    * looking for other quads that might share ownership of this value. For example, if you add a bounding box the size
    * of the whole tree, but then use `removeAt` to remove it's value in just one quad, say the top left quad, the box
    * will continue to exist in all the other quads it touched.
    */
  def removeAt(vertex: Vertex): QuadTree[S, T] =
    QuadTree.removeAt(this, vertex)

  /** Removes any values who's spatial value intersect with the LineSegment. */
  def removeByLine(start: Vertex, end: Vertex)(using CanEqual[T, T]): QuadTree[S, T] =
    QuadTree.removeByLine(this, LineSegment(start, end))

  /** Removes any values who's spatial value intersect with the LineSegment. */
  def removeByLine(line: LineSegment)(using CanEqual[T, T]): QuadTree[S, T] =
    QuadTree.removeByLine(this, line)

  /** Removes any values who's spatial value interset with the BoundingBox. */
  def removeByBoundingBox(boundingBox: BoundingBox)(using CanEqual[T, T]): QuadTree[S, T] =
    QuadTree.removeByBoundingBox(this, boundingBox)

  /** Filter's out any matching value in the tree.
    */
  def filter(p: QuadTreeValue[S, T] => Boolean): QuadTree[S, T] =
    QuadTree.filter(this, p)

  /** Filters the values at the quad directly under the vertex, if there is any. Note that while this is fast, it only
    * works well for simple cases, or trees of vertices / points. This is because it does not visit the whole tree
    * looking for other quads that might share ownership of this value. For example, if you add a bounding box the size
    * of the whole tree, but then use `filterAt` to filter it's value in just one quad, say the top left quad, the box
    * will continue to exist in all the other quads it touched.
    */
  def filterAt(vertex: Vertex, p: QuadTreeValue[S, T] => Boolean): QuadTree[S, T] =
    QuadTree.filterAt(this, vertex, p)

  /** Filters any values who's spatial value intersect with the LineSegment. */
  def filterByLine(start: Vertex, end: Vertex, p: QuadTreeValue[S, T] => Boolean)(using
      CanEqual[T, T]
  ): QuadTree[S, T] =
    QuadTree.filterByLine(this, LineSegment(start, end), p)

  /** Filters any values who's spatial value intersect with the LineSegment. */
  def filterByLine(line: LineSegment, p: QuadTreeValue[S, T] => Boolean)(using CanEqual[T, T]): QuadTree[S, T] =
    QuadTree.filterByLine(this, line, p)

  /** Filters any values who's spatial value interset with the BoundingBox. */
  def filterByBoundingBox(boundingBox: BoundingBox, p: QuadTreeValue[S, T] => Boolean)(using
      CanEqual[T, T]
  ): QuadTree[S, T] =
    QuadTree.filterByBoundingBox(this, boundingBox, p)

  /** Prints the tree as a string, with the levels indented. */
  def prettyPrint: String =
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

  /** Compare two trees for equality. */
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

  /** Compare two trees for inequality. */
  def !==(other: QuadTree[S, T])(using CanEqual[T, T]): Boolean =
    !(this === other)

object QuadTree:

  /** Represents the options used to guide the process of inserting a value into the Quadtree. */
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

  /** Represents the options used to guide the process of inserting a value into the Quadtree. */
  object InsertOptions:
    def apply(_idealCount: Int, _minSize: Double, _maxDepth: Int): InsertOptions =
      new InsertOptions:
        val idealCount: Int = _idealCount
        val minSize: Double = _minSize
        val maxDepth: Int   = _maxDepth

  /** `QuadTree.options(...)` is a convenience method for making instances of `InsertOptions`, and some insert
    * operations require implicitly.
    */
  def options(idealCount: Int, minSize: Double, maxDepth: Int): InsertOptions =
    InsertOptions(idealCount, minSize, maxDepth)

  /** A default set of `InsertOptions`. Please note that these options are very use-case / context specific, so the
    * usefulness of these defaults should be doubted.
    */
  val DefaultOptions: InsertOptions =
    InsertOptions(1, 1, 16)

  given [S, T](using CanEqual[T, T]): CanEqual[Option[QuadTree[S, T]], Option[QuadTree[S, T]]] = CanEqual.derived
  given [S, T](using CanEqual[T, T]): CanEqual[Batch[QuadTree[S, T]], Batch[QuadTree[S, T]]]   = CanEqual.derived

  /** Construct an empty QuadTree of a given size. */
  def empty[S, T](width: Double, height: Double)(using SpatialOps[S]): QuadTree[S, T] =
    Empty(BoundingBox(0, 0, width, height))

  /** Construct an empty QuadTree of a given size. */
  def empty[S, T](size: Vertex)(using SpatialOps[S]): QuadTree[S, T] =
    Empty(BoundingBox(Vertex.zero, size))

  /** Construct an empty QuadTree of a given size. */
  def empty[S, T](size: Size)(using SpatialOps[S]): QuadTree[S, T] =
    Empty(BoundingBox(Vertex.zero, size.toVertex))

  /** Construct an empty QuadTree of a given bounds. */
  def empty[S, T](bounds: BoundingBox)(using SpatialOps[S]): QuadTree[S, T] =
    Empty(bounds)

  /** Construct a QuadTree from a repeated sequence of elements. */
  def apply[S, T](elements: (S, T)*)(using SpatialOps[S], InsertOptions): QuadTree[S, T] =
    QuadTree(Batch.fromSeq(elements))

  /** Construct a QuadTree from a Batch of elements. */
  def apply[S, T](elements: Batch[(S, T)])(using s: SpatialOps[S])(using InsertOptions): QuadTree[S, T] =
    val b =
      if elements.isEmpty then BoundingBox.zero
      else
        elements.tail.foldLeft(s.bounds(elements.head._1)) { case (acc, next) =>
          acc.expandToInclude(s.bounds(next._1))
        }

    Empty(b).insert(elements)

  /** Represents a quad probably containing values. */
  object Leaf:
    /** */
    def apply[S, T](bounds: BoundingBox, location: S, value: T)(using SpatialOps[S]): QuadTree.Leaf[S, T] =
      QuadTree.Leaf(bounds, Batch(QuadTreeValue(location, value)))

  /** Represents four quads that may or may not contain values. */
  object Branch:

    /** Create a QuadTree.Branch from it's bounds. */
    def fromBounds[S, T](bounds: BoundingBox)(using SpatialOps[S]): Branch[S, T] =
      fromBoundsAndQuads(bounds, subdivide(bounds))

    private[trees] def subdivide(quadBounds: BoundingBox): (BoundingBox, BoundingBox, BoundingBox, BoundingBox) =
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

    private def fromBoundsAndQuads[S, T](
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

  /** Insert a Batch of QuadTreeValue instances into the QuadTree with explicit option values.
    */
  def insert[S, T](
      quadTree: QuadTree[S, T],
      values: Batch[QuadTreeValue[S, T]],
      idealCount: Int,
      minSize: Double,
      maxDepth: Int
  )(using
      s: SpatialOps[S]
  ): QuadTree[S, T] =
    values.foldLeft(quadTree) { case (acc, next) => insertValue(acc, next, idealCount, minSize, maxDepth, 0) }

  /** Insert a single QuadTreeValue instance into the QuadTree with explicit option values.
    */
  def insert[S, T](
      quadTree: QuadTree[S, T],
      value: QuadTreeValue[S, T],
      idealCount: Int,
      minSize: Double,
      maxDepth: Int
  )(using
      s: SpatialOps[S]
  ): QuadTree[S, T] =
    insertValue(quadTree, value, idealCount, minSize, maxDepth, 0)

  private def insertValue[S, T](
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
            insertValue(x, value, idealCount, minSize, maxDepth, depth + 1)
        else l

      case Branch(bounds, a, b, c, d) if s.intersects(value.location, bounds) =>
        // Delegate to sub-regions
        Branch[S, T](
          bounds,
          insertValue(a, value, idealCount, minSize, maxDepth, depth + 1),
          insertValue(b, value, idealCount, minSize, maxDepth, depth + 1),
          insertValue(c, value, idealCount, minSize, maxDepth, depth + 1),
          insertValue(d, value, idealCount, minSize, maxDepth, depth + 1)
        )

      case b: Branch[_, _] =>
        b

  /** Return a `Batch` containing all the values in the `QuadTree`, please be aware that there will probably be
    * duplicate entries.
    */
  def toBatch[S, T](quadTree: QuadTree[S, T])(using CanEqual[T, T]): Batch[QuadTreeValue[S, T]] =
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
              rec(xs, l.values ++ acc)

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

  /** Simplifies the `QuadTree` by removing unused leaves and branches. */
  def prune[S, T](quadTree: QuadTree[S, T])(using SpatialOps[S]): QuadTree[S, T] =
    quadTree match
      case e: Empty[S, T] =>
        e

      case l: Leaf[S, T] if l.isEmpty =>
        Empty(l.bounds)

      case l: Leaf[S, T] =>
        l

      case b: Branch[S, T] if b.isEmpty =>
        Empty(b.bounds)

      case Branch(bounds, a, b, c, d) =>
        Branch[S, T](bounds, a.prune, b.prune, c.prune, d.prune)

  /** Traverses the whole tree to find the element that is closest to the vertex specified. Slower than `searchAt`, but
    * it will find a value assuming the tree has values in it.
    */
  def findClosestTo[S, T](quadTree: QuadTree[S, T], vertex: Vertex)(using
      CanEqual[T, T]
  )(using
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
              if dist < current._1 then dist -> Some(v) else current
            }

          rec(rs, res._1, res._2)

        case Branch(bounds, a, b, c, d) :: rs =>
          rec(a :: b :: c :: d :: rs, closestDistance, acc)

        case _ :: rs =>
          rec(rs, closestDistance, acc)

    rec(List(quadTree), Double.MaxValue, None)

  /** Dives down to the leaf nodes directly under the vertex and returns it's values. Faster than `findClosestTo` as it
    * does not traverse the tree, but only returns what is in the final quad, and won't look outside it for values.
    */
  def searchAt[S, T](quadTree: QuadTree[S, T], vertex: Vertex): Batch[QuadTreeValue[S, T]] =
    @tailrec
    def rec(remaining: List[QuadTree[S, T]], acc: Batch[QuadTreeValue[S, T]]): Batch[QuadTreeValue[S, T]] =
      remaining match
        case Nil =>
          acc

        case Branch(bounds, a, b, c, d) :: rs if bounds.contains(vertex) =>
          rec(rs ++ List(a, b, c, d), acc)

        case Leaf(bounds, values) :: rs if bounds.contains(vertex) =>
          rec(rs, values ++ acc)

        case _ :: rs =>
          rec(rs, acc)

    rec(List(quadTree), Batch.empty)

  /** Searches the tree for any values who's spatial value interset with the LineSegment. */
  def searchByLine[S, T](quadTree: QuadTree[S, T], lineSegment: LineSegment)(using
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
          then rec(rs, values ++ acc)
          else rec(rs, acc)

        case _ :: rs =>
          rec(rs, acc)

    rec(List(quadTree), Batch.empty)

  /** Searches the tree for any values who's spatial value interset with the BoundingBox. */
  def searchByBoundingBox[S, T](quadTree: QuadTree[S, T], boundingBox: BoundingBox)(using
      CanEqual[T, T]
  )(using s: SpatialOps[S]): Batch[QuadTreeValue[S, T]] =
    @tailrec
    def rec(remaining: List[QuadTree[S, T]], acc: Batch[QuadTreeValue[S, T]]): Batch[QuadTreeValue[S, T]] =
      remaining match
        case Nil =>
          acc

        case Leaf(bounds, values) :: rs if boundingBox.overlaps(bounds) =>
          val res = values.filter(v => s.intersects(v.location, boundingBox))
          rec(rs, res ++ acc)

        case Branch(bounds, a, b, c, d) :: rs if boundingBox.overlaps(bounds) =>
          rec(rs ++ List(a, b, c, d), acc)

        case _ :: rs =>
          rec(rs, acc)

    rec(List(quadTree), Batch.empty)

  /** Traverses the whole tree to find the element that is closest to the vertex specified, and removes it. Slower than
    * `removeAt`, but it will find a value to remove, assuming the tree has values in it. On removal, all instances of
    * that value are deleted from the tree.
    */
  def removeClosestTo[S, T](quadTree: QuadTree[S, T], vertex: Vertex)(using
      CanEqual[T, T]
  )(using
      s: SpatialOps[S]
  ): QuadTree[S, T] =
    def rec(quadTree: QuadTree[S, T], target: QuadTreeValue[S, T]): QuadTree[S, T] =
      quadTree match
        case l @ QuadTree.Leaf(bounds, values) if s.intersects(target.location, bounds) =>
          val newValues = values.filterNot(v => s.equals(v.location, target.location))
          if newValues.isEmpty then QuadTree.Empty(bounds) else l.copy(values = newValues)

        case QuadTree.Branch(bounds, a, b, c, d) if s.intersects(target.location, bounds) =>
          QuadTree.Branch[S, T](
            bounds,
            rec(a, target),
            rec(b, target),
            rec(c, target),
            rec(d, target)
          )

        case tree =>
          tree

    findClosestTo(quadTree, vertex) match
      case None =>
        quadTree

      case Some(target) =>
        rec(quadTree, target)

  /** Removes the value at the quad directly under the vertex, if there is one. Note that while this is fast, it only
    * works well for simple cases, or trees of vertices / points. This is because it does not visit the whole tree
    * looking for other quads that might share ownership of this value. For example, if you add a bounding box the size
    * of the whole tree, but then use `removeAt` to remove it's value in just one quad, say the top left quad, the box
    * will continue to exist in all the other quads it touched.
    */
  def removeAt[S, T](quadTree: QuadTree[S, T], vertex: Vertex)(using s: SpatialOps[S]): QuadTree[S, T] =
    def rec(quadTree: QuadTree[S, T]): QuadTree[S, T] =
      quadTree match
        case l @ QuadTree.Leaf(bounds, values) if bounds.contains(vertex) =>
          QuadTree.Empty(bounds)

        case QuadTree.Branch(bounds, a, b, c, d) if bounds.contains(vertex) =>
          QuadTree.Branch[S, T](
            bounds,
            rec(a),
            rec(b),
            rec(c),
            rec(d)
          )

        case tree =>
          tree

    rec(quadTree)

  /** Removes any values who's spatial value intersect with the LineSegment. */
  def removeByLine[S, T](quadTree: QuadTree[S, T], lineSegment: LineSegment)(using
      CanEqual[T, T]
  )(using s: SpatialOps[S], ls: SpatialOps[LineSegment]): QuadTree[S, T] =
    def rec(quadTree: QuadTree[S, T]): QuadTree[S, T] =
      quadTree match
        case l @ QuadTree.Leaf(bounds, values) =>
          val newValues = values.filterNot { v =>
            s.intersects(v.location, lineSegment)
          }
          if newValues.isEmpty then QuadTree.Empty(bounds) else l.copy(values = newValues)

        case QuadTree.Branch(bounds, a, b, c, d) =>
          QuadTree.Branch[S, T](
            bounds,
            rec(a),
            rec(b),
            rec(c),
            rec(d)
          )

        case tree =>
          tree

    rec(quadTree)

  /** Removes any values who's spatial value interset with the BoundingBox. */
  def removeByBoundingBox[S, T](quadTree: QuadTree[S, T], boundingBox: BoundingBox)(using
      CanEqual[T, T]
  )(using s: SpatialOps[S]): QuadTree[S, T] =
    def rec(quadTree: QuadTree[S, T]): QuadTree[S, T] =
      quadTree match
        case l @ QuadTree.Leaf(bounds, values) =>
          val newValues = values.filterNot { v =>
            s.intersects(v.location, boundingBox)
          }
          if newValues.isEmpty then QuadTree.Empty(bounds) else l.copy(values = newValues)

        case QuadTree.Branch(bounds, a, b, c, d) =>
          QuadTree.Branch[S, T](
            bounds,
            rec(a),
            rec(b),
            rec(c),
            rec(d)
          )

        case tree =>
          tree

    rec(quadTree)

  /** Filter's out any matching location and/or value in the tree.
    */
  def filter[S, T](quadTree: QuadTree[S, T], p: QuadTreeValue[S, T] => Boolean)(using
      s: SpatialOps[S]
  ): QuadTree[S, T] =
    def rec(quadTree: QuadTree[S, T]): QuadTree[S, T] =
      quadTree match
        case l @ QuadTree.Leaf(bounds, values) =>
          val newValues = values.filter(p)
          if newValues.isEmpty then QuadTree.Empty(bounds) else l.copy(values = newValues)

        case QuadTree.Branch(bounds, a, b, c, d) =>
          QuadTree.Branch[S, T](
            bounds,
            rec(a),
            rec(b),
            rec(c),
            rec(d)
          )

        case tree =>
          tree

    rec(quadTree)

  /** Filters the values at the quad directly under the vertex, if there is one. Note that while this is fast, it only
    * works well for simple cases, or trees of vertices / points. This is because it does not visit the whole tree
    * looking for other quads that might share ownership of this value. For example, if you add a bounding box the size
    * of the whole tree, but then use `filterAt` to filter it's value in just one quad, say the top left quad, the box
    * will continue to exist in all the other quads it touched.
    */
  def filterAt[S, T](quadTree: QuadTree[S, T], vertex: Vertex, p: QuadTreeValue[S, T] => Boolean)(using
      s: SpatialOps[S]
  ): QuadTree[S, T] =
    def rec(quadTree: QuadTree[S, T]): QuadTree[S, T] =
      quadTree match
        case l @ QuadTree.Leaf(bounds, values) if bounds.contains(vertex) =>
          val vs = values.filter(p)
          if vs.isEmpty then QuadTree.Empty(bounds) else l.copy(values = vs)

        case QuadTree.Branch(bounds, a, b, c, d) if bounds.contains(vertex) =>
          QuadTree.Branch[S, T](
            bounds,
            rec(a),
            rec(b),
            rec(c),
            rec(d)
          )

        case tree =>
          tree

    rec(quadTree)

  /** Filters any values who's spatial value intersect with the LineSegment. */
  def filterByLine[S, T](quadTree: QuadTree[S, T], lineSegment: LineSegment, p: QuadTreeValue[S, T] => Boolean)(using
      CanEqual[T, T]
  )(using s: SpatialOps[S], ls: SpatialOps[LineSegment]): QuadTree[S, T] =
    def rec(quadTree: QuadTree[S, T]): QuadTree[S, T] =
      quadTree match
        case l @ QuadTree.Leaf(bounds, values) =>
          val newValues = values.filter { v =>
            if s.intersects(v.location, lineSegment) then p(v) else true
          }
          if newValues.isEmpty then QuadTree.Empty(bounds) else l.copy(values = newValues)

        case QuadTree.Branch(bounds, a, b, c, d) =>
          QuadTree.Branch[S, T](
            bounds,
            rec(a),
            rec(b),
            rec(c),
            rec(d)
          )

        case tree =>
          tree

    rec(quadTree)

  /** Filters any values who's spatial value interset with the BoundingBox. */
  def filterByBoundingBox[S, T](quadTree: QuadTree[S, T], boundingBox: BoundingBox, p: QuadTreeValue[S, T] => Boolean)(
      using CanEqual[T, T]
  )(using s: SpatialOps[S]): QuadTree[S, T] =
    def rec(quadTree: QuadTree[S, T]): QuadTree[S, T] =
      quadTree match
        case l @ QuadTree.Leaf(bounds, values) =>
          val newValues = values.filter { v =>
            if s.intersects(v.location, boundingBox) then p(v) else true
          }
          if newValues.isEmpty then QuadTree.Empty(bounds) else l.copy(values = newValues)

        case QuadTree.Branch(bounds, a, b, c, d) =>
          QuadTree.Branch[S, T](
            bounds,
            rec(a),
            rec(b),
            rec(c),
            rec(d)
          )

        case tree =>
          tree

    rec(quadTree)

/** Holds the spatial location (e.g. a Vertex) and the value. */
final case class QuadTreeValue[S, T](location: S, value: T):
  /** Converts QuadTreeValue to a tuple of (S, T). */
  def toTuple: (S, T) = location -> value
object QuadTreeValue:
  /** Makes a QuadTreeValue from a tuple of (S, T). */
  def fromTuple[S, T](t: (S, T)): QuadTreeValue[S, T] =
    QuadTreeValue(t._1, t._2)
