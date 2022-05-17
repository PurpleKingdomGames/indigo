package indigoexamples

import indigo._
import indigoextras.datatypes.Decreasing

final case class FloatingPoints(fontKey: FontKey) extends SubSystem {
  type EventType      = FloatingPointEvent
  type SubSystemModel = Batch[FloatingPointEntity]

  val id: SubSystemId =
    SubSystemId("floating points")

  val eventFilter: GlobalEvent => Option[FloatingPointEvent] = {
    case s: FloatingPointEvent.Spawn => Option(s)
    case FrameTick                   => Option(FloatingPointEvent.Update)
    case _                           => None
  }

  def initialModel: Outcome[Batch[FloatingPointEntity]] =
    Outcome(Batch.empty)

  def update(context: SubSystemFrameContext, entities: Batch[FloatingPointEntity]): FloatingPointEvent => Outcome[Batch[FloatingPointEntity]] = {
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

  val text: Text[Material.Bitmap] =
    Text("10", 0, 0, 1, fontKey, Material.Bitmap(AssetName(FontDetails.fontName))).alignCenter

  def present(context: SubSystemFrameContext, entities: Batch[FloatingPointEntity]): Outcome[SceneUpdateFragment] =
    entities
      .map { e =>
        FloatingPoints.modifier(e, text).at(context.gameTime.running)
      }
      .sequence
      .map(SceneUpdateFragment.apply)

}

object FloatingPoints {

  val modifier: (FloatingPointEntity, Text[_]) => Signal[Outcome[Text[_]]] =
    (seed, text) =>
      Signal { t =>
        Outcome(
          text.moveTo(
            seed.spawnedAt.x,
            seed.spawnedAt.y - ((t - seed.createdAt) * 30).toInt
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
