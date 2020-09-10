package pirate.scenes.level.subsystems

import utest._

import indigo.GameTime
import indigo.Seconds

object CloudsSubSystemTests extends TestSuite {

  val tests: Tests =
    Tests {

      "Calculate next big cloud position" - {

        val assetWidth: Int = 100

        "reset if less than zero" - {
          val actual =
            CloudsSubSystem.nextBigCloudPosition(GameTime.is(Seconds(1)), -1.0d, assetWidth)

          val expected = assetWidth.toDouble

          actual ==> expected
        }

        "move smoothly independant of framerate" - {
          val startPosition: Double = 50.0d

          val at: Seconds => Double =
            (t: Seconds) =>
              CloudsSubSystem.nextBigCloudPosition(GameTime.withDelta(Seconds(1), t), startPosition, assetWidth)

          at(Seconds(0.5)) ==> startPosition - (CloudsSubSystem.scrollSpeed / 2)
          at(Seconds(1)) ==> startPosition - (CloudsSubSystem.scrollSpeed)
          at(Seconds(1.5)) ==> startPosition - (CloudsSubSystem.scrollSpeed / 2 * 3)
          at(Seconds(5)) ==> startPosition - (CloudsSubSystem.scrollSpeed * 5)
          at(Seconds(10)) ==> startPosition - (CloudsSubSystem.scrollSpeed * 10)
        }

      }

    }

}
