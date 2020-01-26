package pirate

import indigo._

final case class Model(pirateState: PirateState, position: Point, isFalling: Boolean) {
  val navRegion: Rectangle =
    Rectangle(137, 0, 338, 272)

  val pirateIsSafe: Boolean =
    navRegion.isPointWithin(position)
}

object Model {

  val initialModel: Model =
    Model(PirateState.Idle, Point(300, 271), false)

  val walkSpeed: Int = 5
  val fallSpeed: Int = 10

  def update(model: Model, inputState: InputState, screenDimensions: Rectangle): GlobalEvent => Outcome[Model] = {
    case FrameTick =>
      if (!model.isFalling && model.pirateIsSafe) {
        inputState.keyboard.lastKeyHeldDown match {
          case None =>
            Outcome(Model(PirateState.Idle, model.position, model.isFalling))

          case Some(Keys.LEFT_ARROW) =>
            Outcome(Model(PirateState.MoveLeft, model.position - Point(walkSpeed, 0), model.isFalling))

          case Some(Keys.RIGHT_ARROW) =>
            Outcome(Model(PirateState.MoveRight, model.position + Point(walkSpeed, 0), model.isFalling))

          case Some(Keys.UP_ARROW) =>
            Outcome(Model(PirateState.Jump, model.position, model.isFalling))

          case Some(_) =>
            Outcome(Model(PirateState.Idle, model.position, model.isFalling))
        }
      } else if (model.isFalling) {
        if (model.pirateIsSafe && model.position.y + fallSpeed >= model.navRegion.bottom) {
          Outcome(Model(PirateState.Idle, Point(model.position.x, model.navRegion.bottom - 1), false))
        } else if (model.position.y > screenDimensions.height + 50) {
          Outcome(Model(PirateState.Falling, Point(screenDimensions.horizontalCenter, 20), true))
        } else {
          Outcome(Model(PirateState.Falling, model.position + Point(0, fallSpeed), true))
        }
      } else {
        if (model.position.y > screenDimensions.height + 50) {
          Outcome(Model(PirateState.Falling, Point(screenDimensions.horizontalCenter, 20), true))
        } else {
          Outcome(Model(PirateState.Falling, model.position + Point(0, fallSpeed), true))
        }
      }

    case _ =>
      Outcome(model)
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
