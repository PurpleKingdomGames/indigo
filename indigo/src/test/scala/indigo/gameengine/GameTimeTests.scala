package indigo.gameengine

import utest._

object GameTimeTests extends TestSuite {

  import GameTime._
  import indigo.EqualTo._

  val tests: Tests =
    Tests {

      "GameTime" - {

        "should be able to calculate the frame duration of the game" - {

          val gameTime: GameTime = GameTime(Millis.zero, Millis.zero, targetFPS = FPS(30), Millis.zero)

          Math.round(gameTime.frameDuration.value) ==> 33

        }

        // "should be able to jump forward in time" - {
        //   val actual   = GameTime.zero.forwardInTimeBy(Millis(1000))
        //   val expected = GameTime(Millis(1000), Millis(0), FPS.Default)

        //   actual === expected ==> true
        // }

        // "should be able to jump back in time" - {
        //   val actual   = GameTime(Millis(2000), Millis(0), FPS.Default).backInTimeBy(Millis(1000))
        //   val expected = GameTime(Millis(1000), Millis(0), FPS.Default)

        //   actual === expected ==> true
        // }

      }
    }

}
