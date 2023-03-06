package pirate.scenes.level.viewmodel

import indigo.*
import pirate.scenes.level.model.Pirate
import pirate.core.Assets
import pirate.scenes.level.model.PirateState

/*
The model that describes the level is concerned with abstract
boxes floating through space, and is very important to the games
core mechanics.

However, that data isn't enough to make the experience nice for
the player. We need to store some transient state is important
for the presentation, but meaningless in terms of the pure mechanics
of the game.

Specifically, we need to remember which way the player was facing last
(When standing Idle, is he casually facing left or right - depends what
he did last!), and when the walking sound was last played so that we
don't play it too often.

We could store these values in the model, but the model doesn't care
and this way is a bit cleaner.
 */
final case class PirateViewState(
    facingRight: Boolean,
    soundLastPlayed: Seconds
):

  def update(gameTime: GameTime, pirate: Pirate): Outcome[PirateViewState] =
    pirate.state match
      case PirateState.Idle =>
        Outcome(this)

      case PirateState.MoveLeft =>
        val (walkingSound, lastPlayed) = updateWalkSound(gameTime, soundLastPlayed)

        Outcome(
          this.copy(
            facingRight = false,
            soundLastPlayed = lastPlayed
          )
        ).addGlobalEvents(walkingSound)

      case PirateState.MoveRight =>
        val (walkingSound, lastPlayed) = updateWalkSound(gameTime, soundLastPlayed)

        Outcome(
          this.copy(
            facingRight = true,
            soundLastPlayed = lastPlayed
          )
        ).addGlobalEvents(walkingSound)

      case PirateState.FallingLeft =>
        Outcome(this.copy(facingRight = false))

      case PirateState.FallingRight =>
        Outcome(this.copy(facingRight = true))

      case PirateState.JumpingLeft =>
        Outcome(this.copy(facingRight = false))

      case PirateState.JumpingRight =>
        Outcome(this.copy(facingRight = true))

  def updateWalkSound(gameTime: GameTime, soundLastPlayed: Seconds): (Batch[GlobalEvent], Seconds) =
    if gameTime.running > soundLastPlayed + Seconds(0.25) then
      (Batch(PlaySound(Assets.Sounds.walkSound, Volume(0.5d))), gameTime.running)
    else (Batch.empty, soundLastPlayed)

object PirateViewState:

  val initial: PirateViewState =
    PirateViewState(true, Seconds.zero)
