package indigoexts.temporal

import indigo.GameTime.Millis

import org.scalatest.{FunSpec, Matchers}

class ValueOverTimeSpec extends FunSpec with Matchers {

  describe("The changeAmount function (Int)") {

    it("should be able to calculate the amount of change at 10fps") {

      val vot: ValueOverTime[Int] =
        ValueOverTime.intValueOverTime

      vot.changeAmount(Millis(100), 10, Millis(0)) shouldEqual 1
      vot.changeAmount(Millis(200), 10, Millis(0)) shouldEqual 2
      vot.changeAmount(Millis(300), 10, Millis(0)) shouldEqual 3
      vot.changeAmount(Millis(400), 10, Millis(0)) shouldEqual 4
      vot.changeAmount(Millis(500), 10, Millis(0)) shouldEqual 5

    }

    it("should be able to calculate the amount of change") {

      val vot: ValueOverTime[Int] =
        ValueOverTime.intValueOverTime

      vot.changeAmount(Millis(33), 10, Millis(0)) shouldEqual 0
      vot.changeAmount(Millis(33 * 4), 10, Millis(0)) shouldEqual 1
      vot.changeAmount(Millis(17 * 30), 10, Millis(0)) shouldEqual 5
      vot.changeAmount(Millis(33 * 31), 10, Millis(0)) shouldEqual 10

    }

    it("should produce a changing value from a consistently small delta") {

      val vot: ValueOverTime[Int] =
        ValueOverTime.intValueOverTime

      // Almost perfect 30fps for 30 frames @ 10 units per second should equal 10 units completed
      val actual: List[Int] = (1 to 30).toList
        .map(_.toDouble * 33.3)
        .map { runningTime =>
          vot.changeAmount(Millis(runningTime.toLong), 10, Millis(0))
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

      vot.changeAmount(Millis(100), 10f, Millis(0)) shouldEqual 1f
      vot.changeAmount(Millis(200), 10f, Millis(0)) shouldEqual 2f
      vot.changeAmount(Millis(300), 10f, Millis(0)) shouldEqual 3f
      vot.changeAmount(Millis(400), 10f, Millis(0)) shouldEqual 4f
      vot.changeAmount(Millis(500), 10f, Millis(0)) shouldEqual 5f

    }

  }

  describe("The changeAmount function (Double)") {

    it("should be able to calculate the amount of change at 10fps") {

      val vot: ValueOverTime[Double] =
        ValueOverTime.doubleValueOverTime

      vot.changeAmount(Millis(100), 10d, Millis(0)) shouldEqual 1d
      vot.changeAmount(Millis(200), 10d, Millis(0)) shouldEqual 2d
      vot.changeAmount(Millis(300), 10d, Millis(0)) shouldEqual 3d
      vot.changeAmount(Millis(400), 10d, Millis(0)) shouldEqual 4d
      vot.changeAmount(Millis(500), 10d, Millis(0)) shouldEqual 5d

    }

  }

}
