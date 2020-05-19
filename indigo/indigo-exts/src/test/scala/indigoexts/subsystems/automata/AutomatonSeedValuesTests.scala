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
            new AutomatonSeedValues(
              spawnedAt = Point.zero,
              createdAt = Seconds(0.5),
              lifeSpan = Seconds(2),
              timeAliveDelta = Seconds.zero,
              randomSeed = 0,
              payload = None
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
