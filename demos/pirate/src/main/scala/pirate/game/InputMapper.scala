package pirate.game

import indigo._

object InputMapper {

  def apply(inputState: InputState): PirateState =
    checkGamepadInput(inputState) |*| checkKeyboardInput(inputState)

  def checkGamepadInput(inputState: InputState): PirateState =
    if (inputState.gamepad.analog.left.x < -0.5 || inputState.gamepad.dpad.left) {
      PirateState.MoveLeft
    } else if (inputState.gamepad.analog.left.x > 0.5 || inputState.gamepad.dpad.right) {
      PirateState.MoveRight
    } else {
      PirateState.Idle
    }

  def checkKeyboardInput(inputState: InputState): PirateState =
    inputState.keyboard.lastKeyHeldDown match {
      case None =>
        PirateState.Idle

      case Some(Keys.LEFT_ARROW) =>
        PirateState.MoveLeft

      case Some(Keys.RIGHT_ARROW) =>
        PirateState.MoveRight

      case Some(_) =>
        PirateState.Idle
    }

}
