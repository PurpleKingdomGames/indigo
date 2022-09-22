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

final case class TimeWindow[A](start: Seconds, end: Seconds, modifier: A => SignalFunction[Seconds, A]):
  // TODO: Write tests for all these fields and methods!
  lazy val length: Seconds    = end
  lazy val totalTime: Seconds = end - start

  def within(t: Seconds): Boolean =
    t >= start && t <= end

  def withStart(newStart: Seconds): TimeWindow[A] =
    this.copy(start = newStart)

  def withEnd(newEnd: Seconds): TimeWindow[A] =
    this.copy(end = newEnd)

  def withModifier(newModifier: A => SignalFunction[Seconds, A]): TimeWindow[A] =
    this.copy(modifier = newModifier)

  def contractBy(amount: Seconds): TimeWindow[A] =
    multiply((amount / length).toDouble)

  def expandBy(amount: Seconds): TimeWindow[A] =
    multiply(1.0 + (amount / length).toDouble)

  def multiply(amount: Double): TimeWindow[A] =
    this.copy(start = this.start * amount, end = this.end * amount)

  def shiftBy(time: Seconds): TimeWindow[A] =
    this.copy(start = this.start + time, end = this.end + time)

  def trim: TimeWindow[A] =
    this.copy(start = Seconds.zero, end = this.end - this.start)

sealed trait TimeSlot[A]:
  def andThen(next: TimeSlot[A]): TimeSlot.Combine[A] = TimeSlot.Combine(this, next)

  def toWindows: Batch[TimeWindow[A]] =
    @tailrec
    def rec(remaining: List[TimeSlot[A]], playhead: Seconds, acc: Batch[TimeWindow[A]]): Batch[TimeWindow[A]] =
      remaining match
        case Nil =>
          acc.reverse

        case TimeSlot.Wait(t) :: ts =>
          rec(ts, playhead + t, acc)

        case TimeSlot.Animate(t, m) :: ts =>
          val end = playhead + t
          rec(ts, end, TimeWindow(playhead, end, m) :: acc)

        case TimeSlot.Fixed(t, a) :: ts =>
          val end = playhead + t
          rec(ts, end, TimeWindow(playhead, end, _ => SignalFunction(_ => a)) :: acc)

        case TimeSlot.Combine(a, b) :: ts =>
          rec(a :: b :: ts, playhead, acc)

    rec(List(this), Seconds.zero, Batch.empty)

object TimeSlot:

  def start[A]: Wait[A] =
    Wait(Seconds.zero)

  def startAfter[A](time: Seconds): Wait[A] =
    Wait(time)

  def pause[A](time: Seconds): Wait[A] =
    Wait(time)

  def show[A](time: Seconds, value: A): Fixed[A] =
    Fixed(time, value)

  def modify[A](time: Seconds)(modifier: A => SignalFunction[Seconds, A]): Animate[A] =
    Animate(time, modifier)
  def animate[A](time: Seconds)(modifier: A => SignalFunction[Seconds, A]): Animate[A] =
    Animate(time, modifier)
  def tween[A](time: Seconds)(modifier: A => SignalFunction[Seconds, A]): Animate[A] =
    Animate(time, modifier)

  final case class Wait[A](time: Seconds)                                               extends TimeSlot[A]
  final case class Animate[A](over: Seconds, modifier: A => SignalFunction[Seconds, A]) extends TimeSlot[A]
  final case class Fixed[A](over: Seconds, modifier: A)                                 extends TimeSlot[A]
  final case class Combine[A](first: TimeSlot[A], second: TimeSlot[A])                  extends TimeSlot[A]
