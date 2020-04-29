package indigoexamples

import indigo._
import indigoexts.subsystems.SubSystem

final case class PointsTrackerSubSystem(points: Int, fontKey: FontKey) extends SubSystem {
  type EventType = Int

  val eventFilter: GlobalEvent => Option[Int] = {
    case e: PointsTrackerEvent.Add => Option(e.points)
    case _                         => None
  }

  def update(gameTime: GameTime, inputState: InputState, dice: Dice): Int => Outcome[SubSystem] = { additionalPoints =>
    Outcome(this.copy(points = points + additionalPoints))
  }

  def render(gameTime: GameTime): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addGameLayerNodes(Text(s"""Points: ${points.toString()}""", 10, 10, 1, fontKey))
}

sealed trait PointsTrackerEvent extends GlobalEvent with Product with Serializable
object PointsTrackerEvent {
  final case class Add(points: Int) extends PointsTrackerEvent
}
