package indigo.shared.trees

import indigo.shared.geometry.BoundingBox
import org.scalacheck.*
import org.scalacheck.Prop.propBoolean

import scala.annotation.nowarn

@nowarn("msg=unused")
object QuadTreeSpecification extends Properties("QuadTree") {

  val gen: Gen[Double] = Gen.choose(1, 100)

  property("subdivide") = Prop.forAll(gen, gen, gen, gen) { (x: Double, y: Double, width: Double, height: Double) =>
    val original = BoundingBox(x, y, width, height)

    val divisions = QuadTree.Branch.subdivide(original)

    val recombined: BoundingBox =
      List(divisions._1, divisions._2, divisions._3, divisions._4)
        .reduce(_.expandToInclude(_))

    (recombined ~== original) :| s"Recombined: ${recombined.toString()} - Original: ${original.toString()} - Divisions: $divisions"
  }

}
