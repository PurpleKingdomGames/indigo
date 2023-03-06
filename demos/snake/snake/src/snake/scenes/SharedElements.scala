package snake.scenes

import indigo.*

import snake.init.GameAssets

object SharedElements:

  def drawHitSpaceToStart(center: Int, blinkDelay: Seconds, gameTime: GameTime): Batch[SceneNode] =
    Signal
      .Pulse(blinkDelay)
      .map { on =>
        if (on)
          Batch(Text("hit space to start", center, 220, 1, GameAssets.fontKey, GameAssets.fontMaterial).alignCenter)
        else Batch.empty
      }
      .at(gameTime.running)
