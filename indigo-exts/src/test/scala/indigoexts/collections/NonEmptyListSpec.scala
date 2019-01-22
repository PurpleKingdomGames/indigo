package indigoexts.collections

import org.scalactic.Equality
import org.scalatest.{FunSpec, Matchers}

import indigo.Eq._
import indigo.shared.Eq

class NonEmptyListSpec extends FunSpec with Matchers {

  // Needed because of the funky NonEmptyList type.
  implicit def eq[T](implicit eqT: Eq[T]): Equality[NonEmptyList[T]] =
    new Equality[NonEmptyList[T]] {
      def areEqual(a: NonEmptyList[T], b: Any): Boolean =
        b match {
          case l: NonEmptyList[T] @unchecked =>
            NonEmptyList.equality(a, l)(eqT)

          case _ =>
            false
        }
    }

  describe("NonEmptyList usage") {

    it("can be pattern matched") {
      NonEmptyList(1, 2, 3) match {
        case NonEmptyList(head, tail) =>
          head shouldEqual 1
          tail shouldEqual List(2, 3)

        case _ =>
          fail("oops")
      }
    }

  }

  describe("NonEmptyList ops") {

    it("should have: equality") {
      NonEmptyList(1) === NonEmptyList(1) shouldEqual true
    }

    it("should have: point") {
      NonEmptyList.point(1) shouldEqual NonEmptyList(1)
    }

    it("should have: map") {
      NonEmptyList(1, 2, 3).map(_ * 10) shouldEqual NonEmptyList(10, 20, 30)
    }

    it("should have: reverse") {
      NonEmptyList(1, 2, 3, 4, 5).reverse shouldEqual NonEmptyList(5, 4, 3, 2, 1)
      NonEmptyList(1) shouldEqual NonEmptyList(1)
    }

    it("should have: combine") {
      NonEmptyList(1) ++ NonEmptyList(2) shouldEqual NonEmptyList(1, 2)
    }

    it("should have: flatten") {
      NonEmptyList.flatten(NonEmptyList(NonEmptyList.point(1))) shouldEqual NonEmptyList(1)
    }

    it("should have: flatMap") {
      NonEmptyList(1, 2, 3).flatMap(i => NonEmptyList(i * 10 + 1)) shouldEqual NonEmptyList(11, 21, 31)
    }

    it("should have: foldLeft") {
      NonEmptyList("a", "b", "c").foldLeft("")(_ + _) shouldEqual "abc"
    }

    it("should have: reduceLeft") {
      NonEmptyList("a", "b", "c").reduce(_ + _) shouldEqual "abc"
    }

    it("should have: append") {
      NonEmptyList(1) :+ 2 shouldEqual NonEmptyList(1, 2)
    }

    it("should have: cons") {
      1 :: NonEmptyList(2) shouldEqual NonEmptyList(1, 2)
    }

    it("should have: zipWithIndex") {
      NonEmptyList("a", "b", "c").zipWithIndex shouldEqual NonEmptyList(("a", 0), ("b", 1), ("c", 2))
    }

    it("should have: zip") {
      NonEmptyList(1, 2, 3) zip NonEmptyList("a", "b", "c") shouldEqual NonEmptyList((1, "a"), (2, "b"), (3, "c"))
    }

    it("should have: forall") {
      NonEmptyList(1, 2, 3).forall(_ > 0) shouldEqual true
      NonEmptyList(1, 2, 3).forall(_ > 1) shouldEqual false
    }

    it("should have: find") {
      NonEmptyList(1, 2, 3).find(_ == 2) shouldEqual Some(2)
      NonEmptyList(1, 2, 3).find(_ == 4) shouldEqual None
    }

    it("should have: exists") {
      NonEmptyList(1, 2, 3).exists(_ == 2) shouldEqual true
      NonEmptyList(1, 2, 3).exists(_ == 4) shouldEqual false
    }

  }

}
