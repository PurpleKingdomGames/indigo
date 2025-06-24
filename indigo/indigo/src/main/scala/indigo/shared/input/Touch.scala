package indigo.shared.input

import indigo.shared.events.PointerType

final class Touch(val pointers: Pointers) extends PointerState {
  val pointerType: Option[PointerType] = Some(PointerType.Touch)
}

object Touch:
  val default: Touch = Touch(Pointers.default)
