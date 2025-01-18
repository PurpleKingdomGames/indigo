package indigo.shared

import org.scalacheck.*

import scala.annotation.nowarn

@nowarn("msg=unused")
object PowerOfTwoSpecification extends Properties("PowerOfTwo") {

  val validValues: Seq[Int] =
    PowerOfTwo.all.map(_.value).toSeq

  val genValid: Gen[Int] =
    Gen.oneOf(validValues)

  val genInvalid: Gen[Int] =
    Gen
      .choose(-10000, 10000)
      .filter(i => !validValues.contains(i))

  property("is valid") = Prop.forAll(genValid) { (i: Int) =>
    PowerOfTwo.isValidPowerOfTwo(i)
  }

  property("is invalid") = Prop.forAll(genInvalid) { (i: Int) =>
    !PowerOfTwo.isValidPowerOfTwo(i)
  }

}
