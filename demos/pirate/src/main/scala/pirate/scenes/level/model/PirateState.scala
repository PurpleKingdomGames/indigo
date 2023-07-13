package pirate.scenes.level.model

/*
An ADT of all the states the pirate can find himself in.
 */
enum PirateState:
  case Idle
  case MoveLeft
  case MoveRight
  case FallingLeft
  case FallingRight
  case JumpingLeft
  case JumpingRight

  def isGrounded: Boolean =
    !inMidAir

  def isFalling: Boolean =
    this match
      case Idle         => false
      case MoveLeft     => false
      case MoveRight    => false
      case FallingLeft  => true
      case FallingRight => true
      case JumpingLeft  => false
      case JumpingRight => false

  def isJumping: Boolean =
    this match
      case Idle         => false
      case MoveLeft     => false
      case MoveRight    => false
      case FallingLeft  => false
      case FallingRight => false
      case JumpingLeft  => true
      case JumpingRight => true

  def inMidAir: Boolean =
    isFalling || isJumping
