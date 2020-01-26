package indigo.shared.time

import utest._

object GameTimeTests extends TestSuite {

  import GameTime._
  import indigo.shared.EqualTo._

  val tests: Tests =
    Tests {

      "GameTime" - {

        "should be able to calculate the frame duration of the game" - {

          val gameTime: GameTime = GameTime(Millis.zero, Seconds.zero, targetFPS = FPS(30))

          Math.round(gameTime.frameDuration.value) ==> 33

        }

      }
    }

}
