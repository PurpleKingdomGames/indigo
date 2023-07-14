package pirate.scenes.level.model

import indigo.*
import indigo.syntax.*
import indigo.physics.World
import pirate.core.Assets

/*
The model cannot be initialised at game start up, because we want to load
some data during the loading screen, parse it, and use it to generate part
of the model. We _could_ represent that with an Option, but that could get
messy.
 */
enum LevelModel:
  case NotReady
  case Ready(pirate: Pirate, platform: Platform, world: World[String])

  def notReady: Boolean =
    this match
      case NotReady                       => true
      case Ready(pirate, platform, world) => false

  def update(gameTime: GameTime, inputState: InputState): Outcome[LevelModel] =
    this match
      case NotReady =>
        Outcome(this)

      case Ready(pirate, platform, world) =>
        val inputForce =
          inputState.mapInputs(Pirate.inputMappings(pirate.state.inMidAir), Vector2.zero)

        val currentY = world.findByTag("pirate").map(_.position.y)

        world
          .modifyByTag("pirate") { p =>
            p.withVelocity(Vector2(inputForce.x, p.velocity.y + inputForce.y))
          }
          .update(gameTime.delta)
          .flatMap { w =>
            w.findByTag("pirate").headOption match
              case None =>
                Outcome(Ready(pirate, platform, w))

              case Some(p) =>
                val yDiff =
                  Math.abs(p.position.y - currentY.headOption.getOrElse(0.0))
                val nextState =
                  Pirate.decideNextState(pirate.state, p.velocity, inputForce, yDiff)

                // Respawn if the pirate is below the bottom of the map.
                val nextPirate =
                  if p.position.y > platform.rowCount.toDouble + 1 then
                    Outcome(Pirate(nextState, gameTime.running))
                      .addGlobalEvents(
                        PlaySound(Assets.Sounds.respawnSound, Volume.Max),
                        PirateRespawn(Pirate.respawnPoint)
                      )
                  else
                    val maybeJumpSound =
                      if (!pirate.state.inMidAir && nextState.isJumping)
                        Batch(PlaySound(Assets.Sounds.jumpSound, Volume.Max))
                      else Batch.empty

                    Outcome(Pirate(nextState, pirate.lastRespawn))
                      .addGlobalEvents(maybeJumpSound)

                nextPirate.map(p => Ready(p, platform, w))
          }
