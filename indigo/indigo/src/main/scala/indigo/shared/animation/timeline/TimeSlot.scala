package indigo.shared.animation.timeline

import indigo.shared.collections.Batch
import indigo.shared.temporal.SignalFunction
import indigo.shared.time.Seconds

import scala.annotation.tailrec
import scala.annotation.targetName

enum TimeSlot[A] derives CanEqual:
  case Wait[A](time: Seconds)                                               extends TimeSlot[A]
  case Animate[A](over: Seconds, modifier: A => SignalFunction[Seconds, A]) extends TimeSlot[A]
  case Fixed[A](over: Seconds, modifier: A => A)                            extends TimeSlot[A]
  case Combine[A](first: TimeSlot[A], second: TimeSlot[A])                  extends TimeSlot[A]

object TimeSlot:

  def start[A]: Wait[A] =
    Wait(Seconds.zero)

  def startAfter[A](time: Seconds): Wait[A] =
    Wait(time)

  def pause[A](time: Seconds): Wait[A] =
    Wait(time)

  def show[A](time: Seconds)(value: A => A): Fixed[A] =
    Fixed(time, value)
  @targetName("TimeSlot_animate_unsplit_args")
  def show[A](time: Seconds, value: A => A): Fixed[A] =
    Fixed(time, value)
  def show[A](time: Seconds)(value: A): Fixed[A] =
    Fixed(time, _ => value)

  def animate[A](time: Seconds)(modifier: Seconds ?=> A => SignalFunction[Seconds, A]): Animate[A] =
    given Seconds = time
    Animate(time, modifier)

  @targetName("TimeSlot_animate_unsplit_args")
  def animate[A](time: Seconds, modifier: Seconds ?=> A => SignalFunction[Seconds, A]): Animate[A] =
    given Seconds = time
    Animate(time, modifier)

  extension [A](ts: TimeSlot[A])
    def andThen(next: TimeSlot[A]): TimeSlot.Combine[A] = TimeSlot.Combine(ts, next)

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

          case TimeSlot.Fixed(t, f) :: ts =>
            val end = playhead + t
            rec(ts, end, TimeWindow(playhead, end, a => SignalFunction(_ => f(a))) :: acc)

          case TimeSlot.Combine(a, b) :: ts =>
            rec(a :: b :: ts, playhead, acc)

      rec(List(ts), Seconds.zero, Batch.empty)
