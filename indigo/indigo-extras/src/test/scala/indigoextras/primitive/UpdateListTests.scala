package indigoextras.primitive

import utest._
import indigoextras.primitives.UpdateList
import indigoextras.primitives.UpdatePattern

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
          UpdatePattern.Interleave()

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

        val expected3 =
          List(3, 3, 5)

        val actual3 =
          actual2.update(f)

        expected1.toList ==> actual1.toList
        expected2.toList ==> actual2.toList
        expected3.toList ==> actual3.toList

      }

      "update every" - {

        val p =
          UpdatePattern.Every(3)

        val l: UpdateList[Int] =
          UpdateList(List(1, 2, 3)).withPattern(p)

        val f =
          (_: Int) + 1

        val expected1 =
          List(2, 2, 3)

        val actual1 =
          l.update(f)

        val expected2 =
          List(2, 3, 3)

        val actual2 =
          actual1.update(f)

        val expected3 =
          List(2, 3, 4)

        val actual3 =
          actual2.update(f)

        val expected4 =
          List(3, 3, 4)

        val actual4 =
          actual3.update(f)

        expected1.toList ==> actual1.toList
        expected2.toList ==> actual2.toList
        expected3.toList ==> actual3.toList
        expected4.toList ==> actual4.toList

      }

      "batch update" - {

        val p =
          UpdatePattern.Batch(4)

        val list: List[Int] =
          List(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

        val l: UpdateList[Int] =
          UpdateList(list).withPattern(p)

        val expected1 =
          List(1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

        val actual1 =
          l.update(_ + 1)

        val expected2 =
          List(1, 1, 1, 1, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0)

        val actual2 =
          actual1.update(_ + 2)

        val expected3 =
          List(1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 0, 0, 0, 0)

        val actual3 =
          actual2.update(_ + 3)

        val expected4 =
          List(1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4)

        val actual4 =
          actual3.update(_ + 4)

        val expected5 =
          List(2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4)

        val actual5 =
          actual4.update(_ + 1)

        expected1.toList ==> actual1.toList
        expected2.toList ==> actual2.toList
        expected3.toList ==> actual3.toList
        expected4.toList ==> actual4.toList
        expected5.toList ==> actual5.toList
      }

    }

}
