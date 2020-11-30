package indigoextras.datatypes

import indigo.shared.time.{Seconds, Millis}
import indigo.shared.EqualTo._

class TimeVaryingValueTests extends munit.FunSuite {

  test("increasing.should increase one value over time.") {
    assertEquals(Increasing(0, 10).update(Seconds(0.1)).value, 1.0)
  }

  test("increasing.should do a number of iterations over time") {
    val increments: List[Seconds] =
      (1 to 10).toList.map(r => Millis(100).toSeconds)

    val actual: TimeVaryingValue =
      increments.foldLeft(Increasing(0, 10))((tv, rt) => tv.update(rt))

    val expected: TimeVaryingValue =
      Increasing(10, 10)

    assertEquals(actual, expected)
  }

  test("increasing capped.should increase one value over time.") {
    assertEquals(IncreaseTo(0, 10, 100).update(Millis((33.3 * 4).toLong).toSeconds).toInt, 1)
    assertEquals(IncreaseTo(0, 10, 100).update(Millis(50000).toSeconds).value, 100.0)
  }

  test("increasing capped.should do a number of iterations over time up to a limit") {
    val increments: List[Seconds] =
      (1 to 11).toList.map(_ * 100).map(r => Millis(r).toSeconds)

    val actual: IncreaseTo =
      increments.foldLeft(IncreaseTo(0, 10, 5))((tv, rt) => tv.update(rt))

    val expected: IncreaseTo =
      IncreaseTo(5, 10, 5)

    assertEquals(actual, expected)
  }

  test("increasing wrapped.should increase one value over time.Case A") {
    assertEquals(
      IncreaseWrapAt(10, 3)
        .update(Millis(100).toSeconds)
        .value,
      1.0
    )
  }

  test("increasing wrapped.should increase one value over time.Case B") {
    assertEquals(
      IncreaseWrapAt(10, 3)
        .update(Millis(100).toSeconds) // 1
        .update(Millis(100).toSeconds) // 2
        .value,
      2.0
    )
  }

  test("increasing wrapped.should increase one value over time.Case C") {
    assertEquals(
      IncreaseWrapAt(10, 3)
        .update(Millis(100).toSeconds) // 1
        .update(Millis(100).toSeconds) // 2
        .update(Millis(100).toSeconds) // 3
        .value,
      3.0
    )
  }

  test("increasing wrapped.should increase one value over time.Case D") {
    assertEquals(
      IncreaseWrapAt(10, 3)
        .update(Millis(100).toSeconds) // 1
        .update(Millis(100).toSeconds) // 2
        .update(Millis(100).toSeconds) // 3
        .update(Millis(100).toSeconds) // 0
        .value,
      0.0
    )
  }

  test("increasing wrapped.should increase one value over time.Case E") {
    assertEquals(
      IncreaseWrapAt(10, 3)
        .update(Millis(100).toSeconds) // 1
        .update(Millis(100).toSeconds) // 2
        .update(Millis(100).toSeconds) // 3
        .update(Millis(100).toSeconds) // 0
        .update(Millis(100).toSeconds) // 1
        .value,
      1.0
    )
  }

  test("decreasing.should decrease one value over time.") {
    assertEquals(Decreasing(0, 10).update(Seconds(0.1)).value, -1.0)
  }

  test("decreasing.should do a number of iterations over time") {
    val increments: List[Seconds] =
      (1 to 10).toList.map(_ => Millis(100).toSeconds)

    val actual: TimeVaryingValue =
      increments.foldLeft(Decreasing(0, 10))((tv, rt) => tv.update(rt))

    val expected: TimeVaryingValue =
      Decreasing(-10, 10)

    assertEquals(actual, expected)
  }

  test("decreasing capped.should do a number of iterations over time down to a limit") {
    val increments: List[Seconds] =
      (1 to 10).toList.map(_ * 100).map(r => Millis(r).toSeconds)

    val actual: TimeVaryingValue =
      increments.foldLeft(DecreaseTo(0, 10, -5))((tv, rt) => tv.update(rt))

    val expected: TimeVaryingValue =
      new DecreaseTo(-5, 10, -5)

    assertEquals(actual, expected)
  }

  test("decreasing wrapped.should decrease one value over time.Case A") {
    assertEquals(
      DecreaseWrapAt(10, 3)
        .update(Millis(100).toSeconds)
        .value,
      -1.0
    )
  }

  test("decreasing wrapped.should decrease one value over time.Case B") {
    assertEquals(
      DecreaseWrapAt(10, 3)
        .update(Millis(100).toSeconds)
        .update(Millis(100).toSeconds)
        .value,
      -2.0
    )
  }

  test("decreasing wrapped.should decrease one value over time.Case C") {
    assertEquals(
      DecreaseWrapAt(10, 3)
        .update(Millis(100).toSeconds)
        .update(Millis(100).toSeconds)
        .update(Millis(100).toSeconds)
        .value,
      -3.0
    )
  }

  test("decreasing wrapped.should decrease one value over time.Case D") {
    assertEquals(
      DecreaseWrapAt(10, 3)
        .update(Millis(100).toSeconds)
        .update(Millis(100).toSeconds)
        .update(Millis(100).toSeconds)
        .update(Millis(100).toSeconds)
        .value,
      0.0
    )
  }

  test("decreasing wrapped.should decrease one value over time.Case E") {
    assertEquals(
      DecreaseWrapAt(10, 3)
        .update(Millis(100).toSeconds)
        .update(Millis(100).toSeconds)
        .update(Millis(100).toSeconds)
        .update(Millis(100).toSeconds)
        .update(Millis(100).toSeconds)
        .value,
      -1.0
    )
  }

}
