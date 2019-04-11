package indigoexts.quadtrees

import org.scalacheck._
import org.scalacheck.Prop.BooleanOperators

import indigo.shared.AsString._
import indigo.shared.EqualTo._

object QuadBoundsSpecification extends Properties("QuadBounds") {

  val gen: Gen[Int] = Gen.choose(1, 100)

  property("subdivide") = Prop.forAll(gen, gen, gen, gen) { (x: Int, y: Int, width: Int, height: Int) =>
    val original = QuadBounds(x, y, width, height)

    val divisions = original.subdivide

    val recombined =
      QuadBounds.combine(divisions._1, List(divisions._2, divisions._3, divisions._4))

    (recombined === original) :| s"Recombined: ${recombined.show} - Original: ${original.show} - Divisions: $divisions"
  }

}
