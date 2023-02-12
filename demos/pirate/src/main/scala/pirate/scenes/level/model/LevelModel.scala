package pirate.scenes.level.model

import indigo._

/*
The model cannot be initialised at game start up, because we want to load
some data during the loading screen, parse it, and use it to generate part
of the model. We _could_ represent that with an Option, but that could get
messy.
 */
sealed trait LevelModel derives CanEqual {
  val notReady: Boolean

  def update(gameTime: GameTime, inputState: InputState): Outcome[LevelModel]
}
object LevelModel {
  case object NotReady extends LevelModel {
    val notReady: Boolean = true

    def update(gameTime: GameTime, inputState: InputState): Outcome[NotReady.type] =
      Outcome(this)
  }

  final case class Ready(pirate: Pirate, platform: Platform) extends LevelModel {
    val notReady: Boolean = false

    def update(gameTime: GameTime, inputState: InputState): Outcome[Ready] =
      pirate.update(gameTime, inputState, platform).map { p =>
        this.copy(pirate = p)
      }

  }
}
