package indigoextras.waypoints

import indigo.Radians
import indigo.Vector2

import scala.annotation.tailrec

trait WaypointPath:
  def waypoints: List[Vector2]

  def effectiveWaypoints: List[Vector2]

  def loop: Boolean

  def calculatePosition(
      at: Double
  ): (Vector2, Radians)

object WaypointPath:
  def apply(
      waypoints: List[Vector2],
      radius: Double,
      loop: Boolean
  ): WaypointPath =

    val _effectiveWaypoints =
      if radius > 0.0 then waypointsWithRadius(waypoints, radius, loop)
      else waypoints

    val zippedWaypoints =
      if loop then
        _effectiveWaypoints.zip(
          _effectiveWaypoints.tail.appended(_effectiveWaypoints.head)
        )
      else _effectiveWaypoints.dropRight(1).zip(_effectiveWaypoints.tail)

    val distances = zippedWaypoints
      .map: (p1, p2) =>
        ((p1, p2), p1.distanceTo(p2))

    val fullDistance = distances.map(_._2).sum

    val _waypoints = waypoints
    val _loop      = loop

    new WaypointPath:
      override def waypoints: List[Vector2] = _waypoints

      override def effectiveWaypoints: List[Vector2] = _effectiveWaypoints

      override def loop: Boolean = _loop

      override def calculatePosition(
          at: Double
      ): (Vector2, Radians) =
        val clampedAt = if loop then at % 1.0 else at.min(1.0)

        val positiveAt = if clampedAt < 0 then 1.0 + clampedAt else clampedAt

        val coveredDistance = positiveAt * fullDistance

        findPathPosition(distances, coveredDistance, 0.0)

  // traverse the precalculated list of waypoints and the distances between them and finds where the expected position
  @tailrec
  private def findPathPosition(
      distances: List[((Vector2, Vector2), Double)],
      coveredDistance: Double,
      acc: Double
  ): (Vector2, Radians) =
    val ((v1, v2), distance) = distances.head

    if (coveredDistance >= acc && coveredDistance <= acc + distance)
      val innerAt   = (coveredDistance - acc) / distance
      val position  = lerpVector2(v1, v2, innerAt)
      val direction = (v2 - v1).angle
      (position, direction)
    else if (distances.size == 1)
      val position  = v2
      val direction = (v2 - v1).angle
      (position, direction)
    else findPathPosition(distances.tail, coveredDistance, acc + distance)

  private def lerpDouble(start: Double, end: Double, at: Double): Double =
    start + ((end - start) * at)

  private def lerpVector2(start: Vector2, end: Vector2, at: Double): Vector2 =
    Vector2(lerpDouble(start.x, end.x, at), lerpDouble(start.y, end.y, at))

  // Returns the effective waypoints to traverse if the original waypoint is considered visited within the supplied radius
  private def waypointsWithRadius(
      waypoints: List[Vector2],
      radius: Double,
      loop: Boolean
  ): List[Vector2] =
    val list =
      if loop then
        waypoints
          .appended(waypoints.head)
      else waypoints

    val distances = list
      .foldLeft(List.empty[Vector2]):
        case (acc, v2) =>
          acc.lastOption match
            case Some(v1) =>
              val fullDistance   = v1.distanceTo(v2)
              val cappedDistance = (fullDistance - radius).max(0.0)
              val newV2          = lerpVector2(v1, v2, cappedDistance / fullDistance)
              acc :+ newV2
            case None => List(v2)
    if loop then distances.tail.dropRight(1).prepended(distances.last)
    else distances
