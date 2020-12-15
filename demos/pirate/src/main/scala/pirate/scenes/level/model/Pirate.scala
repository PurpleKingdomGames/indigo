package pirate.scenes.level.model

import indigo._
import indigoextras.geometry.BoundingBox
import indigoextras.geometry.Vertex
import pirate.core.Assets

final case class Pirate(
    boundingBox: BoundingBox,
    state: PirateState,
    lastRespawn: Seconds,
    ySpeed: Double
) {

  val position: Vertex =
    Vertex(boundingBox.horizontalCenter, boundingBox.bottom)

  def update(gameTime: GameTime, inputState: InputState, platform: Platform): Outcome[Pirate] = {

    val inputForce =
      inputState.mapInputs(Pirate.inputMappings(state.isFalling), Vector2.zero)

    val (nextBounds, collision) =
      Pirate.adjustOnCollision(
        platform,
        boundingBox.moveBy(
          Vertex(inputForce.x, ySpeed) * gameTime.delta.toDouble
        )
      )

    val ySpeedNext: Double =
      Pirate.decideNextSpeedY(state.inMidAir, boundingBox.y, nextBounds.y, ySpeed, inputForce.y)

    val nextState =
      Pirate.nextStateFromForceDiff(
        state,
        collision,
        boundingBox.position.toVector2,
        nextBounds.position.toVector2
      )

    // Respawn if the pirate is below the bottom of the map.
    if (nextBounds.y > platform.rowCount.toDouble + 1)
      Outcome(Pirate(nextBounds.moveTo(Pirate.RespawnPoint), nextState, gameTime.running, ySpeedNext))
        .addGlobalEvents(PlaySound(Assets.Sounds.respawnSound, Volume.Max))
    else {
      val maybeJumpSound =
        if (!state.inMidAir && nextState.isJumping)
          List(PlaySound(Assets.Sounds.jumpSound, Volume.Max))
        else Nil

      Outcome(Pirate(nextBounds, nextState, lastRespawn, ySpeedNext))
        .addGlobalEvents(maybeJumpSound)
    }
  }
}

object Pirate {

  // Where does the captain start in model terms?
  // Right in the middle, and off the top of the screen
  // by 2 units (tiles).
  val RespawnPoint = Vertex(9.5, -2)

  def initial: Pirate = {

    val startPosition = Vertex(9.5, 6)

    // The model space is 1 unit per tile, a tile is 32 x 32.
    // The captain does not take up a whole block. His bounding
    // box is the width of his body (not extremities so that he
    // slides of the edges of platforms), by his standing height.
    // 32 = 1 so 15/32 x 28/32 is a bounding box of
    // (0.46875, 0.875)
    val size = Vertex(15 / 32, 28 / 32)

    Pirate(
      BoundingBox(startPosition, size),
      PirateState.FallingRight,
      Seconds.zero,
      0
    )
  }

  val inputMappings: Boolean => InputMapping[Vector2] = isFalling => {
    val xSpeed: Double = if (isFalling) 2.0d else 3.0d
    val ySpeed: Double = if (isFalling) 0.0d else -8.0d

    InputMapping(
      Combo.withKeyInputs(Key.LEFT_ARROW, Key.UP_ARROW)  -> Vector2(-xSpeed, ySpeed),
      Combo.withKeyInputs(Key.LEFT_ARROW, Key.SPACE)     -> Vector2(-xSpeed, ySpeed),
      Combo.withKeyInputs(Key.LEFT_ARROW)                -> Vector2(-xSpeed, 0.0d),
      Combo.withKeyInputs(Key.RIGHT_ARROW, Key.UP_ARROW) -> Vector2(xSpeed, ySpeed),
      Combo.withKeyInputs(Key.RIGHT_ARROW, Key.SPACE)    -> Vector2(xSpeed, ySpeed),
      Combo.withKeyInputs(Key.RIGHT_ARROW)               -> Vector2(xSpeed, 0.0d),
      Combo.withKeyInputs(Key.UP_ARROW)                  -> Vector2(0.0d, ySpeed),
      Combo.withKeyInputs(Key.SPACE)                     -> Vector2(0.0d, ySpeed),
      Combo.withGamepadInputs(
        GamepadInput.LEFT_ANALOG(_ < -0.5, _ => true, false),
        GamepadInput.Cross
      )                                                                             -> Vector2(-xSpeed, ySpeed),
      Combo.withGamepadInputs(GamepadInput.LEFT_ANALOG(_ < -0.5, _ => true, false)) -> Vector2(-xSpeed, 0.0d),
      Combo.withGamepadInputs(
        GamepadInput.LEFT_ANALOG(_ > 0.5, _ => true, false),
        GamepadInput.Cross
      )                                                                            -> Vector2(xSpeed, ySpeed),
      Combo.withGamepadInputs(GamepadInput.LEFT_ANALOG(_ > 0.5, _ => true, false)) -> Vector2(xSpeed, 0.0d),
      Combo.withGamepadInputs(GamepadInput.Cross)                                  -> Vector2(0.0d, ySpeed)
    )
  }

  def adjustOnCollision(platform: Platform, proposedBounds: BoundingBox): (BoundingBox, Boolean) =
    platform.hitTest(proposedBounds) match {
      case Some(value) =>
        (
          proposedBounds
            .moveTo(proposedBounds.position.withY(value.y - proposedBounds.height)),
          true
        )

      case None =>
        (proposedBounds, false)
    }

  val gravityIncrement: Double = 0.4d

  def decideNextSpeedY(
      inMidAir: Boolean,
      previousY: Double,
      nextY: Double,
      ySpeed: Double,
      inputY: Double
  ): Double =
    if (Math.abs(nextY - previousY) < 0.0001 && !inMidAir)
      gravityIncrement + inputY
    else if (ySpeed + gravityIncrement <= 8.0d)
      ySpeed + gravityIncrement
    else
      8.0d

  def nextStateFromForceDiff(
      previousState: PirateState,
      collisionOccurred: Boolean,
      oldForce: Vector2,
      newForce: Vector2
  ): PirateState = {
    val forceDiff = newForce - oldForce

    if (forceDiff.y > -0.001 && forceDiff.y < 0.001 && collisionOccurred)
      nextStanding(forceDiff.x)
    else if (newForce.y > oldForce.y)
      nextFalling(previousState)(forceDiff.x)
    else
      nextJumping(previousState)(forceDiff.x)

  }

  private def nextStateFromDiffX(
      movingLeft: PirateState,
      movingRight: PirateState,
      otherwise: PirateState
  ): Double => PirateState =
    xDiff =>
      if (xDiff < -0.01) movingLeft
      else if (xDiff > 0.01) movingRight
      else otherwise

  lazy val nextStanding: Double => PirateState =
    nextStateFromDiffX(
      PirateState.MoveLeft,
      PirateState.MoveRight,
      PirateState.Idle
    )

  def nextFalling(previousState: PirateState): Double => PirateState =
    nextStateFromDiffX(
      PirateState.FallingLeft,
      PirateState.FallingRight,
      previousState match {
        case PirateState.FallingLeft | PirateState.JumpingLeft =>
          PirateState.FallingLeft

        case PirateState.FallingRight | PirateState.JumpingRight =>
          PirateState.FallingRight

        case _ =>
          PirateState.FallingRight
      }
    )

  def nextJumping(previousState: PirateState): Double => PirateState =
    nextStateFromDiffX(
      PirateState.JumpingLeft,
      PirateState.JumpingRight,
      previousState match {
        case l @ PirateState.JumpingLeft  => l
        case r @ PirateState.JumpingRight => r
        case _                            => PirateState.JumpingRight
      }
    )

}
