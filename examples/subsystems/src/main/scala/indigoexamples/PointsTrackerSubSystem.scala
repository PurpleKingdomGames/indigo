package indigoexamples

import indigo._

final case class PointsTrackerSubSystem(fontKey: FontKey) extends SubSystem {
  type EventType      = Int
  type SubSystemModel = Int

  val eventFilter: GlobalEvent => Option[Int] = {
    case e: PointsTrackerEvent.Add => Option(e.points)
    case _                         => None
  }

  def initialModel: Int = 0

  def update(context: FrameContext, points: Int): Int => Outcome[Int] = { additionalPoints =>
    Outcome(points + additionalPoints)
  }

  def render(context: FrameContext, points: Int): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addGameLayerNodes(Text(s"""Points: ${points.toString()}""", 10, 10, 1, fontKey))
}

sealed trait PointsTrackerEvent extends GlobalEvent with Product with Serializable
object PointsTrackerEvent {
  final case class Add(points: Int) extends PointsTrackerEvent
}
