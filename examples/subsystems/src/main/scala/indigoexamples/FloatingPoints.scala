package indigoexamples

import indigo._
import indigoextras.datatypes.TimeVaryingValue

final case class FloatingPoints(fontKey: FontKey) extends SubSystem {
  type EventType      = FloatingPointEvent
  type SubSystemModel = List[FloatingPointEntity]

  val eventFilter: GlobalEvent => Option[FloatingPointEvent] = {
    case s: FloatingPointEvent.Spawn => Option(s)
    case FrameTick                   => Option(FloatingPointEvent.Update)
    case _                           => None
  }

  def initialModel: List[FloatingPointEntity] =
    Nil

  def update(context: SubSystemFrameContext, entities: List[FloatingPointEntity]): FloatingPointEvent => Outcome[List[FloatingPointEntity]] = {
    case FloatingPointEvent.Spawn(position) =>
      Outcome(
        FloatingPointEntity(position, context.gameTime.running, TimeVaryingValue(2, context.gameTime.running)) :: entities
      )

    case FloatingPointEvent.Update =>
      Outcome(
        entities
          .map(_.update(context.gameTime.running))
          .filter(_.ttl.value > 0)
      )
  }

  val text: Text =
    Text("10", 0, 0, 1, fontKey).alignCenter

  def render(context: SubSystemFrameContext, entities: List[FloatingPointEntity]): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addUiLayerNodes(
        entities
          .map { e =>
            FloatingPoints.modifier(e, text).at(context.gameTime.running)
          }
          .sequence
          .state
      )
}

object FloatingPoints {

  val modifier: (FloatingPointEntity, Text) => Signal[Outcome[Text]] =
    (seed, text) =>
      Signal { t =>
        Outcome(
          text.moveTo(
            seed.spawnedAt.x,
            seed.spawnedAt.y - ((t - seed.createdAt).value * 30).toInt
          )
        )
      }

}

final case class FloatingPointEntity(spawnedAt: Point, createdAt: Seconds, ttl: TimeVaryingValue[Int]) {
  def update(runningTime: Seconds): FloatingPointEntity =
    this.copy(ttl = ttl.decrease(2, runningTime))
}

sealed trait FloatingPointEvent extends GlobalEvent
object FloatingPointEvent {
  final case class Spawn(point: Point) extends FloatingPointEvent
  final case object Update             extends FloatingPointEvent
}
