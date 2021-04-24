package indigo.shared.subsystems

import indigo.shared.subsystems.SubSystemFrameContext
import indigo.shared.time.GameTime
import indigo.shared.dice.Dice
import indigo.shared.events.InputState
import indigo.shared.BoundaryLocator
import indigo.shared.AnimationsRegister
import indigo.shared.FontRegister
import indigo.shared.time.Seconds

object FakeSubSystemFrameContext {

  def context(sides: Int): SubSystemFrameContext = 
    new SubSystemFrameContext(
      GameTime.zero,
      Dice.loaded(sides),
      InputState.default,
      new BoundaryLocator(new AnimationsRegister, new FontRegister)
    )

  def context(sides: Int, time: Seconds): SubSystemFrameContext = 
    new SubSystemFrameContext(
      GameTime.is(time),
      Dice.loaded(sides),
      InputState.default,
      new BoundaryLocator(new AnimationsRegister, new FontRegister)
    )

  def context(sides: Int, time: Seconds, delta: Seconds): SubSystemFrameContext = 
    new SubSystemFrameContext(
      GameTime.withDelta(time, delta),
      Dice.loaded(sides),
      InputState.default,
      new BoundaryLocator(new AnimationsRegister, new FontRegister)
    )

}
