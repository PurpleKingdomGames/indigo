package indigo.shared.input

import indigo.KeyCode
import indigo.shared.collections.Batch
import indigo.shared.constants.Key
import indigo.shared.events.KeyboardEvent

import scala.annotation.tailrec
import scala.annotation.targetName

final class Keyboard(keyboardEvents: Batch[KeyboardEvent], val keysDown: Batch[Key], val lastKeyHeldDown: Option[Key]) {

  lazy val keysReleased: Batch[Key] = keyboardEvents.collect { case k: KeyboardEvent.KeyUp => k.key }

  lazy val isMetaKeyDown: Boolean =
    keysDown.exists(k => k.code == KeyCode.MetaLeft || k.code == KeyCode.MetaRight)

  lazy val isShiftKeyDown: Boolean =
    keysDown.exists(k => k.code == KeyCode.ShiftLeft || k.code == KeyCode.ShiftRight)

  lazy val isCtrlKeyDown: Boolean =
    keysDown.exists(k => k.code == KeyCode.ControlLeft || k.code == KeyCode.ControlRight)

  lazy val isAltKeyDown: Boolean =
    keysDown.exists(k => k.code == KeyCode.AltLeft || k.code == KeyCode.AltRight)

  def keysAreDown(keys: Key*): Boolean = keys.forall(keyCode => keysDown.contains(keyCode))
  def keysAreUp(keys: Key*): Boolean   = keys.forall(keyCode => keysReleased.contains(keyCode))

  @targetName("keysAreDownKeyCodes")
  def keysAreDown(keys: KeyCode*): Boolean = keys.forall(keyCode => keysDown.exists(k => k.code == keyCode))

  @targetName("keysAreUpKeyCodes")
  def keysAreUp(keys: KeyCode*): Boolean = keys.forall(keyCode => keysReleased.exists(k => k.code == keyCode))

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

        case _ => rec(remaining.tail, keysDownAcc)
      }

    rec(keyboardEvents.toList, previousKeysDown.reverse.toList)
  }

}
