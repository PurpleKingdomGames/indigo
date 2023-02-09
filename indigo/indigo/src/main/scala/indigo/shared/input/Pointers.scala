package indigo.shared.input

import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.events.PointerEvent

final class Pointers(
    pointerEvents: Batch[PointerEvent],
    val position: Point
)

// TODO: gestures ??

object Pointers:
    val default: Pointers = 
        Pointers(pointerEvents = Batch.empty, position = Point.zero)

    def calculateNext(prev: Pointers, events: Batch[PointerEvent]): Pointers =
        Pointers(
            pointerEvents = events,
            position = lastPointerPosition(prev.position, events)
        )

    private def lastPointerPosition(prev: Point, events: Batch[PointerEvent]): Point = 
        events.collect { case e: PointerEvent.PointerMove => e.position }.lastOption.fold(prev)(identity)