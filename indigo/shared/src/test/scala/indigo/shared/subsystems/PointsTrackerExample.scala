package indigo.shared.subsystems

import indigo.shared.time.GameTime
import indigo.shared.Outcome
import indigo.shared.dice.Dice
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.{SceneUpdateFragment, Text}
import indigo.shared.datatypes.FontKey
import indigo.shared.events.InputState
import indigo.shared.FrameContext

final case class PointsTrackerExample(points: Int) extends SubSystem {
  type EventType = PointsTrackerEvent

  val eventFilter: GlobalEvent => Option[PointsTrackerEvent] = {
    case e: PointsTrackerEvent => Option(e)
    case _                     => None
  }

  def update(context: FrameContext): PointsTrackerEvent => Outcome[SubSystem] = {
    case PointsTrackerEvent.Add(pts) =>
      Outcome(this.copy(points = points + pts))

    case PointsTrackerEvent.LoseAll =>
      Outcome(this.copy(points = 0))
        .addGlobalEvents(GameOver)
  }

  def render(context: FrameContext): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addGameLayerNodes(Text(points.toString, 0, 0, 1, FontKey("")))
}

sealed trait PointsTrackerEvent extends GlobalEvent with Product with Serializable
object PointsTrackerEvent {
  case class Add(points: Int) extends PointsTrackerEvent
  case object LoseAll         extends PointsTrackerEvent
}

case object GameOver extends GlobalEvent
