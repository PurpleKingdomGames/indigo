package indigoextras.pathfinding

import indigo.*
import indigo.shared.dice.Dice
import indigoextras.pathfinding.PathBuilder.DefaultDiagonalCost
import indigoextras.pathfinding.PathBuilder.DefaultMaxHeuristicFactor
import indigoextras.pathfinding.PathBuilder.DefaultSideCost
import indigoextras.pathfinding.PathBuilder.Movements.*
import org.scalacheck.*

import scala.annotation.nowarn
import scala.annotation.tailrec
import scala.scalajs.js

import js.JSConverters.*

final case class TestContext(width: Int, height: Int, path: Batch[Point], allowedMoves: Batch[Point], dice: Dice) {
  def allowedPoints: Batch[Point] = path

  def impassablePoints: Batch[Point] =
    Batch.fromSeq((for {
      x <- 0 until width
      y <- 0 until height
      p = Point(x, y)
      if !path.contains(p)
    } yield p).toList)

  def weighted2DGrid: js.Array[js.Array[Int]] =
    val grid = Array.fill(height, width)(Int.MaxValue)
    path.foreach(p => grid(p.y)(p.x) = 15)
    grid.map(_.toJSArray).toJSArray

}

object TestContext {

  def build(width: Int, height: Int, allowedMoves: Batch[Point], dice: Dice): TestContext =
    val startX     = dice.roll(width) - 1
    val startY     = dice.roll(height) - 1
    val startPoint = Point(startX, startY)
    val path       = buildPath(width, height, allowedMoves, dice, Batch(startPoint), startPoint)
    TestContext(width, height, path.reverse, allowedMoves, dice)

  @tailrec
  private def buildPath(
      width: Int,
      height: Int,
      allowedMoves: Batch[Point],
      dice: Dice,
      path: Batch[Point],
      currentPosition: Point
  ): Batch[Point] =
    computeNextPosition(width, height, path, dice.shuffle(allowedMoves), currentPosition) match {
      case None    => path
      case Some(p) => buildPath(width, height, allowedMoves, dice, p :: path, p)
    }

  private def computeNextPosition(
      width: Int,
      height: Int,
      path: Batch[Point],
      allowedMoves: Batch[Point],
      currentPosition: Point
  ): Option[Point] =
    allowedMoves
      .map(_ + currentPosition)
      .filterNot(p => path.contains(p) || p.x < 0 || p.x >= width || p.y < 0 || p.y >= height)
      .take(1)
      .headOption

}

// simulate a user defined type to use for the path finder
final case class PointWithUserContext(point: Point, ctx: String) derives CanEqual

object PointWithUserContext:
  def fromPoint(p: Point): PointWithUserContext = PointWithUserContext(p, s"(${p.x},${p.y})")

@nowarn("msg=unused")
final class PathFinderTests extends Properties("PathFinder") {

  private def adjacent(p1: Point, p2: Point): Boolean = Math.abs(p1.x - p2.x) <= 1 && Math.abs(p1.y - p2.y) <= 1

  val genAllowedMoves: Gen[Batch[Point]] =
    Gen.oneOf(
      Vertical,
      Horizontal,
      Side,
      Diagonal,
      All
    )

  val genContext: Gen[TestContext] =
    for {
      width        <- Gen.choose(8, 64)
      height       <- Gen.choose(8, 64)
      allowedMoves <- genAllowedMoves
      dice         <- Gen.choose(1L, Long.MaxValue).map(Dice.fromSeed)
    } yield TestContext.build(width, height, allowedMoves, dice)

  val weightedGrid: Gen[(Int, Int, Dice, js.Array[Int])] = // width, height, dice, grid
    for {
      width  <- Gen.choose(4, 16)
      height <- Gen.choose(4, 16)
      dice   <- Gen.choose(0L, Long.MaxValue).map(Dice.fromSeed)
    } yield (width, height, dice, Array.fill(height * width)(dice.roll(Int.MaxValue) - 1).toJSArray)

  property("return Some(Batch(start)) when start and end are the same") = Prop.forAll(genContext) { context =>
    val start: Point = context.dice.shuffle(context.allowedPoints).head
    val pathBuilder: PathBuilder[Point] =
      PathBuilder.fromAllowedPoints(
        context.allowedPoints.toSet,
        context.allowedMoves,
        DefaultSideCost,
        DefaultDiagonalCost,
        DefaultMaxHeuristicFactor
      )
    PathFinder.findPath(start, start, pathBuilder) == Some(Batch(start))
  }

  property("return None when start and end are not connected") = Prop.forAll(genContext) { context =>
    val start: Point = context.dice.shuffle(context.allowedPoints).head
    val end: Point   = context.dice.shuffle(context.impassablePoints).head
    if (start != end && !context.allowedMoves.map(_ + start).contains(end))
      val newContext = context.copy(path = Batch(start, end))
      val pathBuilder: PathBuilder[Point] =
        PathBuilder.fromAllowedPoints(
          newContext.allowedPoints.toSet,
          newContext.allowedMoves,
          DefaultSideCost,
          DefaultDiagonalCost,
          DefaultMaxHeuristicFactor
        )
      PathFinder.findPath(start, end, pathBuilder) == None
    else true
  }

  property("return a path with a length <= to the generated one") = Prop.forAll(genContext) { context =>
    val start: Point = context.dice.shuffle(context.allowedPoints).head
    val end: Point   = context.dice.shuffle(context.allowedPoints).head
    val pathBuilder: PathBuilder[Point] =
      PathBuilder.fromAllowedPoints(
        context.allowedPoints.toSet,
        context.allowedMoves,
        DefaultSideCost,
        DefaultDiagonalCost,
        DefaultMaxHeuristicFactor
      )
    PathFinder.findPath(start, end, pathBuilder).fold(true)(_.length <= context.path.length)
  }

  property("return a path that is a subset of the generated one") = Prop.forAll(genContext) { context =>
    val start: Point = context.dice.shuffle(context.allowedPoints).head
    val end: Point   = context.dice.shuffle(context.allowedPoints).head
    val pathBuilder: PathBuilder[Point] =
      PathBuilder.fromAllowedPoints(
        context.allowedPoints.toSet,
        context.allowedMoves,
        DefaultSideCost,
        DefaultDiagonalCost,
        DefaultMaxHeuristicFactor
      )
    PathFinder.findPath(start, end, pathBuilder).fold(true)(p => p.forall(context.path.contains))
  }

  property("return a path with a length > 1 when start and end are connected") = Prop.forAll(genContext) { context =>
    val start: Point = context.dice.shuffle(context.allowedPoints).head
    val end: Point   = context.dice.shuffle(context.allowedPoints).head
    if (start != end)
      val pathBuilder: PathBuilder[Point] =
        PathBuilder.fromAllowedPoints(
          context.allowedPoints.toSet,
          context.allowedMoves,
          DefaultSideCost,
          DefaultDiagonalCost,
          DefaultMaxHeuristicFactor
        )
      PathFinder.findPath(start, end, pathBuilder).exists(_.length > 1)
    else true
  }

  property("not have duplicated entries") = Prop.forAll(genContext) { context =>
    val start: Point = context.dice.shuffle(context.allowedPoints).head
    val end: Point   = context.dice.shuffle(context.allowedPoints).head
    val pathBuilder: PathBuilder[Point] =
      PathBuilder.fromAllowedPoints(
        context.allowedPoints.toSet,
        context.allowedMoves,
        DefaultSideCost,
        DefaultDiagonalCost,
        DefaultMaxHeuristicFactor
      )
    PathFinder.findPath(start, end, pathBuilder).fold(true)(p => p.distinct.length == p.length)
  }

  property("return a batch of adjacent entries") = Prop.forAll(genContext) { context =>
    val start: Point = context.dice.shuffle(context.allowedPoints).head
    val end: Point   = context.dice.shuffle(context.allowedPoints).head
    val pathBuilder: PathBuilder[Point] =
      PathBuilder.fromAllowedPoints(
        context.allowedPoints.toSet,
        context.allowedMoves,
        DefaultSideCost,
        DefaultDiagonalCost,
        DefaultMaxHeuristicFactor
      )
    PathFinder.findPath(start, end, pathBuilder).fold(true) { path =>
      path.tail.foldLeft((true, path.head))((acc, current) => (acc._1 && adjacent(acc._2, current), current))._1
    }
  }

  property("build a path from the impassable points") = Prop.forAll(genContext) { context =>
    val start: Point = context.dice.shuffle(context.allowedPoints).head
    val end: Point   = context.dice.shuffle(context.allowedPoints).head
    val pathBuilder: PathBuilder[Point] = PathBuilder.fromImpassablePoints(
      context.impassablePoints.toSet,
      context.width,
      context.height,
      context.allowedMoves,
      DefaultSideCost,
      DefaultDiagonalCost,
      DefaultMaxHeuristicFactor
    )
    PathFinder
      .findPath(start, end, pathBuilder)
      .fold(false)(path =>
        path.head == start && path.last == end && path.length >= 1 && path.length <= context.path.length
      )
  }

  property("build a path from a weighted 2D grid") = Prop.forAll(genContext) { context =>
    val start: Point = context.dice.shuffle(context.allowedPoints).head
    val end: Point   = context.dice.shuffle(context.allowedPoints).head
    val pathBuilder: PathBuilder[Point] = PathBuilder.fromWeighted2DGrid(
      context.weighted2DGrid,
      context.width,
      context.height,
      context.allowedMoves,
      DefaultSideCost,
      DefaultDiagonalCost,
      DefaultMaxHeuristicFactor
    )
    PathFinder
      .findPath(start, end, pathBuilder)
      .fold(false)(path =>
        path.head == start && path.last == end && path.length >= 1 && path.length <= context.path.length
      )
  }

  property("produce the same result with weighted 1D grid and 2D grid") = Prop.forAll(genContext) { context =>
    val start: Point = context.dice.shuffle(context.allowedPoints).head
    val end: Point   = context.dice.shuffle(context.allowedPoints).head
    val a2DGrid      = context.weighted2DGrid
    val pathBuilder1: PathBuilder[Point] =
      PathBuilder.fromWeighted2DGrid(
        a2DGrid,
        context.width,
        context.height,
        context.allowedMoves,
        DefaultSideCost,
        DefaultDiagonalCost,
        DefaultMaxHeuristicFactor
      )
    val pathBuilder2: PathBuilder[Point] = PathBuilder.fromWeightedGrid(
      Batch(a2DGrid.flatten),
      context.width,
      context.height,
      context.allowedMoves,
      DefaultSideCost,
      DefaultDiagonalCost,
      DefaultMaxHeuristicFactor
    )
    PathFinder.findPath(start, end, pathBuilder1) == PathFinder.findPath(start, end, pathBuilder2)
  }

  property("properly handles weighted grid") = Prop.forAll(weightedGrid) { (width, height, dice, grid) =>
    // one dimensional grid last element coordinates
    val start                           = Point(0, 0)
    val end                             = Point(width - 1, height - 1)
    val pathBuilder: PathBuilder[Point] = PathBuilder.fromWeightedGrid(Batch(grid), width, height)

    PathFinder.findPath(start, end, pathBuilder) match {
      case Some(firstPath) =>
        // take a coordinate on the path (but not start or end) and make it impassable
        val impassablePoint: Point = dice.shuffle(firstPath.drop(1).dropRight(1)).head
        // since the js array is mutable, we can update it and call the previous path builder again
        grid(impassablePoint.y * width + impassablePoint.x) = Int.MaxValue
        val newPath = PathFinder.findPath(start, end, pathBuilder)
        // new path should be different from the first one
        newPath.fold(false)(p => p.head == start && p.last == end && p.length >= 1 && !p.contains(impassablePoint))
      case _ => false
    }
  }

  property("allow to find a path using a custom type") = Prop.forAll(genContext) { context =>
    val pathWithCustomTypes: Batch[PointWithUserContext] = context.path.map(PointWithUserContext.fromPoint)
    val start: PointWithUserContext                      = context.dice.shuffle(pathWithCustomTypes).head
    val end: PointWithUserContext                        = context.dice.shuffle(pathWithCustomTypes).head

    val pathBuilder: PathBuilder[PointWithUserContext] =
      new PathBuilder[PointWithUserContext]:
        def neighbours(t: PointWithUserContext): Batch[PointWithUserContext] =
          context.allowedMoves
            .map(_ + t.point)
            .flatMap(p => Batch.fromOption(pathWithCustomTypes.find(_.point == p).map(identity)))

        def distance(t1: PointWithUserContext, t2: PointWithUserContext): Int =
          if (t1.point.x == t2.point.x || t1.point.y == t2.point.y) PathBuilder.DefaultSideCost
          else PathBuilder.DefaultDiagonalCost

        def heuristic(t1: PointWithUserContext, t2: PointWithUserContext): Int =
          (Math.abs(t1.point.x - t2.point.x) + Math.abs(
            t1.point.y - t2.point.y
          )) * PathBuilder.DefaultMaxHeuristicFactor

    PathFinder
      .findPath(start, end, pathBuilder)
      .fold(false)(path =>
        path.head == start && path.last == end && path.length >= 1 && path.length <= context.path.length
      )
  }

}
