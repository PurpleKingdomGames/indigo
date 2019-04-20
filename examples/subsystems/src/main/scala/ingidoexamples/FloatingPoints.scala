package ingidoexamples

import indigo._
import indigoexts.subsystems.SubSystem

final case class FloatingPoints(fontKey: FontKey, entities: List[FloatingPointEntity]) extends SubSystem {
  type EventType = FloatingPointEvent

  val eventFilter: GlobalEvent => Option[FloatingPointEvent] = {
    case s: FloatingPointEvent.Spawn => Option(s)
    case FrameTick                   => Option(FloatingPointEvent.Update)
    case _                           => None
  }

  def update(gameTime: GameTime, dice: Dice): FloatingPointEvent => Outcome[SubSystem] = {
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

  def render(gameTime: GameTime): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addUiLayerNodes(
        entities
          .map { e =>
            FloatingPoints.modifier(e, Text("10", 0, 0, 1, fontKey).alignCenter).at(gameTime.running)
          }
          .sequence
          .state
      )

  def report: String =
    "Floating Points"

}

object FloatingPoints {

  val timeAndFloatingPointEntity: FloatingPointEntity => Signal[(Millis, FloatingPointEntity)] =
    seed => Signal.Time |*| Signal.fixed(seed)

  val timeShift: SignalFunction[(Millis, FloatingPointEntity), (Millis, FloatingPointEntity)] =
    SignalFunction(t => (t._1 - t._2.createdAt, t._2))

  val timeToSeconds: SignalFunction[(Millis, FloatingPointEntity), (Double, FloatingPointEntity)] =
    SignalFunction(t => (t._1.toDouble * 0.001d, t._2))

  val positionY: SignalFunction[(Double, FloatingPointEntity), Int] =
    SignalFunction(t => t._2.spawnedAt.y - (t._1 * 30).toInt)

  val signalPipeline: SignalFunction[(Millis, FloatingPointEntity), Int] =
    timeShift >>> timeToSeconds >>> positionY

  val signal: FloatingPointEntity => Signal[Int] =
    seed => timeAndFloatingPointEntity(seed) |> signalPipeline

  val modifier: (FloatingPointEntity, Text) => Signal[Outcome[Text]] =
    (seed, renderable) =>
      Signal.merge(signal(seed), Signal.fixed(renderable)) { (yPos, text) =>
        Outcome(text.moveTo(seed.spawnedAt.x, yPos))
      }

}

final case class FloatingPointEntity(spawnedAt: Point, createdAt: Millis, ttl: TimeVaryingValue[Int]) {
  def update(gameTime: GameTime): FloatingPointEntity =
    this.copy(ttl = ttl.decrease(2, gameTime.running))
}

sealed trait FloatingPointEvent extends GlobalEvent
object FloatingPointEvent {
  final case class Spawn(point: Point) extends FloatingPointEvent
  final case object Update             extends FloatingPointEvent
}
