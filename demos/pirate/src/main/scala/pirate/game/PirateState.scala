package pirate.game

sealed trait PirateState {

  def |*|(other: PirateState): PirateState =
    this.combine(other)

  def combine(other: PirateState): PirateState =
    (this, other) match {
      case (PirateState.Idle, ps)        => ps
      case (ps, PirateState.Idle)        => ps
      case (ps, _)                       => ps
    }

}
object PirateState {

  case object Idle      extends PirateState
  case object MoveLeft  extends PirateState
  case object MoveRight extends PirateState
  case object Falling   extends PirateState
}
