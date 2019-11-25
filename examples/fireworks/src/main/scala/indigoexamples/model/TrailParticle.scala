package indigoexamples.model

import indigo.shared.temporal.Signal
import indigo.shared.temporal.SignalFunction
import indigo.shared.time.Millis
import indigoexts.subsystems.automata.AutomatonPayload
import indigo.shared.datatypes.Tint

final class TrailParticle(val alpha: Double, val tint: Tint) extends AutomatonPayload {

  override def toString: String =
    s"TrailParticle(alpha = ${alpha.toString})"

}

object TrailParticle {

  val initialAlpha: Double = 0.5

  def apply(alpha: Double, tint: Tint): TrailParticle =
    new TrailParticle(alpha, tint)

  def unapply(trailParticle: TrailParticle): Option[(Double, Tint)] =
    Some((trailParticle.alpha, trailParticle.tint))

  def create(tint: Tint): TrailParticle =
    TrailParticle(1.0d, tint)

  def fade(lifeSpan: Millis): SignalFunction[Millis, Double] =
    SignalFunction { t =>
      initialAlpha * (1 - (t.toDouble / lifeSpan.toDouble))
    }

  def combine(tint: Tint): SignalFunction[Double, TrailParticle] =
    SignalFunction { case a => TrailParticle(a, tint) } // WRONG

  def particle(lifeSpan: Millis, tint: Tint): Signal[TrailParticle] =
    Signal.clampTime(Signal.Time, Millis.zero, lifeSpan) |>
      (fade(lifeSpan) >>> combine(tint))

}
