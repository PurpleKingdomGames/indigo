package indigo.shared.subsystems

import indigo.shared.Outcome
import indigo.shared.assets.AssetName
import indigo.shared.datatypes.FontKey
import indigo.shared.events.GlobalEvent
import indigo.shared.materials.Material
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.scenegraph.Text

final case class PointsTrackerExample(num: Int, startingPoints: Int) extends SubSystem[Int] {
  type EventType      = PointsTrackerEvent
  type SubSystemModel = Int
  type ReferenceData  = Int

  val id: SubSystemId = SubSystemId("points-" + num.toString)

  val eventFilter: GlobalEvent => Option[PointsTrackerEvent] = {
    case e: PointsTrackerEvent => Option(e)
    case _                     => None
  }

  def reference(model: Int): Int =
    model

  def initialModel: Outcome[Int] =
    Outcome(startingPoints)

  def update(context: SubSystemContext[Int], points: Int): PointsTrackerEvent => Outcome[Int] = {
    case PointsTrackerEvent.Add(pts) =>
      Outcome(points + pts + context.reference)

    case PointsTrackerEvent.LoseAll =>
      Outcome(0)
        .addGlobalEvents(GameOver)
  }

  def present(context: SubSystemContext[Int], points: Int): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(Text(points.toString, 0, 0, FontKey(""), Material.Bitmap(AssetName("Testing"))))
    )
}

sealed trait PointsTrackerEvent extends GlobalEvent with Product with Serializable
object PointsTrackerEvent {
  case class Add(points: Int) extends PointsTrackerEvent
  case object LoseAll         extends PointsTrackerEvent
}

case object GameOver extends GlobalEvent
