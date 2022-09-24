package indigo.shared.animation.timeline

import indigo.shared.collections.Batch
import indigo.shared.temporal.SignalFunction
import indigo.shared.time.Seconds

import scala.annotation.tailrec
import scala.annotation.targetName

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

  def animate[A](time: Seconds)(modifier: Seconds ?=> A => SignalFunction[Seconds, A]): Animate[A] =
    given Seconds = time
    Animate(time, modifier)

  @targetName("TimeSlot_animate_unsplit_args")
  def animate[A](time: Seconds, modifier: Seconds ?=> A => SignalFunction[Seconds, A]): Animate[A] =
    given Seconds = time
    Animate(time, modifier)

  final case class Wait[A](time: Seconds)                                               extends TimeSlot[A]
  final case class Animate[A](over: Seconds, modifier: A => SignalFunction[Seconds, A]) extends TimeSlot[A]
  final case class Fixed[A](over: Seconds, modifier: A)                                 extends TimeSlot[A]
  final case class Combine[A](first: TimeSlot[A], second: TimeSlot[A])                  extends TimeSlot[A]
