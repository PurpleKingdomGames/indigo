package indigo.shared.animation.timeline

import indigo.shared.collections.Batch
import indigo.shared.temporal.Signal
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
    /** Add a `TimeSlot[A]` to the animation as a `TimeWindow`. Time slots are the building block of animation and can
      * be combined together to build up an animation, for example: `startAfter(2.seconds) andThen
      * show(5.seconds)(_.withAlpha(0.5))`
      *
      * @param animation
      *   the `TimeSlot[A]` to add
      * @return
      *   `Timeline[A]`
      */
    def add(animation: TimeSlot[A]): Timeline[A] =
      addWindows(animation.toWindows)

    /** Add a time window to the animation.
      *
      * @param nextWindow
      *   `TimeWindow[A]`
      * @return
      *   `Timeline[A]`
      */
    def addWindow(nextWindow: TimeWindow[A]): Timeline[A] =
      tl :+ nextWindow

    /** Add a batch of time windows to the animation.
      *
      * @param nextWindows
      *   `Batch[TimeWindow[A]]`
      * @return
      *   `Timeline[A]`
      */
    def addWindows(nextWindows: Batch[TimeWindow[A]]): Timeline[A] =
      tl ++ nextWindows

    /** Samples the animation at the given time to produce a value. The return type is ultimately optional because you
      * could sample the animation outside of the animations time range.
      *
      * @param time
      *   the current running time in `Second`s to sample the animation at.
      * @return
      *   `A => Option[A]` where `A` is the 'subject', the thing to be animated.
      */
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

    /** Calls `at` but always returns a value, using the original subject `A` if no other value would be produced by the
      * animation.
      *
      * @param time
      *   the current running time in `Second`s to sample the animation at.
      * @return
      *   `A`
      */
    def atOrElse(time: Seconds): A => A =
      subject => at(time)(subject).getOrElse(subject)

    /** Calls `at` but always returns a value, using the given default subject `A` if no other value would be produced
      * by the animation.
      *
      * @param time
      *   the current running time in `Second`s to sample the animation at.
      * @param default
      *   the subject to use if no other value is produced
      * @return
      *   `A`
      */
    def atOrElse(time: Seconds, default: A): A => A =
      subject => at(time)(subject).getOrElse(default)

    /** Clamps the time to be within the animation's duration, ensuring that the last frame is present even when over
      * the time windows. Still produces an optional value because you may have a delay at the beginning of you
      * animation, for example.
      *
      * @param time
      *   the current running time in `Second`s to sample the animation at.
      * @return
      *   `Option[A]`
      */
    def atOrLast(time: Seconds): A => Option[A] =
      val d = duration
      subject => at(if time < d then time else d)(subject)

    /** Give the time windows as a Batch.
      *
      * @return
      *   `Batch[TimeWindow[A]]`
      */
    def toWindows: Batch[TimeWindow[A]] =
      tl

    /** Return the total time duration of the animation in seconds, including any initial delays.
      *
      * @return
      *   `Seconds`
      */
    def duration: Seconds =
      tl.maxByOption(_.end.toDouble).fold(Seconds.zero)(_.end)

    /** Alias for duration, returns the total time duration of the animation in seconds, including any initial delays.
      *
      * @return
      *   `Seconds`
      */
    def length: Seconds =
      duration
