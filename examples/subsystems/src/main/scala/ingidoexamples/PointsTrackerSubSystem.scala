package ingidoexamples

import indigo._

final case class PointsTrackerSubSystem(points: Int, fontKey: FontKey) extends SubSystem {
  type EventType = PointsTrackerEvent

  val eventFilter: GlobalEvent => Option[PointsTrackerEvent] = {
    case e: PointsTrackerEvent => Option(e)
    case _                     => None
  }

  def update(gameTime: GameTime): PointsTrackerEvent => UpdatedSubSystem = {
    case PointsTrackerEvent.Add(pts) =>
      UpdatedSubSystem(this.copy(points = points + pts))
  }

  def render(gameTime: GameTime): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addGameLayerNodes(Text(report, 10, 10, 1, fontKey))

  def report: String =
    s"""Points: $points"""
}

sealed trait PointsTrackerEvent extends GlobalEvent with Product with Serializable
object PointsTrackerEvent {
  final case class Add(points: Int) extends PointsTrackerEvent
}
