package pirate.scenes.level.model

import indigo.*
import indigo.syntax.*
import indigo.physics.World

/*
The model cannot be initialised at game start up, because we want to load
some data during the loading screen, parse it, and use it to generate part
of the model. We _could_ represent that with an Option, but that could get
messy.
 */
enum LevelModel(val notReady: Boolean):
  case NotReady                                                        extends LevelModel(true)
  case Ready(pirate: Pirate, platform: Platform, world: World[String]) extends LevelModel(false)

object LevelModel:

  extension (lm: LevelModel)
    def update(gameTime: GameTime, inputState: InputState): Outcome[LevelModel] =
      lm match
        case NotReady =>
          Outcome(lm)

        case Ready(pirate, platform, world) =>
          world.update(gameTime.delta).merge(pirate.update(gameTime, inputState, platform)) { case (w, p) =>
            Ready(p, platform, w)
          }
