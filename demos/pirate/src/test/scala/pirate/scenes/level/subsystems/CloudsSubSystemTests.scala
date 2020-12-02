package pirate.scenes.level.subsystems

import indigo.GameTime
import indigo.Seconds

class CloudsSubSystemTests extends munit.FunSuite {

  val assetWidth: Int = 100

  test("Calculate next big cloud position.reset if less than zero") {
    val actual =
      CloudsSubSystem.nextBigCloudPosition(GameTime.is(Seconds(1)), -1.0d, assetWidth)

    val expected = assetWidth.toDouble

    assertEquals(actual, expected)
  }

  test("Calculate next big cloud position.move smoothly independant of framerate") {
    val startPosition: Double = 50.0d

    val at: Seconds => Double =
      (t: Seconds) => CloudsSubSystem.nextBigCloudPosition(GameTime.withDelta(Seconds(1), t), startPosition, assetWidth)

    assertEquals(at(Seconds(0.5)), startPosition - (CloudsSubSystem.scrollSpeed / 2))
    assertEquals(at(Seconds(1)), startPosition - (CloudsSubSystem.scrollSpeed))
    assertEquals(at(Seconds(1.5)), startPosition - (CloudsSubSystem.scrollSpeed / 2 * 3))
    assertEquals(at(Seconds(5)), startPosition - (CloudsSubSystem.scrollSpeed * 5))
    assertEquals(at(Seconds(10)), startPosition - (CloudsSubSystem.scrollSpeed * 10))
  }

}
