package indigoexamples.model

import org.scalacheck._
import org.scalacheck.Prop._
import indigoexamples.model.TrailParticle
import indigo.shared.temporal.Signal
import indigoexts.geometry.Vertex
import indigo.shared.time.Millis

class TrailParticleSpecification extends Properties("TrailParticle") {

  import Generators._

  property("particle should fade over time") = Prop.forAll(nowNextMillis(0, 1000)) {
    case (t1, t2) =>
      val fade1: Double =
        (Signal.Time |> TrailParticle.fade(Millis(1000))).at(t1)

      val fade2: Double =
        (Signal.Time |> TrailParticle.fade(Millis(1000))).at(t2)

      Prop.all(
        fade1 > fade2
      )
  }

  property("particle should fade to zero by the end of it's life") = Prop.forAll(clampedMillisGen(0, 5000)) { t =>
    val fadeAmount: Double =
      (Signal.Time |> TrailParticle.fade(Millis(5000))).at(t)

    Prop.all(
      "1 >= fadeAmount >= 0" |: fadeAmount >= 0.0d && fadeAmount <= 1.0d,
      fadeAmount ?= TrailParticle.initialAlpha * (1 - (t.toDouble / 5000))
    )
  }

}
