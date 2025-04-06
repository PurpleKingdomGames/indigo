package indigoextras.waypoints

import indigo.Batch
import indigo.Radians
import indigo.Vertex
import indigo.syntax.toBatch

import scala.annotation.tailrec

/** Structure holding a set of positions (waypoints) to traverse in order with a method to calculate the expected
  * position. The constructor of this case class pre-calculates distances between waypoints so it's not recommended to
  * recreate it every frame unless its parameters change.
  *
  * @param waypoints
  *   list of positions to traverse through
  * @param config
  *   set of configurations
  */
final case class WaypointPath(waypoints: Batch[Vertex], config: WaypointPathConfig):

  def withWaypoints(waypoints: Batch[Vertex]): WaypointPath =
    this.copy(waypoints = waypoints)

  def withProximityRadius(proximityRadius: Double): WaypointPath =
    this.copy(config = config.copy(proximityRadius = proximityRadius))

  def withLooping(looping: Boolean): WaypointPath =
    this.copy(config = config.copy(looping = looping))

  /** The actual waypoints that will be traversed through if radius is greater than 0.0
    */
  val calculatedWaypoints: Batch[Vertex] =
    if config.proximityRadius > 0.0 then waypointsWithRadius(waypoints, config.proximityRadius, config.looping)
    else waypoints

  private val zippedWaypoints =
    if config.looping then
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
    val clampedAt = if config.looping then at % 1.0 else at.min(1.0)

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
    val loopedWaypoints =
      if loop then waypoints :+ waypoints.head
      else waypoints

    @tailrec
    def waypointsWithRadiusAux(remainingWaypoints: Batch[Vertex], acc: Batch[Vertex]): Batch[Vertex] =
      remainingWaypoints.headOption match
        case Some(nextWaypoint) =>
          val nextAcc = acc.lastOption match
            case Some(previousWaypoint) =>
              val fullDistance    = previousWaypoint.distanceTo(nextWaypoint)
              val cappedDistance  = (fullDistance - radius).max(0.0)
              val newNextWaypoint = lerpVertex(previousWaypoint, nextWaypoint, cappedDistance / fullDistance)
              acc :+ newNextWaypoint
            case None =>
              Batch(nextWaypoint)
          waypointsWithRadiusAux(remainingWaypoints.tail, nextAcc)
        case None =>
          acc

    val resolvedWaypoints = waypointsWithRadiusAux(loopedWaypoints, Batch.empty)

    if loop then Batch(resolvedWaypoints.last) ++ resolvedWaypoints.tail.dropRight(1)
    else resolvedWaypoints

object WaypointPath:
  def apply(waypoints: Batch[Vertex]): WaypointPath =
    WaypointPath(waypoints, WaypointPathConfig.default)

  def apply(waypoints: Vertex*): WaypointPath =
    WaypointPath(waypoints.toBatch, WaypointPathConfig.default)

/** Configuration parameters for the WaypointPath
  *
  * @param proximityRadius
  *   distance from each waypoint where it can be considered traversed
  * @param looping
  *   whether the path ends at the last position of the batch or at the first
  */
final case class WaypointPathConfig(proximityRadius: Double, looping: Boolean)
object WaypointPathConfig:
  val default: WaypointPathConfig = WaypointPathConfig(proximityRadius = 0.0, looping = false)

final case class WaypointPathPosition(position: Vertex, direction: Radians)
