package indigo.shared

import indigo.shared.time.GameTime
import indigo.shared.events.InputState
import indigo.shared.dice.Dice

final class FrameContext(
  val gameTime: GameTime,
  val dice: Dice,
  val inputState: InputState,
  val boundaryLocator: BoundaryLocator
)
