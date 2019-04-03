package indigoexts.temporal

import indigo.time.Millis

import org.scalatest.{FunSpec, Matchers}

class TimeVaryingValueSpec extends FunSpec with Matchers {

  val millis0: Millis   = Millis(0)
  val millis100: Millis = Millis(100)

  describe("increasing") {

    it("should increase one value over time.") {
      TimeVaryingValue(0, millis0).increase(10, millis100).value shouldEqual 1
    }

    it("should do a number of iterations over time") {
      val runningTimes: List[Millis] =
        (1 to 10).toList.map(_ * 100).map(r => Millis(r))

      val actual: TimeVaryingValue[Int] =
        runningTimes.foldLeft(TimeVaryingValue(0, millis0))((tv, rt) => tv.increase(10, rt))

      val expected: TimeVaryingValue[Int] =
        new TimeVaryingValue(10, 0, Millis(0))

      actual === expected shouldEqual true
    }

  }

  describe("increasing capped") {

    it("should increase one value over time.") {
      TimeVaryingValue(0, millis0).increaseTo(100, 10, Millis((33.3 * 4).toLong)).value shouldEqual 1
      TimeVaryingValue(0, millis0).increaseTo(100, 10, Millis(50000)).value shouldEqual 100
    }

    it("should do a number of iterations over time up to a limit") {
      val runningTimes: List[Millis] =
        (1 to 10).toList.map(_ * 100).map(r => Millis(r))

      val actual: TimeVaryingValue[Int] =
        runningTimes.foldLeft(TimeVaryingValue(0, millis0))((tv, rt) => tv.increaseTo(5, 10, rt))

      val expected: TimeVaryingValue[Int] =
        new TimeVaryingValue(5, 0, Millis(0))

      actual === expected shouldEqual true
    }

  }

  describe("increasing wrapped") {

    it("should increase one value over time.") {
      withClue("Case A") {
        TimeVaryingValue(0, millis0)
          .increaseWrapAt(3, 10, Millis(100))
          .value shouldEqual 1
      }

      withClue("Case B") {
        TimeVaryingValue(0, millis0)
          .increaseWrapAt(3, 10, Millis(100))
          .increaseWrapAt(3, 10, Millis(200))
          .value shouldEqual 2
      }

      withClue("Case C") {
        TimeVaryingValue(0, millis0)
          .increaseWrapAt(3, 10, Millis(100))
          .increaseWrapAt(3, 10, Millis(200))
          .increaseWrapAt(3, 10, Millis(300))
          .value shouldEqual 3
      }

      withClue("Case D") {
        TimeVaryingValue(0, millis0)
          .increaseWrapAt(3, 10, Millis(100))
          .increaseWrapAt(3, 10, Millis(200))
          .increaseWrapAt(3, 10, Millis(300))
          .increaseWrapAt(3, 10, Millis(400))
          .value shouldEqual 0
      }

      withClue("Case E") {
        TimeVaryingValue(0, millis0)
          .increaseWrapAt(3, 10, Millis(100))
          .increaseWrapAt(3, 10, Millis(200))
          .increaseWrapAt(3, 10, Millis(300))
          .increaseWrapAt(3, 10, Millis(400))
          .increaseWrapAt(3, 10, Millis(500))
          .value shouldEqual 1
      }
    }

  }

  describe("decreasing") {

    it("should decrease one value over time.") {
      TimeVaryingValue(0, millis0).decrease(10, millis100).value shouldEqual -1
    }

    it("should do a number of iterations over time") {
      val runningTimes: List[Millis] =
        (1 to 10).toList.map(_ * 100).map(r => Millis(r))

      val actual: TimeVaryingValue[Int] =
        runningTimes.foldLeft(TimeVaryingValue(0, millis0))((tv, rt) => tv.decrease(10, rt))

      val expected: TimeVaryingValue[Int] =
        new TimeVaryingValue(-10, 0, Millis(0))

      actual === expected shouldEqual true
    }

  }

  describe("decreasing capped") {

    it("should do a number of iterations over time down to a limit") {
      val runningTimes: List[Millis] =
        (1 to 10).toList.map(_ * 100).map(r => Millis(r))

      val actual: TimeVaryingValue[Int] =
        runningTimes.foldLeft(TimeVaryingValue(0, millis0))((tv, rt) => tv.decreaseTo(-5, 10, rt))

      val expected: TimeVaryingValue[Int] =
        new TimeVaryingValue(-5, 0, Millis(0))

      actual === expected shouldEqual true
    }

  }

  describe("decreasing wrapped") {

    it("should decrease one value over time.") {
      withClue("Case A") {
        TimeVaryingValue(0, millis0)
          .decreaseWrapAt(3, 10, Millis(100))
          .value shouldEqual -1
      }

      withClue("Case B") {
        TimeVaryingValue(0, millis0)
          .decreaseWrapAt(3, 10, Millis(100))
          .decreaseWrapAt(3, 10, Millis(200))
          .value shouldEqual -2
      }

      withClue("Case C") {
        TimeVaryingValue(0, millis0)
          .decreaseWrapAt(3, 10, Millis(100))
          .decreaseWrapAt(3, 10, Millis(200))
          .decreaseWrapAt(3, 10, Millis(300))
          .value shouldEqual -3
      }

      withClue("Case D") {
        TimeVaryingValue(0, millis0)
          .decreaseWrapAt(3, 10, Millis(100))
          .decreaseWrapAt(3, 10, Millis(200))
          .decreaseWrapAt(3, 10, Millis(300))
          .decreaseWrapAt(3, 10, Millis(400))
          .value shouldEqual 0
      }

      withClue("Case E") {
        TimeVaryingValue(0, millis0)
          .decreaseWrapAt(3, 10, Millis(100))
          .decreaseWrapAt(3, 10, Millis(200))
          .decreaseWrapAt(3, 10, Millis(300))
          .decreaseWrapAt(3, 10, Millis(400))
          .decreaseWrapAt(3, 10, Millis(500))
          .value shouldEqual -1
      }
    }

  }

}
