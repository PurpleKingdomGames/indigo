package indigo.shared.animation.timeline

import indigo.shared.temporal.SignalFunction
import indigo.shared.time.Seconds

final case class TimeWindow[A](start: Seconds, end: Seconds, modifier: A => SignalFunction[Seconds, A]):
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
    multiply(1.0 - (amount / length).toDouble)

  def expandBy(amount: Seconds): TimeWindow[A] =
    multiply(1.0 + (amount / length).toDouble)

  def multiply(amount: Double): TimeWindow[A] =
    this.copy(start = this.start * amount, end = this.end * amount)

  def shiftBy(time: Seconds): TimeWindow[A] =
    this.copy(start = this.start + time, end = this.end + time)

  def trim: TimeWindow[A] =
    this.copy(start = Seconds.zero, end = this.end - this.start)
