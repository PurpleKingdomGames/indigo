package snake.scenes

import indigo._

import snake.init.GameAssets

object SharedElements {

  def drawHitSpaceToStart(center: Int, blinkDelay: Seconds, gameTime: GameTime): List[SceneNode] =
    Signal
      .Pulse(blinkDelay)
      .map { on =>
        if (on)
          List(Text("hit space to start", center, 220, 1, GameAssets.fontKey, GameAssets.fontMaterial).alignCenter)
        else Nil
      }
      .at(gameTime.running)

}
