package indigoextras.datatypes

import indigo.shared.collections.Batch
import indigo.shared.datatypes.Circle
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Size
import indigo.shared.geometry.LineSegment
import indigoextras.utils.Bresenham

import scalajs.js
import js.JSConverters.*

/** SparseGrid is a collection for managing grid of values where grid squares may or may not actually contain a value.
  */
final class SparseGrid[A](
    val size: Size,
    values: Batch[js.UndefOr[A]]
):
  /** Returns the length of the grid, i.e. width * height */
  lazy val length: Int = size.width * size.height

  /** Put a value into the grid */
  def put(
      coords: Point,
      value: A
  ): SparseGrid[A] =
    new SparseGrid(
      size,
      values(SparseGrid.pointToIndex(coords, size.width)) = value
    )

  /** Put a batch of values into the grid */
  def put(newValues: Batch[(Point, A)]): SparseGrid[A] =
    val arr = values.toJSArray
    newValues.foreach { t =>
      val idx = SparseGrid.pointToIndex(t._1, size.width)
      val tt  = t._2

      if idx < length then arr(idx) = tt
    }

    new SparseGrid(size, Batch(arr))

  /** Put a batch of values into the grid, at an offset position */
  def put(values: Batch[(Point, A)], offset: Point): SparseGrid[A] =
    put(values.map(p => p._1 + offset -> p._2))

  /** Put repeating values into the grid */
  def put(values: (Point, A)*): SparseGrid[A] =
    put(Batch.fromSeq(values))

  /** Fill the grid with the given value */
  def fill(value: A): SparseGrid[A] =
    new SparseGrid[A](size, Batch.fill(size.width * size.height)(value))

  /** Get the value at the given coordinates */
  def get(coords: Point): Option[A] =
    val idx = SparseGrid.pointToIndex(coords, size.width)
    values(idx).toOption

  /** Remove the value at the given coordinates */
  def remove(coords: Point): SparseGrid[A] =
    new SparseGrid(
      size,
      values(SparseGrid.pointToIndex(coords, size.width)) = js.undefined
    )

  /** Empty the grid */
  def clear: SparseGrid[A] =
    SparseGrid(size)

  /** Returns all set values, guarantees order. */
  def toBatch: Batch[Option[A]] =
    values.map(_.toOption)

  /** Returns all set values and a default for any value that is not present, guarantees order. */
  def toBatch(default: A): Batch[A] =
    values.map(v => v.getOrElse(default))

  /** Returns all values in a given region. */
  @SuppressWarnings(Array("scalafix:DisableSyntax.while", "scalafix:DisableSyntax.var"))
  def toBatch(region: Rectangle): Batch[A] =
    val count = length
    var i     = 0
    var j     = 0
    val acc   = new js.Array[js.UndefOr[A]](length)

    while i < count do
      if region.contains(SparseGrid.indexToPoint(i, size.width)) then acc(j) = values(i)
      j += 1
      i += 1

    Batch(acc.collect { case p if p.isDefined => p.get })

  /** Returns all values in a given region, or the provided default for any missing values. */
  @SuppressWarnings(Array("scalafix:DisableSyntax.while", "scalafix:DisableSyntax.var"))
  def toBatch(region: Rectangle, default: A): Batch[A] =
    val count = length
    var i     = 0
    var j     = 0
    val acc   = new js.Array[js.UndefOr[A]](length)

    while i < count do
      if region.contains(SparseGrid.indexToPoint(i, size.width)) then
        val v = values(i)
        acc(j) = v.getOrElse(default)
      j += 1
      i += 1

    Batch(acc.collect { case p if p.isDefined => p.get })

  /** Returns all set values with their grid positions.
    */
  @SuppressWarnings(Array("scalafix:DisableSyntax.while", "scalafix:DisableSyntax.var"))
  def toPositionedBatch: Batch[(Point, A)] =
    val count = length
    var i     = 0
    var j     = 0
    val acc   = new js.Array[js.UndefOr[(Point, A)]](length)

    while i < count do
      val v  = values(i)
      val pt = SparseGrid.indexToPoint(i, size.width)
      if !js.isUndefined(v) then acc(j) = pt -> v.get
      j += 1
      i += 1

    Batch(acc.collect { case p if p.isDefined => p.get })

  /** Returns all values with their grid positions in a given region.
    */
  @SuppressWarnings(Array("scalafix:DisableSyntax.while", "scalafix:DisableSyntax.var"))
  def toPositionedBatch(region: Rectangle): Batch[(Point, A)] =
    val count = length
    var i     = 0
    var j     = 0
    val acc   = new js.Array[js.UndefOr[(Point, A)]](length)

    while i < count do
      val v  = values(i)
      val pt = SparseGrid.indexToPoint(i, size.width)
      if !js.isUndefined(v) && region.contains(pt) then acc(j) = pt -> v.get
      j += 1
      i += 1

    Batch(acc.collect { case p if p.isDefined => p.get })

  /** Alias for `combine`. Takes all of the 'set' entries of 'other' and puts them into this grid.
    */
  def |+|(other: SparseGrid[A]): SparseGrid[A] =
    combine(other)

  /** Takes all of the 'set' entries of 'other' and puts them into this grid. */
  def combine(other: SparseGrid[A]): SparseGrid[A] =
    put(other.toPositionedBatch)

  /** Combines two grids be insetting one within the other, at an offset. */
  def inset(other: SparseGrid[A], offset: Point): SparseGrid[A] =
    put(other.toPositionedBatch, offset)

  /** Modify a specific point on the grid */
  def modifyAt(coords: Point)(modifier: Option[A] => Option[A]): SparseGrid[A] =
    val idx = SparseGrid.pointToIndex(coords, size.width)
    val t   = values(idx)

    modifier(t.toOption) match
      case None        => remove(coords)
      case Some(value) => put(coords, value)

  /** Map over and modify all the points on the grid */
  def map(modifier: (Point, Option[A]) => Option[A]): SparseGrid[A] =
    new SparseGrid[A](
      size,
      values.zipWithIndex.map { case (v, i) =>
        val pt = SparseGrid.indexToPoint(i, size.width)
        modifier(pt, v.toOption).orUndefined
      }
    )

  /** Map over and modify all the points on the grid that are within the rectangle */
  def mapRectangle(region: Rectangle)(
      modifier: (Point, Option[A]) => Option[A]
  ): SparseGrid[A] =
    new SparseGrid[A](
      size,
      values.zipWithIndex.map { case (v, i) =>
        val pt = SparseGrid.indexToPoint(i, size.width)
        if region.contains(pt) then modifier(pt, v.toOption).orUndefined
        else v
      }
    )

  /** Fill a rectangle on the grid with a value */
  def fillRectangle(region: Rectangle, value: A): SparseGrid[A] =
    mapRectangle(region)((_, _) => Option(value))

  /** Map over and modify all the points on the grid that are within the circle */
  def mapCircle(circle: Circle)(modifier: (Point, Option[A]) => Option[A]): SparseGrid[A] =
    new SparseGrid[A](
      size,
      values.zipWithIndex.map { case (v, i) =>
        val pt = SparseGrid.indexToPoint(i, size.width)
        if circle.contains(pt) then modifier(pt, v.toOption).orUndefined
        else v
      }
    )

  /** Fill a circle on the grid with a value */
  def fillCircle(circle: Circle, value: A): SparseGrid[A] =
    mapCircle(circle)((_, _) => Option(value))

  /** Map over the grid squares on a line (bresenham) and modify */
  def mapLine(from: Point, to: Point)(
      modifier: (Point, Option[A]) => Option[A]
  ): SparseGrid[A] =
    val pts = Bresenham.line(from, to)
    new SparseGrid[A](
      size,
      values.zipWithIndex.map { case (v, i) =>
        val pt = SparseGrid.indexToPoint(i, size.width)
        if pts.contains(pt) then modifier(pt, v.toOption).orUndefined
        else v
      }
    )

  /** Map over the grid squares on a line (bresenham) and modify */
  def mapLine(line: LineSegment)(modifier: (Point, Option[A]) => Option[A]): SparseGrid[A] =
    mapLine(line.start.toPoint, line.end.toPoint)(modifier)

  /** Fill a line (bresenham) with a value */
  def fillLine(from: Point, to: Point, value: A): SparseGrid[A] =
    mapLine(from, to)((_, _) => Option(value))

  /** Fill a line (bresenham) with a value */
  def fillLine(line: LineSegment, value: A): SparseGrid[A] =
    mapLine(line.start.toPoint, line.end.toPoint)((_, _) => Option(value))

object SparseGrid:

  inline def pointToIndex(point: Point, gridWidth: Int): Int =
    point.x + (point.y * gridWidth)

  inline def indexToPoint(index: Int, gridWidth: Int): Point =
    Point(
      x = index % gridWidth,
      y = index / gridWidth
    )

  def apply[A](size: Size): SparseGrid[A] =
    new SparseGrid[A](
      size,
      Batch(new js.Array[A](size.width * size.height))
    )
