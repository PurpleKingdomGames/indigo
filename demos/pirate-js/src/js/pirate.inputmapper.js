'use strict';
class InputMapper {
    apply = function(inputState) {
      return checkGamepadInput(inputState).merge(checkKeyboardInput(inputState));
    }

    checkGamepadInput = function(inputState) {
        if (inputState.gamepad.analog.left.x < -0.5 || inputState.gamepad.dpad.left)
            return PirateState.MoveLeft;
        else if (inputState.gamepad.analog.left.x > 0.5 || inputState.gamepad.dpad.right)
            return PirateState.MoveRight;
        else
            return PirateState.Idle;
    }

    checkKeyboardInput = function(inputState) {
        switch (inputState.keyboard.lastKeyHeldDown) {
            case null:
                return PirateState.Idle

            case Some(Keys.LEFT_ARROW):
                return PirateState.MoveLeft

            case Some(Keys.RIGHT_ARROW):
                return PirateState.MoveRight;

            default:
                return PirateState.Idle
        }
    }
}
