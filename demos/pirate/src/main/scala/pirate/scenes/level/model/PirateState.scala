package pirate.scenes.level.model

/*
An ADT of all the states the pirate can find himself in.
 */
enum PirateState(val isFalling: Boolean, val isJumping: Boolean, val inMidAir: Boolean):
  case Idle         extends PirateState(false, false, false)
  case MoveLeft     extends PirateState(false, false, false)
  case MoveRight    extends PirateState(false, false, false)
  case FallingLeft  extends PirateState(true, false, true)
  case FallingRight extends PirateState(true, false, true)
  case JumpingLeft  extends PirateState(false, true, true)
  case JumpingRight extends PirateState(false, true, true)
