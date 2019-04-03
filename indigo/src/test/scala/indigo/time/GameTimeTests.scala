package indigo.time

import utest._

object GameTimeTests extends TestSuite {

  import GameTime._
  import indigo.shared.EqualTo._

  val tests: Tests =
    Tests {

      "GameTime" - {

        "should be able to calculate the frame duration of the game" - {

          val gameTime: GameTime = GameTime(Millis.zero, Millis.zero, targetFPS = FPS(30), Millis.zero)

          Math.round(gameTime.frameDuration.value) ==> 33

        }

      }
    }

}
