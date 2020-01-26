package pirate

import indigo._

final case class Model(pirateState: PirateState)

object Model {

  val initialModel: Model =
    Model(PirateState.Idle)

  def update(previous: Model, inputState: InputState): GlobalEvent => Outcome[Model] = {
    case FrameTick =>
      inputState.keyboard.lastKeyHeldDown match {
        case None =>
          Outcome(Model(PirateState.Idle))

        case Some(Keys.LEFT_ARROW) =>
          Outcome(Model(PirateState.MoveLeft))

        case Some(Keys.RIGHT_ARROW) =>
          Outcome(Model(PirateState.MoveRight))

        case Some(Keys.UP_ARROW) =>
          Outcome(Model(PirateState.Jump))

        case Some(_) =>
          Outcome(Model(PirateState.Idle))
      }

    case _ =>
      Outcome(previous)
  }
}

sealed trait PirateState
object PirateState {
  case object Idle      extends PirateState
  case object MoveLeft  extends PirateState
  case object MoveRight extends PirateState
  case object Jump      extends PirateState
  case object Falling   extends PirateState
}
