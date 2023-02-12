package pirate.scenes.level.model

/*
An ADT of all the states the pirate can find himself in.
 */
sealed trait PirateState derives CanEqual {

  val isFalling: Boolean =
    this match {
      case PirateState.FallingLeft  => true
      case PirateState.FallingRight => true
      case _                        => false
    }

  val isJumping: Boolean =
    this match {
      case PirateState.JumpingLeft  => true
      case PirateState.JumpingRight => true
      case _                        => false
    }

  val inMidAir: Boolean =
    isFalling || isJumping

}
object PirateState {
  case object Idle         extends PirateState
  case object MoveLeft     extends PirateState
  case object MoveRight    extends PirateState
  case object FallingLeft  extends PirateState
  case object FallingRight extends PirateState
  case object JumpingLeft  extends PirateState
  case object JumpingRight extends PirateState
}
