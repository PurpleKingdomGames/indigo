package pirate

import indigo._

final case class Model(pirateState: PirateState, position: Point, isFalling: Boolean, lastPlayed: Millis, lastRespawn: Millis) {
  val navRegion: Rectangle =
    Rectangle(137, 0, 338, 272)

  val pirateIsSafe: Boolean =
    navRegion.isPointWithin(position)

  val beat: Millis =
    Millis(250)
}

object Model {

  val initialModel: Model =
    Model(PirateState.Idle, Point(300, 271), false, Millis.zero, Millis.zero)

  val walkSpeed: Int = 5
  val fallSpeed: Int = 10

  def update(gameTime: GameTime, model: Model, inputState: InputState, screenDimensions: Rectangle): GlobalEvent => Outcome[Model] = {
    case FrameTick =>
      if (!model.isFalling && model.pirateIsSafe) {

        val gamepadInput: Option[Outcome[Model]] =
          if (inputState.gamepad.connected) {
            if (inputState.gamepad.analog.left.x < -0.5 || inputState.gamepad.dpad.left) {
              val x = if (gameTime.running > model.lastPlayed + model.beat) {
                (List(PlaySound(Assets.Sounds.walkSound, Volume(0.5d))), gameTime.running)
              } else (Nil, model.lastPlayed)

              Some(
                Outcome(Model(PirateState.MoveLeft, model.position - Point(walkSpeed, 0), model.isFalling, x._2, model.lastRespawn))
                  .addGlobalEvents(x._1)
              )
            } else if (inputState.gamepad.analog.left.x > 0.5 || inputState.gamepad.dpad.right) {
              val x = if (gameTime.running > model.lastPlayed + model.beat) {
                (List(PlaySound(Assets.Sounds.walkSound, Volume(0.5d))), gameTime.running)
              } else (Nil, model.lastPlayed)

              Some(
                Outcome(Model(PirateState.MoveRight, model.position + Point(walkSpeed, 0), model.isFalling, x._2, model.lastRespawn))
                  .addGlobalEvents(x._1)
              )
            } else {
              None
            }
          } else {
            None
          }

        val keyboardInput: Option[Outcome[Model]] =
          inputState.keyboard.lastKeyHeldDown match {
            case None =>
              None

            case Some(Keys.LEFT_ARROW) =>
              val x = if (gameTime.running > model.lastPlayed + model.beat) {
                (List(PlaySound(Assets.Sounds.walkSound, Volume(0.5d))), gameTime.running)
              } else (Nil, model.lastPlayed)

              Some(
                Outcome(Model(PirateState.MoveLeft, model.position - Point(walkSpeed, 0), model.isFalling, x._2, model.lastRespawn))
                  .addGlobalEvents(x._1)
              )

            case Some(Keys.RIGHT_ARROW) =>
              val x = if (gameTime.running > model.lastPlayed + model.beat) {
                (List(PlaySound(Assets.Sounds.walkSound, Volume(0.5d))), gameTime.running)
              } else (Nil, model.lastPlayed)

              Some(
                Outcome(Model(PirateState.MoveRight, model.position + Point(walkSpeed, 0), model.isFalling, x._2, model.lastRespawn))
                  .addGlobalEvents(x._1)
              )

            case Some(_) =>
              None
          }

        gamepadInput
          .orElse(keyboardInput)
          .getOrElse(Outcome(Model(PirateState.Idle, model.position, model.isFalling, model.lastPlayed, model.lastRespawn)))

      } else if (model.isFalling) {
        if (model.pirateIsSafe && model.position.y + fallSpeed >= model.navRegion.bottom) {
          Outcome(Model(PirateState.Idle, Point(model.position.x, model.navRegion.bottom - 1), false, model.lastPlayed, model.lastRespawn))
        } else if (model.position.y > screenDimensions.height + 50) {
          Outcome(Model(PirateState.Falling, Point(screenDimensions.horizontalCenter, 20), true, model.lastPlayed, gameTime.running))
            .addGlobalEvents(PlaySound(Assets.Sounds.respawnSound, Volume.Max))
        } else {
          Outcome(Model(PirateState.Falling, model.position + Point(0, fallSpeed), true, model.lastPlayed, model.lastRespawn))
        }
      } else {
        if (model.position.y > screenDimensions.height + 50) {
          Outcome(Model(PirateState.Falling, Point(screenDimensions.horizontalCenter, 20), true, model.lastPlayed, gameTime.running))
            .addGlobalEvents(PlaySound(Assets.Sounds.respawnSound, Volume.Max))
        } else {
          Outcome(Model(PirateState.Falling, model.position + Point(0, fallSpeed), true, model.lastPlayed, model.lastRespawn))
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
  case object Falling   extends PirateState
}
