package indigoexts.collections

import org.scalatest.{FunSpec, Matchers}

class HListSpec extends FunSpec with Matchers {

  describe("Using an HList") {

    it("should allow construction") {

      val a: HList = 1 :: "a" :: true :: HNil

      a.headOption.get shouldEqual 1
      a.tail.headOption.get shouldEqual "a"
      a.tail.tail.headOption.get shouldEqual true
      a.tail.tail.tail.headOption shouldEqual None

    }

    it("should have a notion of equality checking") {
      val a: HList = 1 :: "a" :: true :: HNil
      val b: HList = false :: "b" :: 2 :: HNil

      a === a shouldEqual true
      HList.equalityCheck(a, b) shouldEqual false
    }

    it("should allow conversion from a regular list") {
      val hlist: HList = HList.fromList(List(1, 2, 3))

      hlist.headOption.get shouldEqual 1
      hlist.tail.headOption.get shouldEqual 2
      hlist.tail.tail.headOption.get shouldEqual 3
      hlist.tail.tail.tail.headOption shouldEqual None
    }

    it("should allow joining HLists together") {

      val a = 1 :: 2 :: 3 :: HNil
      val b = "a" :: "b" :: "c" :: HNil

      val c = a ++ b

      c.headOption.get shouldEqual 1
      c.tail.headOption.get shouldEqual 2
      c.tail.tail.headOption.get shouldEqual 3
      c.tail.tail.tail.headOption.get shouldEqual "a"
      c.tail.tail.tail.tail.headOption.get shouldEqual "b"
      c.tail.tail.tail.tail.tail.headOption.get shouldEqual "c"
      c.tail.tail.tail.tail.tail.tail.headOption shouldEqual None
    }

    it("should be able to reverse an HList") {

      val a: HList = 1 :: "a" :: true :: HNil

      val b: HList = a.reverse

      b.headOption.get shouldEqual true
      b.tail.headOption.get shouldEqual "a"
      b.tail.tail.headOption.get shouldEqual 1
      b.tail.tail.tail.headOption shouldEqual None
    }

    it("should allow a pattern match") {
      val a: HList = 1 :: "a" :: true :: HNil

      a match {
        case HNil =>
          fail("should have contents")

        case HList(h, _) =>
          h shouldEqual 1
      }

      val b: HList = HNil

      b match {
        case x @ HNil =>
          x shouldEqual HNil

        case _ =>
          fail("shouldn't have contents")
      }
    }

    it("should be able to report it's length") {
      val a: HList = 1 :: "a" :: true :: HNil

      a.length shouldEqual 3
    }

  }

}
