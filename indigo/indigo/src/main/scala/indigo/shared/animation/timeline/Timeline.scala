package indigo.shared.animation.timeline

import indigo.shared.collections.Batch
import indigo.shared.temporal.Signal
import indigo.shared.temporal.SignalFunction
import indigo.shared.time.Seconds

import scala.annotation.tailrec
import scala.annotation.targetName

opaque type Timeline[A] = Batch[TimeWindow[A]]

object Timeline:
  inline def apply[A](timeWindows: Batch[TimeWindow[A]]): Timeline[A] =
    timeWindows

  inline def apply[A](timeWindows: TimeWindow[A]*): Timeline[A] =
    Timeline(Batch.fromSeq(timeWindows))

  @targetName("timeline_apply_slots")
  inline def apply[A](timeSlots: TimeSlot[A]*): Timeline[A] =
    Timeline(Batch.fromSeq(timeSlots).flatMap(_.toWindows))

  @targetName("timeline_apply_animations")
  inline def apply[A](animations: TimelineAnimation[A]*): Timeline[A] =
    Timeline(Batch.fromSeq(animations).flatMap(_.compile.toWindows))

  def empty[A]: Timeline[A] =
    Timeline(Batch.empty[TimeWindow[A]])

  extension [A](tl: Timeline[A])
    def add(animation: TimeSlot[A]): Timeline[A] =
      addWindows(animation.toWindows)

    def addWindow(nextWindow: TimeWindow[A]): Timeline[A] =
      tl :+ nextWindow

    def addWindows(nextWindows: Batch[TimeWindow[A]]): Timeline[A] =
      tl ++ nextWindows

    def at(time: Seconds): A => Option[A] = subject =>
      @tailrec
      def rec(remaining: Batch[TimeWindow[A]], acc: Option[A]): Option[A] =
        if remaining.isEmpty then acc
        else
          val x  = remaining.head
          val xs = remaining.tail

          if x.within(time) then
            val value = acc.getOrElse(subject)
            val next  = (Signal.Time |> x.modifier(value)).at(time - x.start)
            rec(xs, Some(next))
          else rec(xs, acc)

      rec(tl, None)

    def atOrElse(time: Seconds): A => A =
      subject => at(time)(subject).getOrElse(subject)

    def atOrElse(time: Seconds, default: A): A => A =
      subject => at(time)(subject).getOrElse(default)

    def toWindows: Batch[TimeWindow[A]] =
      tl

    def duration: Seconds =
      tl.map(_.end).sortWith(_ > _).headOption.getOrElse(Seconds.zero)

    def length: Seconds =
      duration
