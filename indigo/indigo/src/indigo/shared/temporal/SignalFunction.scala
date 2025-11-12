package indigo.shared.temporal

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Vector2
import indigo.shared.time.Seconds

/** A Signal Function is a combinator that maps `Signal[A] -> Signal[B]`. One way to thing of signal functions is to
  * think of each instance as one section of a transformation pipeline. When you attach the pipeline to a `Signal` you
  * can ask the pipeline for a transformed value over time. For example, you could have a signal that produces an
  * endlessly looping angle, and a signal function that turns that angle into perhaps and orbit or a color.
  */
opaque type SignalFunction[A, B] = Signal[A] => Signal[B]

object SignalFunction:

  inline def apply[A, B](f: A => B): SignalFunction[A, B] =
    lift(f)

  extension [A, B](sf: SignalFunction[A, B])
    def run: Signal[A] => Signal[B] = sf

    def >>>[C](other: SignalFunction[B, C]): SignalFunction[A, C] =
      andThen(other)

    def andThen[C](other: SignalFunction[B, C]): SignalFunction[A, C] =
      sf andThen other

    def &&&[C](other: SignalFunction[A, C]): SignalFunction[A, (B, C)] =
      and(other)

    def and[C](other: SignalFunction[A, C]): SignalFunction[A, (B, C)] =
      SignalFunction.parallel(sf, other)

  /** Equvilent to `pure` but for SignalFunctions
    */
  def arr[A, B](f: A => B): SignalFunction[A, B] =
    lift[A, B](f)

  def lift[A, B](f: A => B): SignalFunction[A, B] =
    (sa: Signal[A]) => sa.map(f)

  def flatLift[A, B](f: A => Signal[B]): SignalFunction[A, B] =
    (sa: Signal[A]) => sa.flatMap(f)

  def parallel[A, B, C](sa: SignalFunction[A, B], sb: SignalFunction[A, C]): SignalFunction[A, (B, C)] =
    (s: Signal[A]) => sa.run(s) |*| sb.run(s)

  // Helper functions

  private def clampToTime[A](time: Seconds, over: Seconds, start: A, end: A): A => A = value =>
    if time <= Seconds.zero then start
    else if time >= over then end
    else value

  private def lerpDouble(start: Double, end: Double, over: Seconds): Seconds => Double = time =>
    clampToTime(time, over, start, end) {
      start + ((end - start) * (time / over).toDouble)
    }

  def lerp(over: Seconds): SignalFunction[Seconds, Double] =
    SignalFunction(lerpDouble(0, 1, over))
  def lerp(start: Double, end: Double, over: Seconds): SignalFunction[Seconds, Double] =
    SignalFunction(lerpDouble(start, end, over))
  def lerp(start: Vector2, end: Vector2, over: Seconds): SignalFunction[Seconds, Vector2] =
    SignalFunction(t => Vector2(lerpDouble(start.x, end.x, over)(t), lerpDouble(start.y, end.y, over)(t)))
  def lerp(start: Point, end: Point, over: Seconds): SignalFunction[Seconds, Point] =
    lerp(start.toVector, end.toVector, over) >>> SignalFunction(v => v.round.toPoint)

  private def lerpFromDouble(start: Double, end: Double): Double => Double = amount =>
    if amount <= 0.0d then start
    else if amount >= 1.0d then end
    else start + ((end - start) * amount)

  def lerp(start: Double, end: Double): SignalFunction[Double, Double] =
    SignalFunction(lerpFromDouble(start, end))
  def lerp(start: Vector2, end: Vector2): SignalFunction[Double, Vector2] =
    SignalFunction(d => Vector2(lerpFromDouble(start.x, end.x)(d), lerpFromDouble(start.y, end.y)(d)))
  def lerp(start: Point, end: Point): SignalFunction[Double, Point] =
    lerp(start.toVector, end.toVector) >>> SignalFunction(v => v.round.toPoint)

  private def easeInDouble(start: Double, end: Double, over: Seconds): Seconds => Double = time =>
    clampToTime(time, over, start, end) {
      val amount = lerpDouble(start, end, over)(time)
      amount * amount
    }

  def easeIn(over: Seconds): SignalFunction[Seconds, Double] =
    SignalFunction(easeInDouble(0, 1, over))
  def easeIn(start: Double, end: Double, over: Seconds): SignalFunction[Seconds, Double] =
    SignalFunction(easeInDouble(start, end, over))
  def easeIn(start: Vector2, end: Vector2, over: Seconds): SignalFunction[Seconds, Vector2] =
    SignalFunction(t => Vector2(easeInDouble(start.x, end.x, over)(t), easeInDouble(start.y, end.y, over)(t)))
  def easeIn(start: Point, end: Point, over: Seconds): SignalFunction[Seconds, Point] =
    easeIn(start.toVector, end.toVector, over) >>> SignalFunction(v => v.round.toPoint)

  private def easeOutDouble(start: Double, end: Double, over: Seconds): Seconds => Double = time =>
    clampToTime(time, over, start, end) {
      val amount = 1 - lerpDouble(start, end, over)(time)
      1 - (amount * amount)
    }

  def easeOut(over: Seconds): SignalFunction[Seconds, Double] =
    SignalFunction(easeOutDouble(0, 1, over))
  def easeOut(start: Double, end: Double, over: Seconds): SignalFunction[Seconds, Double] =
    SignalFunction(easeOutDouble(start, end, over))
  def easeOut(start: Vector2, end: Vector2, over: Seconds): SignalFunction[Seconds, Vector2] =
    SignalFunction(t => Vector2(easeOutDouble(start.x, end.x, over)(t), easeOutDouble(start.y, end.y, over)(t)))
  def easeOut(start: Point, end: Point, over: Seconds): SignalFunction[Seconds, Point] =
    easeOut(start.toVector, end.toVector, over) >>> SignalFunction(v => v.round.toPoint)

  private val pi: Double  = Math.PI
  private val pi2: Double = Math.PI / 2

  private def easeInOutDouble(start: Double, end: Double, over: Seconds): Seconds => Double = time =>
    clampToTime(time, over, start, end) {
      val t = time.toDouble / over.toDouble
      val m = 1 - ((Math.sin(pi2 + (pi * t)) / 2) + 0.5)

      start + ((end - start) * m)
    }

  def easeInOut(over: Seconds): SignalFunction[Seconds, Double] =
    SignalFunction(easeInOutDouble(0, 1, over))
  def easeInOut(start: Double, end: Double, over: Seconds): SignalFunction[Seconds, Double] =
    SignalFunction(easeInOutDouble(start, end, over))
  def easeInOut(start: Vector2, end: Vector2, over: Seconds): SignalFunction[Seconds, Vector2] =
    SignalFunction(t => Vector2(easeInOutDouble(start.x, end.x, over)(t), easeInOutDouble(start.y, end.y, over)(t)))
  def easeInOut(start: Point, end: Point, over: Seconds): SignalFunction[Seconds, Point] =
    easeInOut(start.toVector, end.toVector, over) >>> SignalFunction(v => v.round.toPoint)

  def wrap[A](at: Seconds): SignalFunction[Seconds, Seconds] =
    SignalFunction(_ % at)

  def clamp[A](from: Seconds, to: Seconds): SignalFunction[Seconds, Seconds] =
    SignalFunction { t =>
      if (from < to)
        if (t < from) from
        else if (t > to) to
        else t
      else if (t < to) to
      else if (t > from) from
      else t
    }

  def step(at: Seconds): SignalFunction[Seconds, Boolean] =
    SignalFunction(t => if t >= at then true else false)

  def sin: SignalFunction[Seconds, Double] =
    SignalFunction(t => Math.sin(Radians.fromSeconds(t).toDouble))

  def cos: SignalFunction[Seconds, Double] =
    SignalFunction(t => Math.cos(Radians.fromSeconds(t).toDouble))

  def orbit(center: Vector2, distance: Double, offset: Radians): SignalFunction[Seconds, Vector2] =
    SignalFunction { t =>
      Vector2(
        (Math.sin((Radians.fromSeconds(t) + offset).toDouble) * distance) + center.x,
        (Math.cos((Radians.fromSeconds(t) + offset).toDouble) * distance) + center.y
      )
    }

  def pulse(interval: Seconds): SignalFunction[Seconds, Boolean] =
    SignalFunction(t => (t.toMillis / interval.toMillis).toLong % 2 == 0)

  def smoothPulse: SignalFunction[Seconds, Double] =
    SignalFunction.cos >>> SignalFunction(a => 1.0 - (a + 1.0) / 2.0)

  def multiply(amount: Seconds): SignalFunction[Seconds, Seconds] =
    SignalFunction(_ * amount)
