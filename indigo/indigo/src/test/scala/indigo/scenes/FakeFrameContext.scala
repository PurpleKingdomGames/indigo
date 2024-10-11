package indigo.scenes

import indigo.platform.assets.DynamicText
import indigo.platform.renderer.Renderer
import indigo.shared.AnimationsRegister
import indigo.shared.BoundaryLocator
import indigo.shared.FontRegister
import indigo.shared.FrameContext
import indigo.shared.dice.Dice
import indigo.shared.events.InputState
import indigo.shared.time.GameTime
import indigo.shared.time.Seconds

object FakeFrameContext {

  def context(sides: Int): FrameContext[Unit] =
    new FrameContext[Unit](
      GameTime.zero,
      Dice.loaded(sides),
      InputState.default,
      new BoundaryLocator(new AnimationsRegister, new FontRegister, new DynamicText),
      (),
      Renderer.blackHole
    )

  def context(sides: Int, time: Seconds): FrameContext[Unit] =
    new FrameContext[Unit](
      GameTime.is(time),
      Dice.loaded(sides),
      InputState.default,
      new BoundaryLocator(new AnimationsRegister, new FontRegister, new DynamicText),
      (),
      Renderer.blackHole
    )

  def context(sides: Int, time: Seconds, delta: Seconds): FrameContext[Unit] =
    new FrameContext[Unit](
      GameTime.withDelta(time, delta),
      Dice.loaded(sides),
      InputState.default,
      new BoundaryLocator(new AnimationsRegister, new FontRegister, new DynamicText),
      (),
      Renderer.blackHole
    )

}
