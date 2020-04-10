package indigoexamples

import indigo._
import indigoexts.subsystems.SubSystem

final case class PointsTrackerSubSystem(points: Int, fontKey: FontKey) extends SubSystem {
  type EventType = PointsTrackerEvent

  val eventFilter: GlobalEvent => Option[PointsTrackerEvent] = {
    case e: PointsTrackerEvent => Option(e)
    case _                     => None
  }

  def update(gameTime: GameTime, dice: Dice): PointsTrackerEvent => Outcome[SubSystem] = {
    case PointsTrackerEvent.Add(pts) =>
      Outcome(this.copy(points = points + pts))
  }

  def render(gameTime: GameTime): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addGameLayerNodes(Text(s"""Points: ${points.toString()}""", 10, 10, 1, fontKey))
}

sealed trait PointsTrackerEvent extends GlobalEvent with Product with Serializable
object PointsTrackerEvent {
  final case class Add(points: Int) extends PointsTrackerEvent
}
