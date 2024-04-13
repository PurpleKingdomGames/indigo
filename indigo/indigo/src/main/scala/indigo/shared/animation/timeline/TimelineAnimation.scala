package indigo.shared.animation.timeline

import indigo.shared.collections.Batch

opaque type TimelineAnimation[A] = Batch[TimeSlot[A]]

object TimelineAnimation:

  inline def apply[A](slots: TimeSlot[A]*): TimelineAnimation[A] =
    Batch.fromSeq(slots)

  inline def apply[A](slots: Batch[TimeSlot[A]]): TimelineAnimation[A] =
    slots

  extension [A](tl: TimelineAnimation[A])
    def compile: TimeSlot[A] =
      if tl.isEmpty then TimeSlot.start
      else tl.tail.foldLeft(tl.head) { case (acc, next) => acc `andThen` next }
