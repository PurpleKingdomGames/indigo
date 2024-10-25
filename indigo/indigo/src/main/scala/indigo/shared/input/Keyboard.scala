package indigo.shared.input

import indigo.shared.collections.Batch
import indigo.shared.constants.Key
import indigo.shared.events.KeyboardEvent

import scala.annotation.tailrec
import indigo.shared.constants.KeyCode

final class Keyboard(
    keyboardEvents: Batch[KeyboardEvent],
    val keysDown: Batch[KeyCode],
    val lastKeyHeldDown: Option[KeyCode]
) {

  lazy val keysReleased: Batch[KeyCode] = keyboardEvents.collect { case k: KeyboardEvent.KeyUp => k.key.code }

  def keysAreDown(keys: KeyCode*): Boolean = keys.forall(keyCode => keysDown.contains(keyCode))
  def keysAreUp(keys: KeyCode*): Boolean   = keys.forall(keyCode => keysReleased.contains(keyCode))

}
object Keyboard {

  val default: Keyboard =
    new Keyboard(Batch.empty, Batch.empty, None)

  def calculateNext(previous: Keyboard, events: Batch[KeyboardEvent]): Keyboard = {
    val keysDown = calculateKeysDown(events, previous.keysDown)

    new Keyboard(
      events,
      keysDown,
      keysDown.reverse.headOption
    )
  }

  private given CanEqual[Batch[KeyboardEvent], Batch[KeyboardEvent]] = CanEqual.derived

  def calculateKeysDown(keyboardEvents: Batch[KeyboardEvent], previousKeysDown: Batch[KeyCode]): Batch[KeyCode] = {
    @tailrec
    def rec(remaining: List[KeyboardEvent], keysDownAcc: List[KeyCode]): Batch[KeyCode] =
      remaining match {
        case Nil =>
          Batch.fromList(keysDownAcc.reverse)

        case KeyboardEvent.KeyDown(k) :: tl =>
          rec(tl, k.code :: keysDownAcc)

        case KeyboardEvent.KeyUp(k) :: tl =>
          rec(tl, keysDownAcc.filterNot(p => p == k.code))

        case _ => rec(remaining.tail, keysDownAcc)
      }

    rec(keyboardEvents.toList, previousKeysDown.reverse.toList)
  }

}
