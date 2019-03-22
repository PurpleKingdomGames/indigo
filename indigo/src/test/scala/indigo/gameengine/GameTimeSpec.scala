package indigo.gameengine

import org.scalatest.{FunSpec, Matchers}

class GameTimeSpec extends FunSpec with Matchers {

  describe("GameTime") {

    it("should be able to calculate the frame duration of the game") {

      val gameTime: GameTime = new GameTime(running = GameTime.Millis.zero, delta = GameTime.Millis.zero, targetFPS = GameTime.FPS(30))

      Math.round(gameTime.frameDuration.value) shouldEqual 33

    }

  }

}
