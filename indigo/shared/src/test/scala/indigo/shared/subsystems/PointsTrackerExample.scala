package indigo.shared.subsystems

import indigo.shared.time.GameTime
import indigo.shared.Outcome
import indigo.shared.dice.Dice
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.{SceneUpdateFragment, Text}
import indigo.shared.datatypes.FontKey
import indigo.shared.events.InputState
import indigo.shared.subsystems.SubSystemFrameContext
import indigo.shared.datatypes.BindingKey

final case class PointsTrackerExample(startingPoints: Int) extends SubSystem {
  type EventType      = PointsTrackerEvent
  type SubSystemModel = Int

  val eventFilter: GlobalEvent => Option[PointsTrackerEvent] = {
    case e: PointsTrackerEvent => Option(e)
    case _                     => None
  }

  def initialModel: Int =
    startingPoints

  def update(context: SubSystemFrameContext, points: Int): PointsTrackerEvent => Outcome[Int] = {
    case PointsTrackerEvent.Add(pts) =>
      Outcome(points + pts)

    case PointsTrackerEvent.LoseAll =>
      Outcome(0)
        .addGlobalEvents(GameOver)
  }

  def present(context: SubSystemFrameContext, points: Int): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addGameLayerNodes(Text(points.toString, 0, 0, 1, FontKey("")))
}

sealed trait PointsTrackerEvent extends GlobalEvent with Product with Serializable
object PointsTrackerEvent {
  case class Add(points: Int) extends PointsTrackerEvent
  case object LoseAll         extends PointsTrackerEvent
}

case object GameOver extends GlobalEvent
