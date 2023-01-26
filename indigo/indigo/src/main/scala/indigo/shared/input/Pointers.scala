package indigo.shared.input

import indigo.shared.collections.Batch
import indigo.shared.events.PointerEvent

final class Pointers(
    pointerEvents: Batch[PointerEvent]
)

object Pointers:
    val default: Pointers = Pointers(Batch.empty)

    def calculateNext(previous: Pointers, events: Batch[PointerEvent]): Pointers =
        Pointers(events)