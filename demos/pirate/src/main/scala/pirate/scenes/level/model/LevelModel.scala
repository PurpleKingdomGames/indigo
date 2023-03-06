package pirate.scenes.level.model

import indigo.*

/*
The model cannot be initialised at game start up, because we want to load
some data during the loading screen, parse it, and use it to generate part
of the model. We _could_ represent that with an Option, but that could get
messy.
 */
enum LevelModel(val notReady: Boolean):
  case NotReady                                  extends LevelModel(true)
  case Ready(pirate: Pirate, platform: Platform) extends LevelModel(false)

object LevelModel:

  extension (lm: LevelModel)
    def update(gameTime: GameTime, inputState: InputState): Outcome[LevelModel] =
      lm match
        case NotReady =>
          Outcome(lm)

        case Ready(pirate, platform) =>
          pirate.update(gameTime, inputState, platform).map { p =>
            Ready(p, platform)
          }
