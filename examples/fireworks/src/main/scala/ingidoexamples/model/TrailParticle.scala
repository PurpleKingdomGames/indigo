package ingidoexamples.model

import indigo.shared.temporal.Signal
import indigo.shared.temporal.SignalFunction
import indigo.shared.time.Millis
import indigoexts.subsystems.automata.AutomatonPayload

final class TrailParticle(val alpha: Double) extends AutomatonPayload {

  override def toString: String =
    s"TrailParticle(alpha = ${alpha.toString})"

}

object TrailParticle {

  val initialAlpha: Double = 0.5

  def apply(alpha: Double): TrailParticle =
    new TrailParticle(alpha)

  def unapply(trailParticle: TrailParticle): Option[Double] =
    Some(trailParticle.alpha)

  def create: TrailParticle =
    TrailParticle(1.0d)

  def fade(lifeSpan: Millis): SignalFunction[Millis, Double] =
    SignalFunction { t =>
      initialAlpha * (1 - (t.toDouble / lifeSpan.toDouble))
    }

  val combine: SignalFunction[Double, TrailParticle] =
    SignalFunction { case a => TrailParticle(a) }

  def particle(lifeSpan: Millis): Signal[TrailParticle] =
    Signal.clampTime(Signal.Time, Millis.zero, lifeSpan) |>
      (fade(lifeSpan) >>> combine)

}
