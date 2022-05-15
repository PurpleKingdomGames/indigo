package com.example.jobs

import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.time.Millis
import indigo.shared.time.Seconds
import indigoextras.datatypes.IncreaseTo
import indigoextras.datatypes.TimeVaryingValue
import indigoextras.jobs.JobMarketEvent

class TreeTests extends munit.FunSuite {

  val tree = Tree(
    index = 0,
    position = Point.zero,
    growth = IncreaseTo(0, 10, 100),
    fullyGrown = false
  )

  test("Tree update function.several growth iterations") {
    val actual =
      tree
        .update(Millis(17).toSeconds)
        .unsafeGet
        .update(Millis(17).toSeconds)
        .unsafeGet
        .update(Millis(17).toSeconds)
        .unsafeGet
        .update(Millis(17).toSeconds)
        .unsafeGet
        .update(Millis(17).toSeconds)
        .unsafeGet
        .update(Millis(17).toSeconds)
        .unsafeGet
        .update(Millis(17).toSeconds)
        .unsafeGet
        .update(Millis(17).toSeconds)
        .unsafeGet
        .update(Millis(17).toSeconds)
        .unsafeGet
        .update(Millis(17).toSeconds)

    // Slightly confusing at first glance - remember the growth rate.
    // 10 iterations of 0.017 Seconds = 0.17 * growth rate of 10 = 1.7
    assertEquals(Math.round(actual.unsafeGet.growth.value * 10d) / 10d, 1.7)
  }

  test("Tree update function.general growth 100 millis") {
    val actual =
      tree.update(Seconds(0.1))

    val expected =
      tree.copy(
        growth = IncreaseTo(1, 10, 100),
        fullyGrown = false
      )

    assertEquals(actual.unsafeGet, expected)
    assertEquals(actual.unsafeGlobalEvents, Batch.empty)
  }

  test("Tree update function.general growth 1000 millis") {
    val actual =
      tree.update(Seconds(1))

    val expected =
      tree.copy(
        growth = IncreaseTo(10, 10, 100),
        fullyGrown = false
      )

    assertEquals(actual.unsafeGet, expected)
    assertEquals(actual.unsafeGlobalEvents, Batch.empty)
  }

  test("Tree update function.fully grown") {
    val actual =
      tree.update(Seconds(10))

    val expected =
      tree.copy(
        growth = IncreaseTo(100, 10, 100),
        fullyGrown = true
      )

    assertEquals(actual.unsafeGet, expected)
    assertEquals(actual.unsafeGlobalEvents, Batch(JobMarketEvent.Post(ChopDown(0, Point.zero))))
  }

}
