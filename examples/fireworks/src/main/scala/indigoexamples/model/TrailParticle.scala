package indigoexamples.model

import indigo._
import indigoextras.subsystems.AutomatonPayload

final class TrailParticle(val alpha: Double, val tint: RGBA) extends AutomatonPayload {

  override def toString: String =
    s"TrailParticle(alpha = ${alpha.toString})"

}

object TrailParticle {

  val initialAlpha: Double = 0.5

  def apply(alpha: Double, tint: RGBA): TrailParticle =
    new TrailParticle(alpha, tint)

  def unapply(trailParticle: TrailParticle): Option[(Double, RGBA)] =
    Some((trailParticle.alpha, trailParticle.tint))

  def create(tint: RGBA): TrailParticle =
    TrailParticle(1.0d, tint)

  def fade(lifeSpan: Seconds): Signal[Double] =
    Signal { t =>
      initialAlpha * (1 - (t.toDouble / lifeSpan.toDouble))
    }

}
