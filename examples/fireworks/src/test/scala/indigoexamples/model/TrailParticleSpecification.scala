package indigoexamples.model

import indigo.shared.temporal.Signal
import indigo.shared.time.Seconds
import indigoextras.geometry.Vertex
import org.scalacheck.Prop._
import org.scalacheck._

class TrailParticleSpecification extends Properties("TrailParticle") {

  import Generators._

  property("particle should fade over time") = Prop.forAll(nowNextSeconds(0, 1)) {
    case (t1, t2) =>
      val fade1: Double =
        TrailParticle.fade(Seconds(1)).at(t1)

      val fade2: Double =
        TrailParticle.fade(Seconds(1)).at(t2)

      Prop.all(
        fade1 > fade2
      )
  }

  property("particle should fade to zero by the end of it's life") = Prop.forAll(clampedSecondsGen(0, 5)) { t =>
    val fadeAmount: Double =
      TrailParticle.fade(Seconds(5)).at(t)

    Prop.all(
      "1 >= fadeAmount >= 0" |: fadeAmount >= 0.0d && fadeAmount <= 1.0d,
      fadeAmount ?= TrailParticle.initialAlpha * (1 - (t.toDouble / 5))
    )
  }

}
