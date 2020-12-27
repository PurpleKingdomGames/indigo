package indigoextras.trees

import org.scalacheck._
import org.scalacheck.Prop.propBoolean
import indigoextras.geometry.BoundingBox

object QuadTreeSpecification extends Properties("QuadTree") {

  val gen: Gen[Double] = Gen.choose(1, 100)

  property("subdivide") = Prop.forAll(gen, gen, gen, gen) { (x: Double, y: Double, width: Double, height: Double) =>
    val original = BoundingBox(x, y, width, height)

    val divisions = QuadTree.QuadBranch.subdivide(original)

    val z =
      BoundingBox(
        Double.PositiveInfinity,
        Double.PositiveInfinity,
        Double.NegativeInfinity,
        Double.NegativeInfinity
      )

    val recombined: BoundingBox =
      List(divisions._1, divisions._2, divisions._3, divisions._4)
        .foldLeft(z)((acc, next) => acc.expandToInclude(next))

    (recombined ~== original) :| s"Recombined: ${recombined.toString()} - Original: ${original.toString()} - Divisions: $divisions"
  }

}
