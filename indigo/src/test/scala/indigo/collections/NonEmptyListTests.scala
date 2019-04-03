package indigo.collections

import utest._
import indigo.shared.EqualTo._
import indigo.shared.AsString._

object NonEmptyListTests extends TestSuite {

  val tests: Tests =
    Tests {

      "NonEmptyList AsString" - {
        "should be able to show a list of Ints" - {

          val nel = NonEmptyList(1, 2, 3)

          nel.show ==> "Nel[1][2, 3]"

        }
      }

      "NonEmptyList usage" - {

        "can be pattern matched" - {
          NonEmptyList(1, 2, 3) match {
            case NonEmptyList(head, tail) =>
              head ==> 1
              tail ==> List(2, 3)
          }
        }

      }

      "NonEmptyList ops" - {

        "should have: equality" - {
          NonEmptyList(1) === NonEmptyList(1) ==> true
        }

        "should have: point" - {
          NonEmptyList(1) === NonEmptyList(1) ==> true
        }

        "should have: map" - {
          NonEmptyList(1, 2, 3).map(_ * 10) === NonEmptyList(10, 20, 30) ==> true
        }

        "should have: reverse" - {
          NonEmptyList(1, 2, 3, 4, 5).reverse === NonEmptyList(5, 4, 3, 2, 1) ==> true
          NonEmptyList(1) === NonEmptyList(1) ==> true
        }

        "should have: combine" - {
          NonEmptyList(1) ++ NonEmptyList(2) === NonEmptyList(1, 2) ==> true
        }

        "should have: flatten" - {
          NonEmptyList.flatten(NonEmptyList(NonEmptyList.point(1))) === NonEmptyList(1) ==> true
        }

        "should have: flatMap" - {
          NonEmptyList(1, 2, 3).flatMap(i => NonEmptyList(i * 10 + 1)) === NonEmptyList(11, 21, 31) ==> true
        }

        "should have: foldLeft" - {
          NonEmptyList("a", "b", "c").foldLeft("")(_ + _) ==> "abc"
        }

        "should have: reduceLeft" - {
          NonEmptyList("a", "b", "c").reduce(_ + _) ==> "abc"
        }

        "should have: append" - {
          NonEmptyList(1) :+ 2 === NonEmptyList(1, 2) ==> true
        }

        "should have: cons" - {
          1 :: NonEmptyList(2) === NonEmptyList(1, 2) ==> true
        }

        "should have: zipWithIndex" - {
          NonEmptyList("a", "b", "c").zipWithIndex === NonEmptyList(("a", 0), ("b", 1), ("c", 2)) ==> true
        }

        "should have: zip" - {
          (NonEmptyList(1, 2, 3) zip NonEmptyList("a", "b", "c")) === NonEmptyList((1, "a"), (2, "b"), (3, "c")) ==> true
        }

        "should have: forall" - {
          NonEmptyList(1, 2, 3).forall(_ > 0) ==> true
          NonEmptyList(1, 2, 3).forall(_ > 1) ==> false
        }

        "should have: find" - {
          NonEmptyList(1, 2, 3).find(_ == 2) ==> Some(2)
          NonEmptyList(1, 2, 3).find(_ == 4) ==> None
        }

        "should have: exists" - {
          NonEmptyList(1, 2, 3).exists(_ == 2) ==> true
          NonEmptyList(1, 2, 3).exists(_ == 4) ==> false
        }

      }

    }

}
