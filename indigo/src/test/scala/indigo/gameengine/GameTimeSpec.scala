package indigo.gameengine

import org.scalatest.{FunSpec, Matchers}

class GameTimeSpec extends FunSpec with Matchers {

  describe("GameTime") {

    it("should be able to calculate the frame duration of the game") {

      val gameTime: GameTime = new GameTime(running = 0, delta = 0, targetFPS = 30)

      Math.round(gameTime.frameDuration) shouldEqual 33

    }

  }

}
