package tyrian

import cats.effect.kernel.Async
import indigo.shared.Outcome
import indigo.shared.collections.Batch
import indigo.shared.events.FrameTick
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.subsystems.SubSystem
import indigo.shared.subsystems.SubSystemFrameContext
import indigo.shared.subsystems.SubSystemId

import scala.collection.mutable

final case class TyrianSubSystem[F[_]: Async, A](indigoGameId: Option[IndigoGameId], bridge: TyrianIndigoBridge[F, A])
    extends SubSystem:

  val id: SubSystemId =
    SubSystemId(indigoGameId.map(id => "[TyrianSubSystem] " + id).getOrElse("[TyrianSubSystem] " + hashCode.toString))

  type EventType      = GlobalEvent
  type SubSystemModel = Unit

  def send(value: A): TyrianEvent.Send =
    TyrianEvent.Send(value)

  private val eventQueue: mutable.Queue[TyrianEvent.Receive] =
    new mutable.Queue[TyrianEvent.Receive]()

  bridge.eventTarget.addEventListener[TyrianIndigoBridge.BridgeToIndigo[A]](
    TyrianIndigoBridge.BridgeToIndigo.EventName,
    {
      case TyrianIndigoBridge.BridgeToIndigo(id, value) if id == indigoGameId =>
        eventQueue.enqueue(TyrianEvent.Receive(value))

      case _ =>
        ()
    }
  )

  def eventFilter: GlobalEvent => Option[EventType] =
    case FrameTick      => Some(TyrianSubSystemEnqueue)
    case e: TyrianEvent => Some(e)
    case _              => None

  def initialModel: Outcome[Unit] =
    Outcome(())

  def update(context: SubSystemFrameContext, model: Unit): GlobalEvent => Outcome[Unit] =
    case TyrianEvent.Send(value) =>
      bridge.eventTarget.dispatchEvent(TyrianIndigoBridge.BridgeToTyrian(indigoGameId, value))
      Outcome(model)

    case TyrianSubSystemEnqueue =>
      Outcome(model, Batch.fromSeq(eventQueue.dequeueAll(_ => true)))

    case _ =>
      Outcome(model)

  def present(context: SubSystemFrameContext, model: Unit): Outcome[SceneUpdateFragment] =
    Outcome(SceneUpdateFragment.empty)

  enum TyrianEvent extends GlobalEvent:
    case Send(value: A)    extends TyrianEvent
    case Receive(value: A) extends TyrianEvent

  case object TyrianSubSystemEnqueue extends GlobalEvent

object TyrianSubSystem:
  def apply[F[_]: Async, A](bridge: TyrianIndigoBridge[F, A]): TyrianSubSystem[F, A] =
    TyrianSubSystem(None, bridge)
