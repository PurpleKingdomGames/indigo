package indigoextras.pathfinding

import indigo.*
import indigoextras.pathfinding.DefaultPathBuilders.Movements.*

import scala.scalajs.js

// simple path builder implementations based on Point
object DefaultPathBuilders:

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
    val Vertical: List[Point]   = List(Up, Down)
    val Horizontal: List[Point] = List(Right, Left)
    val Side: List[Point]       = Vertical ++ Horizontal
    val Diagonal: List[Point]   = List(UpRight, DownRight, DownLeft, UpLeft)
    val All: List[Point]        = Side ++ Diagonal

  // the common default values for A* algorithm
  val DefaultSideCost: Int           = 10
  val DefaultDiagonalCost: Int       = 14
  val DefaultMaxHeuristicFactor: Int = 10

  /** Builds a function that returns the neighbours of a point from a list of allowed movements and a filter on the
    * generated points
    * @param allowedMovements
    *   the allowed movements
    * @param pointsFilter
    *   a filter on the generated points (e.g. to filter impassable points, or points outside the grid)
    * @return
    *   a function that returns the neighbours of a point
    */
  def buildPointNeighbours(allowedMovements: List[Point], pointsFilter: Point => Boolean): Point => List[Point] =
    (p: Point) => allowedMovements.map(p + _).filter(pointsFilter)

  /** Builds a path finder builder from a set of allowed points
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
  @SuppressWarnings(Array("scalafix:DisableSyntax.defaultArgs"))
  def fromAllowedPoints(
      allowedPoints: Set[Point],
      allowedMovements: List[Point] = All,
      directSideCost: Int = DefaultSideCost,
      diagonalCost: Int = DefaultDiagonalCost,
      maxHeuristicFactor: Int = DefaultMaxHeuristicFactor
  ): PathBuilder[Point] =

    val buildNeighbours = buildPointNeighbours(allowedMovements, allowedPoints.contains)

    new PathBuilder[Point]:
      def neighbours(t: Point): List[Point] = buildNeighbours(t)

      def distance(t1: Point, t2: Point): Int =
        if (t1.x == t2.x || t1.y == t2.y) directSideCost else diagonalCost

      def heuristic(t1: Point, t2: Point): Int =
        (Math.abs(t1.x - t2.x) + Math.abs(t1.y - t2.y)) * maxHeuristicFactor

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
  @SuppressWarnings(Array("scalafix:DisableSyntax.defaultArgs"))
  def fromImpassablePoints(
      impassablePoints: Set[Point],
      width: Int,
      height: Int,
      allowedMovements: List[Point] = All,
      directSideCost: Int = DefaultSideCost,
      diagonalCost: Int = DefaultDiagonalCost,
      maxHeuristicFactor: Int = DefaultMaxHeuristicFactor
  ): PathBuilder[Point] =

    val neighboursFilter = (p: Point) =>
      p.x >= 0 && p.x < width && p.y >= 0 && p.y < height && !impassablePoints.contains(p)
    val buildNeighbours = buildPointNeighbours(allowedMovements, neighboursFilter)

    new PathBuilder[Point]:
      def neighbours(t: Point): List[Point] = buildNeighbours(t)

      def distance(t1: Point, t2: Point): Int =
        if (t1.x == t2.x || t1.y == t2.y) directSideCost else diagonalCost

      def heuristic(t1: Point, t2: Point): Int =
        (Math.abs(t1.x - t2.x) + Math.abs(t1.y - t2.y)) * maxHeuristicFactor

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
  @SuppressWarnings(Array("scalafix:DisableSyntax.defaultArgs"))
  def fromWeighted2DGrid(
      grid: js.Array[js.Array[Int]],
      width: Int,
      height: Int,
      allowedMovements: List[Point] = All,
      directSideCost: Int = DefaultSideCost,
      diagonalCost: Int = DefaultDiagonalCost,
      maxHeuristicFactor: Int = DefaultMaxHeuristicFactor
  ): PathBuilder[Point] =

    val neighboursFilter = (p: Point) =>
      p.x >= 0 && p.x < width && p.y >= 0 && p.y < height && grid(p.y)(p.x) != Int.MaxValue
    val buildNeighbours = buildPointNeighbours(allowedMovements, neighboursFilter)

    new PathBuilder[Point]:

      def neighbours(t: Point): List[Point] = buildNeighbours(t)

      def distance(t1: Point, t2: Point): Int =
        (if (t1.x == t2.x || t1.y == t2.y) directSideCost else diagonalCost) + grid(t2.y)(t2.x)

      def heuristic(t1: Point, t2: Point): Int =
        (Math.abs(t1.x - t2.x) + Math.abs(t1.y - t2.y)) * maxHeuristicFactor

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
  @SuppressWarnings(Array("scalafix:DisableSyntax.defaultArgs"))
  def fromWeightedGrid(
      grid: js.Array[Int],
      width: Int,
      height: Int,
      allowedMovements: List[Point] = All,
      directSideCost: Int = DefaultSideCost,
      diagonalCost: Int = DefaultDiagonalCost,
      maxHeuristicFactor: Int = DefaultMaxHeuristicFactor
  ): PathBuilder[Point] =
    val neighboursFilter = (p: Point) =>
      p.x >= 0 && p.x < width && p.y >= 0 && p.y < height && grid(p.y * width + p.x) != Int.MaxValue
    val buildNeighbours = buildPointNeighbours(allowedMovements, neighboursFilter)

    new PathBuilder[Point]:
      def neighbours(t: Point): List[Point] = buildNeighbours(t)

      def distance(t1: Point, t2: Point): Int =
        (if (t1.x == t2.x || t1.y == t2.y) directSideCost else diagonalCost) + grid(t2.y * width + t2.x)

      def heuristic(t1: Point, t2: Point): Int =
        (Math.abs(t1.x - t2.x) + Math.abs(t1.y - t2.y)) * maxHeuristicFactor
