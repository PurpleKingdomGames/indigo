package indigo.shared.input

import indigo.shared.collections.Batch
import indigo.shared.constants.Key
import indigo.shared.events.KeyboardEvent

import scala.annotation.tailrec

final class Keyboard(keyboardEvents: Batch[KeyboardEvent], val keysDown: Batch[Key], val lastKeyHeldDown: Option[Key]) {

  lazy val keysReleased: Batch[Key] = keyboardEvents.collect { case k: KeyboardEvent.KeyUp => k.keyCode }

  def keysAreDown(keys: Key*): Boolean = keys.forall(keyCode => keysDown.contains(keyCode))
  def keysAreUp(keys: Key*): Boolean   = keys.forall(keyCode => keysReleased.contains(keyCode))

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

  def calculateKeysDown(keyboardEvents: Batch[KeyboardEvent], previousKeysDown: Batch[Key]): Batch[Key] = {
    @tailrec
    def rec(remaining: List[KeyboardEvent], keysDownAcc: List[Key]): Batch[Key] =
      remaining match {
        case Nil =>
          Batch.fromList(keysDownAcc.reverse)

        case KeyboardEvent.KeyDown(k) :: tl =>
          rec(tl, k :: keysDownAcc)

        case KeyboardEvent.KeyUp(k) :: tl =>
          rec(tl, keysDownAcc.filterNot(p => p === k))
      }

    rec(keyboardEvents.toList, previousKeysDown.reverse.toList)
  }

}
