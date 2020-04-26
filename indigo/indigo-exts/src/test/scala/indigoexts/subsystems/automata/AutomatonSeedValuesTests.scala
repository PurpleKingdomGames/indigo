package indigoexts.subsystems.automata

import utest._
import indigo.shared.datatypes.Point
import indigo.shared.time.Seconds

object AutomataSeedValuesTests extends TestSuite {

  val tests: Tests =
    Tests {

      "Progression" - {

        "a progression multiplier should range from 0 to 1 as the life of an automaton expires" - {

          val seed =
            AutomatonSeedValues(
              spawnPosition = Point.zero,
              creationTime = Seconds(0.5),
              lifeExpectancy = Seconds(2),
              age = Seconds.zero,
              randomSeedValue = 0,
              initialPayload = None
            )

          seed.progression ==> 0d
          seed.updateDelta(Seconds(0.5)).progression ==> 0.25d
          seed.updateDelta(Seconds(1)).progression ==> 0.5d
          seed.updateDelta(Seconds(1.5)).progression ==> 0.75d
          seed.updateDelta(Seconds(2)).progression ==> 1d

        }

      }

    }

}
