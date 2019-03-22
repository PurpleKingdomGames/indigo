package indigoexts.temporal

import indigo.GameTime

import org.scalatest.{FunSpec, Matchers}

class TimeVaryingValueSpec extends FunSpec with Matchers {

  implicit def intToMillis(i: Int): GameTime.Millis =
    GameTime.Millis(i.toDouble)

  implicit def doubleToMillis(d: Double): GameTime.Millis =
    GameTime.Millis(d)

  val fps10: GameTime.FPS = GameTime.FPS(10)
  val fps33: GameTime.FPS = GameTime.FPS(33)

  val gameTime0: GameTime   = new GameTime(0, 0, fps10)
  val gameTime100: GameTime = new GameTime(100, 100, fps10)

  describe("increasing") {

    it("should increase one value over time.") {
      TimeVaryingValue(0, gameTime0).increase(10, gameTime100).value shouldEqual 1
    }

    it("should do a number of iterations over time") {
      val runningTimes: List[GameTime] =
        (1 to 10).toList.map(_ * 100).map(r => new GameTime(r, 100, fps10))

      val actual: TimeVaryingValue[Int] =
        runningTimes.foldLeft(TimeVaryingValue(0, gameTime0))((tv, rt) => tv.increase(10, rt))

      val expected: TimeVaryingValue[Int] =
        new TimeVaryingValue(10, 0, 0)

      actual === expected shouldEqual true
    }

  }

  describe("increasing capped") {

    it("should increase one value over time.") {
      TimeVaryingValue(0, gameTime0).increaseTo(100, 10, new GameTime(33.3 * 4, 33, fps33)).value shouldEqual 1
      TimeVaryingValue(0, gameTime0).increaseTo(100, 10, new GameTime(50000, 33, fps33)).value shouldEqual 100
    }

    it("should do a number of iterations over time up to a limit") {
      val runningTimes: List[GameTime] =
        (1 to 10).toList.map(_ * 100).map(r => new GameTime(r, 100, fps10))

      val actual: TimeVaryingValue[Int] =
        runningTimes.foldLeft(TimeVaryingValue(0, gameTime0))((tv, rt) => tv.increaseTo(5, 10, rt))

      val expected: TimeVaryingValue[Int] =
        new TimeVaryingValue(5, 0, 0)

      actual === expected shouldEqual true
    }

  }

  describe("increasing wrapped") {

    it("should increase one value over time.") {
      withClue("Case A") {
        TimeVaryingValue(0, gameTime0)
          .increaseWrapAt(3, 10, new GameTime(100, 100, fps10))
          .value shouldEqual 1
      }

      withClue("Case B") {
        TimeVaryingValue(0, gameTime0)
          .increaseWrapAt(3, 10, new GameTime(100, 100, fps10))
          .increaseWrapAt(3, 10, new GameTime(200, 100, fps10))
          .value shouldEqual 2
      }

      withClue("Case C") {
        TimeVaryingValue(0, gameTime0)
          .increaseWrapAt(3, 10, new GameTime(100, 100, fps10))
          .increaseWrapAt(3, 10, new GameTime(200, 100, fps10))
          .increaseWrapAt(3, 10, new GameTime(300, 100, fps10))
          .value shouldEqual 3
      }

      withClue("Case D") {
        TimeVaryingValue(0, gameTime0)
          .increaseWrapAt(3, 10, new GameTime(100, 100, fps10))
          .increaseWrapAt(3, 10, new GameTime(200, 100, fps10))
          .increaseWrapAt(3, 10, new GameTime(300, 100, fps10))
          .increaseWrapAt(3, 10, new GameTime(400, 100, fps10))
          .value shouldEqual 0
      }

      withClue("Case E") {
        TimeVaryingValue(0, gameTime0)
          .increaseWrapAt(3, 10, new GameTime(100, 100, fps10))
          .increaseWrapAt(3, 10, new GameTime(200, 100, fps10))
          .increaseWrapAt(3, 10, new GameTime(300, 100, fps10))
          .increaseWrapAt(3, 10, new GameTime(400, 100, fps10))
          .increaseWrapAt(3, 10, new GameTime(500, 100, fps10))
          .value shouldEqual 1
      }
    }

  }

  describe("decreasing") {

    it("should decrease one value over time.") {
      TimeVaryingValue(0, gameTime0).decrease(10, gameTime100).value shouldEqual -1
    }

    it("should do a number of iterations over time") {
      val runningTimes: List[GameTime] =
        (1 to 10).toList.map(_ * 100).map(r => new GameTime(r, 100, fps10))

      val actual: TimeVaryingValue[Int] =
        runningTimes.foldLeft(TimeVaryingValue(0, gameTime0))((tv, rt) => tv.decrease(10, rt))

      val expected: TimeVaryingValue[Int] =
        new TimeVaryingValue(-10, 0, 0)

      actual === expected shouldEqual true
    }

  }

  describe("decreasing capped") {

    it("should do a number of iterations over time down to a limit") {
      val runningTimes: List[GameTime] =
        (1 to 10).toList.map(_ * 100).map(r => new GameTime(r, 100, fps10))

      val actual: TimeVaryingValue[Int] =
        runningTimes.foldLeft(TimeVaryingValue(0, gameTime0))((tv, rt) => tv.decreaseTo(-5, 10, rt))

      val expected: TimeVaryingValue[Int] =
        new TimeVaryingValue(-5, 0, 0)

      actual === expected shouldEqual true
    }

  }

  describe("decreasing wrapped") {

    it("should decrease one value over time.") {
      withClue("Case A") {
        TimeVaryingValue(0, gameTime0)
          .decreaseWrapAt(3, 10, new GameTime(100, 100, fps10))
          .value shouldEqual -1
      }

      withClue("Case B") {
        TimeVaryingValue(0, gameTime0)
          .decreaseWrapAt(3, 10, new GameTime(100, 100, fps10))
          .decreaseWrapAt(3, 10, new GameTime(200, 100, fps10))
          .value shouldEqual -2
      }

      withClue("Case C") {
        TimeVaryingValue(0, gameTime0)
          .decreaseWrapAt(3, 10, new GameTime(100, 100, fps10))
          .decreaseWrapAt(3, 10, new GameTime(200, 100, fps10))
          .decreaseWrapAt(3, 10, new GameTime(300, 100, fps10))
          .value shouldEqual -3
      }

      withClue("Case D") {
        TimeVaryingValue(0, gameTime0)
          .decreaseWrapAt(3, 10, new GameTime(100, 100, fps10))
          .decreaseWrapAt(3, 10, new GameTime(200, 100, fps10))
          .decreaseWrapAt(3, 10, new GameTime(300, 100, fps10))
          .decreaseWrapAt(3, 10, new GameTime(400, 100, fps10))
          .value shouldEqual 0
      }

      withClue("Case E") {
        TimeVaryingValue(0, gameTime0)
          .decreaseWrapAt(3, 10, new GameTime(100, 100, fps10))
          .decreaseWrapAt(3, 10, new GameTime(200, 100, fps10))
          .decreaseWrapAt(3, 10, new GameTime(300, 100, fps10))
          .decreaseWrapAt(3, 10, new GameTime(400, 100, fps10))
          .decreaseWrapAt(3, 10, new GameTime(500, 100, fps10))
          .value shouldEqual -1
      }
    }

  }

}
