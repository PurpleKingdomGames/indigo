package indigoexts.timevarying

import indigo.GameTime

import org.scalatest.{FunSpec, Matchers}

class ValueOverTimeSpec extends FunSpec with Matchers {

  describe("The changeAmount function (Int)") {

    it("should be able to calculate the amount of change at 10fps") {

      val vot: ValueOverTime[Int] =
        ValueOverTime.intValueOverTime

      vot.changeAmount(new GameTime(100, 100, 100), 10, 0) shouldEqual 1
      vot.changeAmount(new GameTime(200, 100, 100), 10, 0) shouldEqual 2
      vot.changeAmount(new GameTime(300, 100, 100), 10, 0) shouldEqual 3
      vot.changeAmount(new GameTime(400, 100, 100), 10, 0) shouldEqual 4
      vot.changeAmount(new GameTime(500, 100, 100), 10, 0) shouldEqual 5

    }

    it("should be able to calculate the amount of change") {

      val vot: ValueOverTime[Int] =
        ValueOverTime.intValueOverTime

      vot.changeAmount(new GameTime(33, 33, 30), 10, 0) shouldEqual 0
      vot.changeAmount(new GameTime(33 * 4, 33, 30), 10, 0) shouldEqual 1
      vot.changeAmount(new GameTime(17 * 30, 33, 30), 10, 0) shouldEqual 5
      vot.changeAmount(new GameTime(33 * 31, 33, 30), 10, 0) shouldEqual 10

    }

    it("should produce a changing value from a consistently small delta") {

      val vot: ValueOverTime[Int] =
        ValueOverTime.intValueOverTime

      // Almost perfect 30fps for 30 frames @ 10 units per second should equal 10 units completed
      val actual: List[Int] = (1 to 30).toList
        .map(_.toDouble * 33.3)
        .map { runningTime =>
          vot.changeAmount(new GameTime(runningTime, 33.3, 30), 10, 0)
        }
        .distinct

      val expected: List[Int] = (0 to 9).toList

      actual shouldEqual expected

    }

  }

  describe("The changeAmount function (Float)") {

    it("should be able to calculate the amount of change at 10fps") {

      val vot: ValueOverTime[Float] =
        ValueOverTime.floatValueOverTime

      vot.changeAmount(new GameTime(100, 100, 100), 10f, 0) shouldEqual 1f
      vot.changeAmount(new GameTime(200, 100, 100), 10f, 0) shouldEqual 2f
      vot.changeAmount(new GameTime(300, 100, 100), 10f, 0) shouldEqual 3f
      vot.changeAmount(new GameTime(400, 100, 100), 10f, 0) shouldEqual 4f
      vot.changeAmount(new GameTime(500, 100, 100), 10f, 0) shouldEqual 5f

    }

  }

  describe("The changeAmount function (Double)") {

    it("should be able to calculate the amount of change at 10fps") {

      val vot: ValueOverTime[Double] =
        ValueOverTime.doubleValueOverTime

      vot.changeAmount(new GameTime(100, 100, 100), 10d, 0) shouldEqual 1d
      vot.changeAmount(new GameTime(200, 100, 100), 10d, 0) shouldEqual 2d
      vot.changeAmount(new GameTime(300, 100, 100), 10d, 0) shouldEqual 3d
      vot.changeAmount(new GameTime(400, 100, 100), 10d, 0) shouldEqual 4d
      vot.changeAmount(new GameTime(500, 100, 100), 10d, 0) shouldEqual 5d

    }

  }

}
