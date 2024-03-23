package indigo.shared.subsystems

import indigo.platform.assets.DynamicText
import indigo.shared.AnimationsRegister
import indigo.shared.BoundaryLocator
import indigo.shared.FontRegister
import indigo.shared.dice.Dice
import indigo.shared.events.InputState
import indigo.shared.subsystems.SubSystemFrameContext
import indigo.shared.time.GameTime
import indigo.shared.time.Seconds

object FakeSubSystemFrameContext:

  def context(sides: Int): SubSystemFrameContext[Unit] =
    SubSystemFrameContext(
      GameTime.zero,
      Dice.loaded(sides),
      InputState.default,
      new BoundaryLocator(new AnimationsRegister, new FontRegister, new DynamicText),
      ()
    )

  def context(sides: Int, time: Seconds): SubSystemFrameContext[Unit] =
    SubSystemFrameContext(
      GameTime.is(time),
      Dice.loaded(sides),
      InputState.default,
      new BoundaryLocator(new AnimationsRegister, new FontRegister, new DynamicText),
      ()
    )

  def context(sides: Int, time: Seconds, delta: Seconds): SubSystemFrameContext[Unit] =
    SubSystemFrameContext(
      GameTime.withDelta(time, delta),
      Dice.loaded(sides),
      InputState.default,
      new BoundaryLocator(new AnimationsRegister, new FontRegister, new DynamicText),
      ()
    )
