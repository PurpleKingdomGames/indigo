package indigoexamples

import indigo._
import indigoextras.datatypes.Decreasing

final case class FloatingPoints(fontKey: FontKey) extends SubSystem {
  type EventType      = FloatingPointEvent
  type SubSystemModel = List[FloatingPointEntity]

  val eventFilter: GlobalEvent => Option[FloatingPointEvent] = {
    case s: FloatingPointEvent.Spawn => Option(s)
    case FrameTick                   => Option(FloatingPointEvent.Update)
    case _                           => None
  }

  def initialModel: Outcome[List[FloatingPointEntity]] =
    Outcome(Nil)

  def update(context: SubSystemFrameContext, entities: List[FloatingPointEntity]): FloatingPointEvent => Outcome[List[FloatingPointEntity]] = {
    case FloatingPointEvent.Spawn(position) =>
      Outcome(
        FloatingPointEntity(position, context.gameTime.running, Decreasing(2, 1)) :: entities
      )

    case FloatingPointEvent.Update =>
      Outcome(
        entities
          .map(_.update(context.gameTime.delta))
          .filter(_.ttl.value > 0)
      )
  }

  val text: Text =
    Text("10", 0, 0, 1, fontKey).alignCenter

  def present(context: SubSystemFrameContext, entities: List[FloatingPointEntity]): Outcome[SceneUpdateFragment] =
    entities
      .map { e =>
        FloatingPoints.modifier(e, text).at(context.gameTime.running)
      }
      .sequence
      .map(SceneUpdateFragment.empty.addUiLayerNodes)

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

final case class FloatingPointEntity(spawnedAt: Point, createdAt: Seconds, ttl: Decreasing) {
  def update(timeDelta: Seconds): FloatingPointEntity =
    this.copy(ttl = ttl.update(timeDelta))
}

sealed trait FloatingPointEvent extends GlobalEvent
object FloatingPointEvent {
  final case class Spawn(point: Point) extends FloatingPointEvent
  case object Update                   extends FloatingPointEvent
}
