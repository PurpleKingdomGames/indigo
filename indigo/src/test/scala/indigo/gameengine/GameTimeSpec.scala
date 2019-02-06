package indigo.gameengine

import org.scalatest.{FunSpec, Matchers}

class GameTimeSpec extends FunSpec with Matchers {

  describe("GameTime") {

    it("should be able to calculate the fps of the game") {

      val gameTime: GameTime = GameTime(running = 0, delta = 0, frameDuration = 33.3)

      gameTime.fps shouldEqual 30

    }

  }

}
