package indigo.shared.animation.timeline

import indigo.shared.collections.Batch

final case class TimelineAnimation[A](slots: Batch[TimeSlot[A]]):
  def compile: TimeSlot[A] =
    if slots.isEmpty then TimeSlot.start
    else slots.tail.foldLeft(slots.head) { case (acc, next) => acc andThen next }

object TimelineAnimation:
  def apply[A](slots: TimeSlot[A]*): TimelineAnimation[A] =
    TimelineAnimation(Batch.fromSeq(slots))
