package indigoextras.waypoints

import indigo.Batch
import indigo.Radians
import indigo.Vertex

import scala.annotation.tailrec

/** Structure holding a set of positions (waypoints) to traverse in order with a method to calculate the expected
  * position. The constructor of this case class pre-calculates distances between waypoints so it's not recommended to
  * recreate it every frame unless its parameters change.
  *
  * @param waypoints
  *   list of positions to traverse through
  * @param proximityRadius
  *   distance from each waypoint where it can be considered traversed
  * @param looping
  *   whether the path ends at the last position of the batch or at the first
  */
final case class WaypointPath(waypoints: Batch[Vertex], proximityRadius: Double, looping: Boolean):
  def withProximityRadius(proximityRadius: Double): WaypointPath = this.copy(proximityRadius = proximityRadius)
  def withLooping(looping: Boolean): WaypointPath                = this.copy(looping = looping)
  def withLooping(waypoints: Batch[Vertex]): WaypointPath        = this.copy(waypoints = waypoints)

  /** The actual waypoints that will be traversed through if radius is greater than 0.0
    */
  val calculatedWaypoints: Batch[Vertex] =
    if proximityRadius > 0.0 then waypointsWithRadius(waypoints, proximityRadius, looping)
    else waypoints

  private val zippedWaypoints =
    if looping then
      calculatedWaypoints.zip(
        calculatedWaypoints.tail :+ calculatedWaypoints.head
      )
    else calculatedWaypoints.dropRight(1).zip(calculatedWaypoints.tail)

  private val waypointDistances = zippedWaypoints
    .map: (p1, p2) =>
      ((p1, p2), p1.distanceTo(p2))

  /** The full distance of the path
    */
  val pathLength: Double = waypointDistances.map(_._2).sum

  /** Calculates the position and direction in the path at a given relative point in time.
    *
    * @param at
    *   the relative point in time from 0.0 to 1.0. If looping is enabled values greater than 1.0 will loop and negative
    *   values will loop in reverse.
    * @return
    *   the position and direction at the specified relative point in time
    */
  def calculatePosition(
      at: Double
  ): WaypointPathPosition =
    val clampedAt = if looping then at % 1.0 else at.min(1.0)

    val positiveAt = if clampedAt < 0 then 1.0 + clampedAt else clampedAt

    val coveredDistance = positiveAt * pathLength

    findPathPosition(waypointDistances, coveredDistance, 0.0)

    // traverse the precalculated list of waypoints and the distances between them and finds where the expected position
  @tailrec
  private def findPathPosition(
      distances: Batch[((Vertex, Vertex), Double)],
      coveredDistance: Double,
      acc: Double
  ): WaypointPathPosition =
    val ((v1, v2), distance) = distances.head

    if (coveredDistance >= acc && coveredDistance <= acc + distance)
      val innerAt   = (coveredDistance - acc) / distance
      val position  = lerpVertex(v1, v2, innerAt)
      val direction = (v2 - v1).angle
      WaypointPathPosition(position, direction)
    else if (distances.size == 1)
      val position  = v2
      val direction = (v2 - v1).angle
      WaypointPathPosition(position, direction)
    else findPathPosition(distances.tail, coveredDistance, acc + distance)

  private def lerpDouble(start: Double, end: Double, at: Double): Double =
    start + ((end - start) * at)

  private def lerpVertex(start: Vertex, end: Vertex, at: Double): Vertex =
    Vertex(lerpDouble(start.x, end.x, at), lerpDouble(start.y, end.y, at))

  // Returns the effective waypoints to traverse if the original waypoint is considered visited within the supplied radius
  private def waypointsWithRadius(
      waypoints: Batch[Vertex],
      radius: Double,
      loop: Boolean
  ): Batch[Vertex] =
    val list =
      if loop then waypoints :+ waypoints.head
      else waypoints

    val distances = list
      .foldLeft(Batch.empty[Vertex]):
        case (acc, v2) =>
          acc.lastOption match
            case Some(v1) =>
              val fullDistance   = v1.distanceTo(v2)
              val cappedDistance = (fullDistance - radius).max(0.0)
              val newV2          = lerpVertex(v1, v2, cappedDistance / fullDistance)
              acc :+ newV2
            case None => Batch(v2)
    if loop then Batch(distances.last) ++ distances.tail.dropRight(1)
    else distances

object WaypointPath:

  def apply(waypoints: Batch[Vertex], proximityRadius: Double): WaypointPath =
    WaypointPath(waypoints, proximityRadius, looping = false)
  def apply(waypoints: Batch[Vertex], looping: Boolean): WaypointPath =
    WaypointPath(waypoints, proximityRadius = 0.0, looping)
  def apply(waypoints: Batch[Vertex]): WaypointPath =
    WaypointPath(waypoints, proximityRadius = 0.0, looping = false)

final case class WaypointPathPosition(position: Vertex, direction: Radians)
