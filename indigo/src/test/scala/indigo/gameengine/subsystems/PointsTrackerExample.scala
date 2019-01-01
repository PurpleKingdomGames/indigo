package indigo.gameengine.subsystems

import indigo.gameengine.GameTime
import indigo.gameengine.events.GlobalEvent
import indigo.gameengine.scenegraph.{SceneUpdateFragment, Text}
import indigo.gameengine.scenegraph.datatypes.FontKey

final case class PointsTrackerExample(points: Int) extends SubSystem {
  type EventType = PointsTrackerEvent

  val eventFilter: GlobalEvent => Option[PointsTrackerEvent] = {
    case e: PointsTrackerEvent => Option(e)
    case _                     => None
  }

  def update(gameTime: GameTime): PointsTrackerEvent => UpdatedSubSystem = {
    case PointsTrackerEvent.Add(pts) =>
      UpdatedSubSystem(this.copy(points = points + pts))

    case PointsTrackerEvent.LoseAll =>
      UpdatedSubSystem(this.copy(points = 0))
        .addGlobalEvents(GameOver)
  }

  def render(gameTime: GameTime): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addGameLayerNodes(Text(points.toString, 0, 0, 1, FontKey("")))

  def report: String =
    s"""Points: $points"""
}

sealed trait PointsTrackerEvent extends GlobalEvent with Product with Serializable
object PointsTrackerEvent {
  case class Add(points: Int) extends PointsTrackerEvent
  case object LoseAll         extends PointsTrackerEvent
}

case object GameOver extends GlobalEvent
