package indigoextras.animation

import indigo.shared.collections.Batch
import indigo.shared.temporal.Signal
import indigo.shared.temporal.SignalFunction
import indigo.shared.time.Seconds

import scala.annotation.tailrec
import scala.annotation.targetName

final case class Timeline[A](windows: Batch[TimeWindow[A]]):

  def add(animation: TimeSlot[A]): Timeline[A] =
    addWindows(animation.toWindows)

  def addWindow(nextWindow: TimeWindow[A]): Timeline[A] =
    this.copy(windows = windows :+ nextWindow)

  def addWindows(nextWindows: Batch[TimeWindow[A]]): Timeline[A] =
    this.copy(windows = windows ++ nextWindows)

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

    rec(windows, None)

object Timeline:
  def apply[A](timeWindows: TimeWindow[A]*): Timeline[A] =
    Timeline(Batch.fromSeq(timeWindows))

  @targetName("timeline_apply_slots")
  def apply[A](timeSlots: TimeSlot[A]*): Timeline[A] =
    Timeline(Batch.fromSeq(timeSlots).flatMap(_.toWindows))

  def empty[A]: Timeline[A] =
    Timeline(Batch.empty[TimeWindow[A]])


