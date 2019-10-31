package indigoexts.primitive

import utest._
import indigoexts.primitives.UpdateList

object UpdateListTests extends TestSuite {

  val tests: Tests =
    Tests {

      "trivial updating" - {

        val l: UpdateList[Int] =
          UpdateList(List(1, 2, 3))

        val expected =
          UpdateList(List(2, 3, 4))

        val actual =
          l.update(_ + 1)

        expected.toList ==> actual.toList

      }

      "interleaved update" - {

        val p =
          UpdateList.Pattern.interleave(2)

        val l: UpdateList[Int] =
          UpdateList(List(1, 2, 3)).withPattern(p)

        val f =
          (_: Int) + 1

        val expected1 =
          List(2, 2, 4)

        val actual1 =
          l.update(f)

        val expected2 =
          List(2, 3, 4)

        val actual2 =
          actual1.update(f)

        expected1.toList ==> actual1.toList
        expected2.toList ==> actual2.toList

      }

    }

}
