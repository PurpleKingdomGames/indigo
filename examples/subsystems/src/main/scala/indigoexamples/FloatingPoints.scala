package indigoexamples

import indigo._
import indigoexts.subsystems.SubSystem

final case class FloatingPoints(fontKey: FontKey, entities: List[FloatingPointEntity]) extends SubSystem {
  type EventType = FloatingPointEvent

  val eventFilter: GlobalEvent => Option[FloatingPointEvent] = {
    case s: FloatingPointEvent.Spawn => Option(s)
    case FrameTick                   => Option(FloatingPointEvent.Update)
    case _                           => None
  }

  def update(gameTime: GameTime, inputState: InputState, dice: Dice): FloatingPointEvent => Outcome[SubSystem] = {
    case FloatingPointEvent.Spawn(position) =>
      Outcome(
        this.copy(entities = FloatingPointEntity(position, gameTime.running, TimeVaryingValue(2, gameTime.running)) :: entities)
      )

    case FloatingPointEvent.Update =>
      Outcome(
        this.copy(
          entities = entities
            .map(_.update(gameTime))
            .filter(_.ttl.value > 0)
        )
      )
  }

  val text: Text =
    Text("10", 0, 0, 1, fontKey).alignCenter

  def render(gameTime: GameTime): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addUiLayerNodes(
        entities
          .map { e =>
            FloatingPoints.modifier(e, text).at(gameTime.running)
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
  def update(gameTime: GameTime): FloatingPointEntity =
    this.copy(ttl = ttl.decrease(2, gameTime.running))
}

sealed trait FloatingPointEvent extends GlobalEvent
object FloatingPointEvent {
  final case class Spawn(point: Point) extends FloatingPointEvent
  final case object Update             extends FloatingPointEvent
}
