package indigoexamples

import indigo._

final case class PointsTrackerSubSystem(fontKey: FontKey) extends SubSystem {
  type EventType      = Int
  type SubSystemModel = Int

  val id: SubSystemId =
    SubSystemId("points tracker")

  val eventFilter: GlobalEvent => Option[Int] = {
    case e: PointsTrackerEvent.Add => Option(e.points)
    case _                         => None
  }

  def initialModel: Outcome[Int] =
    Outcome(0)

  def update(context: SubSystemFrameContext, points: Int): Int => Outcome[Int] = { additionalPoints =>
    Outcome(points + additionalPoints)
  }

  def present(context: SubSystemFrameContext, points: Int): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(Text(s"""Points: ${points.toString()}""", 10, 10, 1, fontKey, Material.Bitmap(AssetName(FontDetails.fontName))))
    )
}

sealed trait PointsTrackerEvent extends GlobalEvent with Product with Serializable
object PointsTrackerEvent {
  final case class Add(points: Int) extends PointsTrackerEvent
}
