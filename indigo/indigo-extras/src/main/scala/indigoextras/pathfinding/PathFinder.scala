package indigoextras.pathfinding

import indigo.*

import scala.annotation.tailrec

// A* (A Star) inspired algorithm version allowing generic types and customisation of the path finding
object PathFinder:

  private type OpenList[T]   = Batch[PathProps[T]] // the open list is the list of the points to explore
  private type ClosedList[T] = Batch[PathProps[T]] // the closed list is the list of the points already explored

  /** The structure containing the properties of a point
    * @param value
    *   the underlying value (coordinate, point, etc.)
    * @param g
    *   the distance from start
    * @param h
    *   the heuristic (distance from end)
    * @param f
    *   the g + h
    * @param parent
    *   the parent point
    * @tparam T
    *   the type of the points
    */
  private case class PathProps[T](value: T, g: Int, h: Int, f: Int, parent: Option[PathProps[T]]) derives CanEqual

  /** Find a path from start to end using the A* algorithm
    * @param start
    *   the start point
    * @param end
    *   the end point
    * @param pathBuilder
    *   the structure allowing to customize the path finding and to build a path of type T
    * @tparam T
    *   the type of the points
    * @return
    *   the path from start to end if it exists
    */
  def findPath[T](start: T, end: T, pathBuilder: PathBuilder[T])(using CanEqual[T, T]): Option[Batch[T]] = {
    val startProps = PathProps(start, 0, pathBuilder.heuristic(start, end), pathBuilder.heuristic(start, end), None)
    val path       = loop[T](end, pathBuilder, Batch(startProps), Batch.empty)
    if (path.isEmpty && start != end) None else Some(path)
  }

  @tailrec
  private def loop[T](end: T, pathBuilder: PathBuilder[T], open: OpenList[T], closed: ClosedList[T])(using
      CanEqual[T, T]
  ): Batch[T] =
    if (open.isEmpty) Batch.empty
    else
      val current = open.minBy(_.f)
      if (current.value == end)
        // the end is reached, so we can build the path
        buildPath(current)
      else
        // the end is not reached, so we need to continue the search
        // we remove the current node from the open list
        // we add the current node to the closed list
        // we update the lists with the neighbours of the current node
        val tmpOpen   = open.filterNot(_ == current)
        val tmpClosed = current :: closed
        val (newOpen, newClosed) =
          pathBuilder
            .neighbours(current.value)
            .foldLeft((tmpOpen, tmpClosed))(updateWithNeighbours(end, pathBuilder, current))
        loop(end, pathBuilder, newOpen, newClosed)

  private def updateWithNeighbours[T](
      end: T,
      pathBuilder: PathBuilder[T],
      current: PathProps[T]
  )(using CanEqual[T, T]): ((OpenList[T], ClosedList[T]), T) => (OpenList[T], ClosedList[T]) =
    (openClosed: (OpenList[T], ClosedList[T]), neighbour: T) =>
      updateWithNeighbours(end, pathBuilder, openClosed._1, openClosed._2, current, neighbour)

  private def updateWithNeighbours[T](
      end: T,
      pathBuilder: PathBuilder[T],
      open: OpenList[T],
      closed: ClosedList[T],
      current: PathProps[T],
      neighbour: T
  )(using CanEqual[T, T]): (OpenList[T], ClosedList[T]) =
    if (closed.exists(_.value == neighbour)) (open, closed)
    else
      val g = current.g + pathBuilder.distance(current.value, neighbour)
      val h = pathBuilder.heuristic(neighbour, end)
      val f = g + h
      val newOpen =
        open.find(_.value == neighbour) match {
          case Some(oldProps) => // the neighbour is already in the open list
            if (f < oldProps.f) // we update the neighbour if the new path is better (according to the heuristic value)
              val newProps = PathProps(value = neighbour, g = g, h = h, f = f, parent = Some(current))
              newProps :: open.filterNot(_.value == neighbour)
            else open
          case None => // the neighbour is not in the open list, so we add it
            val newProps = PathProps(value = neighbour, g = g, h = h, f = f, parent = Some(current))
            newProps :: open
        }
      (newOpen, closed)

  // build the path from the end to the start
  private def buildPath[T](props: PathProps[T]): Batch[T] =
    @tailrec
    def loop(props: PathProps[T], acc: Batch[T]): Batch[T] =
      props.parent match {
        case Some(parent) => loop(parent, props.value :: acc)
        case None         => props.value :: acc
      }

    loop(props, Batch.empty)
