package tyrian

import cats.effect.kernel.Async
import org.scalajs.dom.Event
import org.scalajs.dom.EventTarget
import util.Functions

import scala.annotation.nowarn
import scala.scalajs.js

final class TyrianIndigoBridge[F[_]: Async, A, Model]:

  val eventTarget: EventTarget = new EventTarget()

  def publish(value: A): Cmd[F, Nothing] =
    publishToBridge(None, value)
  def publish(indigoGame: IndigoGameId, value: A): Cmd[F, Nothing] =
    publishToBridge(Option(indigoGame), value)

  def subscribe[B](extract: A => Option[B])(using CanEqual[B, B]): Sub[F, B] =
    subscribeToBridge(None, extract)
  def subscribe[B](indigoGame: IndigoGameId)(extract: A => Option[B])(using CanEqual[B, B]): Sub[F, B] =
    subscribeToBridge(Option(indigoGame), extract)

  def subSystem: TyrianSubSystem[F, A, Model] =
    TyrianSubSystem(this)
  def subSystem(indigoGame: IndigoGameId): TyrianSubSystem[F, A, Model] =
    TyrianSubSystem(Option(indigoGame), this)

  @nowarn("msg=unused")
  private def publishToBridge(indigoGameId: Option[IndigoGameId], value: A): Cmd[F, Nothing] =
    Cmd.SideEffect {
      eventTarget.dispatchEvent(TyrianIndigoBridge.BridgeToIndigo(indigoGameId, value))
      ()
    }

  private def subscribeToBridge[B](indigoGameId: Option[IndigoGameId], extract: A => Option[B])(using
      CanEqual[B, B]
  ): Sub[F, B] =
    import TyrianIndigoBridge.BridgeToTyrian

    val eventExtract: BridgeToTyrian[A] => Option[B] = e =>
      indigoGameId match
        case None                       => extract(e.value)
        case id if e.indigoGameId == id => extract(e.value)
        case _                          => None

    val acquire = (callback: Either[Throwable, BridgeToTyrian[A]] => Unit) =>
      Async[F].delay {
        val listener = Functions.fun((a: BridgeToTyrian[A]) => callback(Right(a)))
        eventTarget.addEventListener(BridgeToTyrian.EventName, listener)
        listener
      }

    val release = (listener: js.Function1[BridgeToTyrian[A], Unit]) =>
      Async[F].delay(eventTarget.removeEventListener(BridgeToTyrian.EventName, listener))

    Sub.Observe(
      BridgeToTyrian.EventName + this.hashCode,
      acquire,
      release,
      eventExtract
    )

object TyrianIndigoBridge:

  def apply[F[_]: Async, A, Model](): TyrianIndigoBridge[F, A, Model] =
    new TyrianIndigoBridge[F, A, Model]()

  final class BridgeToIndigo[A](val indigoGameId: Option[IndigoGameId], val value: A)
      extends Event(BridgeToIndigo.EventName)
  object BridgeToIndigo:
    val EventName: String = "SendToIndigo"

    def unapply[A](e: BridgeToIndigo[A]): Option[(Option[IndigoGameId], A)] =
      Some((e.indigoGameId, e.value))

  final class BridgeToTyrian[A](val indigoGameId: Option[IndigoGameId], val value: A)
      extends Event(BridgeToTyrian.EventName)
  object BridgeToTyrian:
    val EventName: String = "SendToTyrian"

    def unapply[A](e: BridgeToTyrian[A]): Option[(Option[IndigoGameId], A)] =
      Some((e.indigoGameId, e.value))

opaque type IndigoGameId = String
object IndigoGameId:
  inline def apply(id: String): IndigoGameId    = id
  def unapply(id: IndigoGameId): Option[String] = Some(id)

  given CanEqual[IndigoGameId, IndigoGameId]                 = CanEqual.derived
  given CanEqual[Option[IndigoGameId], Option[IndigoGameId]] = CanEqual.derived
