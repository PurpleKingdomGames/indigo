package indigoexamples.model

import org.scalacheck._
import org.scalacheck.Prop._
import ingidoexamples.model.TrailParticle
import indigo.shared.temporal.Signal
import indigoexts.geometry.Vertex
import indigo.shared.time.Millis

class TrailParticleSpecification extends Properties("TrailParticle") {

  import Generators._

  //TODO: Missing tests that demonstrate change over time.

  property("particle should fall at a rate of 1 per second") = Prop.forAll(clampedMillisGen(0, 5000)) { t =>
    val fallAmount: Double =
      (Signal.Time |> TrailParticle.fall).at(t)

    Prop.all(
      "0 <= fallAmount <= 1" |: fallAmount >= 0.0d && fallAmount <= 5.0d // Because clamped to 5000 millis
    )
  }

  property("particle should fade to zero by the end of it's life") = Prop.forAll(clampedMillisGen(0, 5000)) { t =>
    val fadeAmount: Double =
      (Signal.Time |> TrailParticle.fade(Millis(5000))).at(t)

    Prop.all(
      "1 >= fadeAmount >= 0" |: fadeAmount >= 0.0d && fadeAmount <= 1.0d,
      fadeAmount + "==" + t.toDouble / 5000 |: fadeAmount == t.toDouble / 5000
    )
  }

}
