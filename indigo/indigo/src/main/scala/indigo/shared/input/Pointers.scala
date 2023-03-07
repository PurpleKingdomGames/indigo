package indigo.shared.input

import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.events.PointerEvent

final class Pointers(
    val pointerEvents: Batch[PointerEvent],
    val position: Point
)

object Pointers:
  val default: Pointers =
    Pointers(Batch.empty, Point.zero)

  def calculateNext(previous: Pointers, events: Batch[PointerEvent]) =
    Pointers(
      pointerEvents = events,
      position = lastPointerPosition(previous.position, events)
    )

  private def lastPointerPosition(previous: Point, events: Batch[PointerEvent]): Point =
    events.collect { case mp: PointerEvent.PointerMove => mp.position }.lastOption.fold(previous)(identity)
