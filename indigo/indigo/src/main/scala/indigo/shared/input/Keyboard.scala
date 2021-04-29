package indigo.shared.input

import indigo.shared.events.KeyboardEvent
import indigo.shared.constants.Key
import scala.annotation.tailrec

final class Keyboard(keyboardEvents: List[KeyboardEvent], val keysDown: List[Key], val lastKeyHeldDown: Option[Key]) {

  lazy val keysReleased: List[Key] = keyboardEvents.collect { case k: KeyboardEvent.KeyUp => k.keyCode }

  def keysAreDown(keys: Key*): Boolean = keys.forall(keyCode => keysDown.contains(keyCode))
  def keysAreUp(keys: Key*): Boolean   = keys.forall(keyCode => keysReleased.contains(keyCode))

}
object Keyboard {

  val default: Keyboard =
    new Keyboard(Nil, Nil, None)

  def calculateNext(previous: Keyboard, events: List[KeyboardEvent]): Keyboard = {
    val keysDown = calculateKeysDown(events, previous.keysDown)

    new Keyboard(
      events,
      keysDown,
      keysDown.reverse.headOption
    )
  }

  private given CanEqual[List[KeyboardEvent], List[KeyboardEvent]] = CanEqual.derived

  def calculateKeysDown(keyboardEvents: List[KeyboardEvent], previousKeysDown: List[Key]): List[Key] = {
    @tailrec
    def rec(remaining: List[KeyboardEvent], keysDownAcc: List[Key]): List[Key] =
      remaining match {
        case Nil =>
          keysDownAcc.reverse

        case KeyboardEvent.KeyDown(k) :: tl =>
          rec(tl, k :: keysDownAcc)

        case KeyboardEvent.KeyUp(k) :: tl =>
          rec(tl, keysDownAcc.filterNot(p => p === k))

        case _ :: tl =>
          rec(tl, keysDownAcc)
      }

    rec(keyboardEvents, previousKeysDown.reverse)
  }

}
