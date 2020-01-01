package indigo.shared.collections

import utest._
import indigo.shared.TestFail._

object HListTests extends TestSuite {

  val tests: Tests =
    Tests {
      "Using an HList" - {

        "should allow construction" - {

          val a: HList = 1 :: "a" :: true :: HNil

          a.headOption.get ==> 1
          a.tail.headOption.get ==> "a"
          a.tail.tail.headOption.get ==> true
          a.tail.tail.tail.headOption ==> None

        }

        "should have a notion of equality checking" - {
          val a: HList = 1 :: "a" :: true :: HNil
          val b: HList = false :: "b" :: 2 :: HNil

          a === a ==> true
          HList.equalityCheck(a, b) ==> false
        }

        "should allow conversion from a regular list" - {
          val hlist: HList = HList.fromList(List(1, 2, 3))

          hlist.headOption.get ==> 1
          hlist.tail.headOption.get ==> 2
          hlist.tail.tail.headOption.get ==> 3
          hlist.tail.tail.tail.headOption ==> None
        }

        "should allow joining HLists together" - {

          val a = 1 :: 2 :: 3 :: HNil
          val b = "a" :: "b" :: "c" :: HNil

          val c = a ++ b

          c.headOption.get ==> 1
          c.tail.headOption.get ==> 2
          c.tail.tail.headOption.get ==> 3
          c.tail.tail.tail.headOption.get ==> "a"
          c.tail.tail.tail.tail.headOption.get ==> "b"
          c.tail.tail.tail.tail.tail.headOption.get ==> "c"
          c.tail.tail.tail.tail.tail.tail.headOption ==> None
        }

        "should be able to reverse an HList" - {

          val a: HList = 1 :: "a" :: true :: HNil

          val b: HList = a.reverse

          b.headOption.get ==> true
          b.tail.headOption.get ==> "a"
          b.tail.tail.headOption.get ==> 1
          b.tail.tail.tail.headOption ==> None
        }

        "should allow a pattern match" - {
          val a: HList = 1 :: "a" :: true :: HNil

          a match {
            case HList(h, _) =>
              h ==> 1
          }

          val b: HList = HNil

          b match {
            case x @ HNil =>
              x ==> HNil

            case _ =>
              fail("")
          }
        }

        "should be able to report it's length" - {
          val a: HList = 1 :: "a" :: true :: HNil

          a.length ==> 3
        }

      }
    }

}
