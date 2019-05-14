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
              spawnPosition = Point.zero,
              creationTime = Millis(500),
              lifeExpectancy = Millis(2000),
              age = Millis.zero,
              randomSeedValue = 0,
              initialPayload = None
            )

          seed.progression ==> 0d
          seed.updateDelta(Millis(500)).progression ==> 0.25d
          seed.updateDelta(Millis(1000)).progression ==> 0.5d
          seed.updateDelta(Millis(1500)).progression ==> 0.75d
          seed.updateDelta(Millis(2000)).progression ==> 1d

        }

      }

    }

}
