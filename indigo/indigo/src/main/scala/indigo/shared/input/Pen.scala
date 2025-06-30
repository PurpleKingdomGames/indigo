package indigo.shared.input

import indigo.shared.events.PointerType

final class Pen(val pointers: Pointers) extends PointerState {
  val pointerType: Option[PointerType] = Some(PointerType.Pen)
}

object Pen:
  val default: Pen = Pen(Pointers.default)
