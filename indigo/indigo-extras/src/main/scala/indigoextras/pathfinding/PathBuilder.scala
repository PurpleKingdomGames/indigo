package indigoextras.pathfinding

import indigo.*
import indigoextras.pathfinding.PathBuilder.Movements.*

import scala.scalajs.js

/** The structure allowing to customize the path finding and to build a path of type T
  *
  * @tparam T
  *   the type of the points
  */
trait PathBuilder[T]:
  def neighbours(
      t: T
  ): Batch[T] // neighbours retrieval allows to select the allowed moves (horizontal, vertical, diagonal, impossible moves, jumps, etc.)
  def distance(t1: T, t2: T): Int // distance allows to select the cost of each move (diagonal, slow terrain, etc.)
  def heuristic(
      t1: T,
      t2: T
  ): Int // heuristic allows to select the way to estimate the distance from a point to the end

// simple path builder implementations based on Point
object PathBuilder:

  // defines the movements as relative points
  object Movements:
    // most common movements
    val Up: Point        = Point(0, -1)
    val Down: Point      = Point(0, 1)
    val Right: Point     = Point(1, 0)
    val Left: Point      = Point(-1, 0)
    val UpRight: Point   = Up + Right
    val DownRight: Point = Down + Right
    val DownLeft: Point  = Down + Left
    val UpLeft: Point    = Up + Left

    // most common movement groups
    val Vertical: Batch[Point]   = Batch(Up, Down)
    val Horizontal: Batch[Point] = Batch(Right, Left)
    val Side: Batch[Point]       = Vertical ++ Horizontal
    val Diagonal: Batch[Point]   = Batch(UpRight, DownRight, DownLeft, UpLeft)
    val All: Batch[Point]        = Side ++ Diagonal

  // the common default values for A* algorithm
  val DefaultSideCost: Int           = 10
  val DefaultDiagonalCost: Int       = 14
  val DefaultMaxHeuristicFactor: Int = 10

  /** Builds a function that returns the neighbours of a point from a list of allowed movements and a filter on the
    * generated points
    *
    * @param allowedMovements
    *   the allowed movements
    * @param pointsFilter
    *   a filter on the generated points (e.g. to filter impassable points, or points outside the grid)
    * @return
    *   a function that returns the neighbours of a point
    */
  def buildPointNeighbours(allowedMovements: Batch[Point], pointsFilter: Point => Boolean): Point => Batch[Point] =
    (p: Point) => allowedMovements.map(p + _).filter(pointsFilter)

  /** Builds a path finder builder from a set of allowed points
    *
    * @param allowedPoints
    *   the set of allowed points
    * @param allowedMovements
    *   the allowed movements
    * @param directSideCost
    *   the cost of a direct side movement
    * @param diagonalCost
    *   the cost of a diagonal movement
    * @param maxHeuristicFactor
    *   the maximum heuristic factor
    * @return
    *   a path finder builder
    */
  def fromAllowedPoints(
      allowedPoints: Set[Point],
      allowedMovements: Batch[Point],
      directSideCost: Int,
      diagonalCost: Int,
      maxHeuristicFactor: Int
  ): PathBuilder[Point] =

    val buildNeighbours = buildPointNeighbours(allowedMovements, allowedPoints.contains)

    new PathBuilder[Point]:
      def neighbours(t: Point): Batch[Point] = buildNeighbours(t)

      def distance(t1: Point, t2: Point): Int =
        if (t1.x == t2.x || t1.y == t2.y) directSideCost else diagonalCost

      def heuristic(t1: Point, t2: Point): Int =
        (Math.abs(t1.x - t2.x) + Math.abs(t1.y - t2.y)) * maxHeuristicFactor

  def fromAllowedPoints(allowedPoints: Set[Point]): PathBuilder[Point] =
    fromAllowedPoints(allowedPoints, All, DefaultSideCost, DefaultDiagonalCost, DefaultMaxHeuristicFactor)

  /** Builds a path finder builder from a set of impassable points
    *
    * @param impassablePoints
    *   the set of impassable points
    * @param width
    *   the width of the grid
    * @param height
    *   the height of the grid
    * @param allowedMovements
    *   the allowed movements
    * @param directSideCost
    *   the cost of a direct side movement
    * @param diagonalCost
    *   the cost of a diagonal movement
    * @param maxHeuristicFactor
    *   the maximum heuristic factor
    * @return
    *   a path finder builder
    */
  def fromImpassablePoints(
      impassablePoints: Set[Point],
      width: Int,
      height: Int,
      allowedMovements: Batch[Point],
      directSideCost: Int,
      diagonalCost: Int,
      maxHeuristicFactor: Int
  ): PathBuilder[Point] =

    val neighboursFilter = (p: Point) =>
      p.x >= 0 && p.x < width && p.y >= 0 && p.y < height && !impassablePoints.contains(p)
    val buildNeighbours = buildPointNeighbours(allowedMovements, neighboursFilter)

    new PathBuilder[Point]:
      def neighbours(t: Point): Batch[Point] = buildNeighbours(t)

      def distance(t1: Point, t2: Point): Int =
        if (t1.x == t2.x || t1.y == t2.y) directSideCost else diagonalCost

      def heuristic(t1: Point, t2: Point): Int =
        (Math.abs(t1.x - t2.x) + Math.abs(t1.y - t2.y)) * maxHeuristicFactor

  def fromImpassablePoints(impassablePoints: Set[Point], width: Int, height: Int): PathBuilder[Point] =
    fromImpassablePoints(
      impassablePoints,
      width,
      height,
      All,
      DefaultSideCost,
      DefaultDiagonalCost,
      DefaultMaxHeuristicFactor
    )

  /** Builds a path finder builder from a weighted 2D grid. Impassable points are represented by Int.MaxValue other
    * points are represented by their weight/ cost grid(y)(x) is the weight of the point (x, y)
    *
    * @param grid
    *   the weighted 2D grid
    * @param width
    *   the width of the grid
    * @param height
    *   the height of the grid
    * @param allowedMovements
    *   the allowed movements
    * @param directSideCost
    *   the cost of a direct side movement
    * @param diagonalCost
    *   the cost of a diagonal movement
    * @param maxHeuristicFactor
    *   the maximum heuristic factor
    * @return
    *   a path finder builder
    */
  def fromWeighted2DGrid(
      grid: js.Array[js.Array[Int]],
      width: Int,
      height: Int,
      allowedMovements: Batch[Point],
      directSideCost: Int,
      diagonalCost: Int,
      maxHeuristicFactor: Int
  ): PathBuilder[Point] =

    val neighboursFilter = (p: Point) =>
      p.x >= 0 && p.x < width && p.y >= 0 && p.y < height && grid(p.y)(p.x) != Int.MaxValue
    val buildNeighbours = buildPointNeighbours(allowedMovements, neighboursFilter)

    new PathBuilder[Point]:

      def neighbours(t: Point): Batch[Point] = buildNeighbours(t)

      def distance(t1: Point, t2: Point): Int =
        (if (t1.x == t2.x || t1.y == t2.y) directSideCost else diagonalCost) + grid(t2.y)(t2.x)

      def heuristic(t1: Point, t2: Point): Int =
        (Math.abs(t1.x - t2.x) + Math.abs(t1.y - t2.y)) * maxHeuristicFactor

  def fromWeighted2DGrid(grid: js.Array[js.Array[Int]], width: Int, height: Int): PathBuilder[Point] =
    fromWeighted2DGrid(grid, width, height, All, DefaultSideCost, DefaultDiagonalCost, DefaultMaxHeuristicFactor)

  /** Builds a path finder builder from a weighted 1D grid. Impassable points are represented by Int.MaxValue other
    * points are represented by their weight/ cost grid(y * width + x) is the weight of the point (x, y)
    *
    * @param grid
    *   the weighted 1D grid
    * @param width
    *   the width of the grid
    * @param height
    *   the height of the grid
    * @param allowedMovements
    *   the allowed movements
    * @param directSideCost
    *   the cost of a direct side movement
    * @param diagonalCost
    *   the cost of a diagonal movement
    * @param maxHeuristicFactor
    *   the maximum heuristic factor
    * @return
    *   a path finder builder
    */
  def fromWeightedGrid(
      grid: Batch[Int],
      width: Int,
      height: Int,
      allowedMovements: Batch[Point],
      directSideCost: Int,
      diagonalCost: Int,
      maxHeuristicFactor: Int
  ): PathBuilder[Point] =
    val neighboursFilter = (p: Point) =>
      p.x >= 0 && p.x < width && p.y >= 0 && p.y < height && grid(p.y * width + p.x) != Int.MaxValue
    val buildNeighbours = buildPointNeighbours(allowedMovements, neighboursFilter)

    new PathBuilder[Point]:
      def neighbours(t: Point): Batch[Point] = buildNeighbours(t)

      def distance(t1: Point, t2: Point): Int =
        (if (t1.x == t2.x || t1.y == t2.y) directSideCost else diagonalCost) + grid(t2.y * width + t2.x)

      def heuristic(t1: Point, t2: Point): Int =
        (Math.abs(t1.x - t2.x) + Math.abs(t1.y - t2.y)) * maxHeuristicFactor

  def fromWeightedGrid(grid: Batch[Int], width: Int, height: Int): PathBuilder[Point] =
    fromWeightedGrid(grid, width, height, All, DefaultSideCost, DefaultDiagonalCost, DefaultMaxHeuristicFactor)
