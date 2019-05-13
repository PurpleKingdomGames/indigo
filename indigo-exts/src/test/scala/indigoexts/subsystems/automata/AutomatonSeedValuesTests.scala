package indigoexts.subsystems.automata

import utest._
import indigo.shared.datatypes.Point
import indigo.shared.time.Millis

object AutomataSeedValuesTests extends TestSuite {

  val tests: Tests =
    Tests {

      "Progression" - {

        "a progression multiplier should range from 0 to 1 as the life of an automaton expires" - {

          val seed =
            AutomatonSeedValues(
              spawnedAt = Point.zero,
              createdAt = Millis(500),
              lifeSpan = Millis(2000),
              timeAliveDelta = Millis.zero,
              randomSeed = 0
            )

          seed.progression ==> 0d
          seed.copy(timeAliveDelta = Millis(500)).progression ==> 0.25d
          seed.copy(timeAliveDelta = Millis(1000)).progression ==> 0.5d
          seed.copy(timeAliveDelta = Millis(1500)).progression ==> 0.75d
          seed.copy(timeAliveDelta = Millis(2000)).progression ==> 1d

        }

      }

    }

}
