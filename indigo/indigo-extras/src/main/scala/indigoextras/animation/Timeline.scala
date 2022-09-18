package indigoextras.animation

import indigo.shared.collections.Batch
import indigo.shared.temporal.Signal
import indigo.shared.temporal.SignalFunction
import indigo.shared.time.Seconds

import scala.annotation.tailrec

final case class Timeline[A](slots: Batch[TimeSlot[A]]):

  def at(time: Seconds): A => Option[A] = subject =>
    @tailrec
    def rec(remaining: Batch[TimeSlot[A]], acc: Option[A]): Option[A] =
      if remaining.isEmpty then acc
      else
        val x  = remaining.head
        val xs = remaining.tail

        if x.within(time) then
          val value = acc.getOrElse(subject)
          val next  = (Signal.Time |> x.modifier(value)).at(time - x.start)
          rec(xs, Some(next))
        else rec(xs, acc)

    rec(slots, None)

object Timeline:
  def apply[A](timeSlots: TimeSlot[A]*): Timeline[A] =
    Timeline(Batch.fromSeq(timeSlots))

final case class TimeSlot[A](start: Seconds, end: Seconds, modifier: A => SignalFunction[Seconds, A]):
  def within(t: Seconds): Boolean =
    t >= start && t <= end
