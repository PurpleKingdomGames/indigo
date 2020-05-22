package indigogame.scenemanager

import indigo.shared.FrameContext
import indigo.shared.time.GameTime
import indigo.shared.dice.Dice
import indigo.shared.events.InputState
import indigo.shared.BoundaryLocator
import indigo.shared.AnimationsRegister
import indigo.shared.FontRegister
import indigo.shared.time.Seconds

object FakeFrameContext {

  def context(sides: Int): FrameContext = 
    new FrameContext(
      GameTime.zero,
      Dice.loaded(sides),
      InputState.default,
      new BoundaryLocator(new AnimationsRegister, new FontRegister)
    )

  def context(sides: Int, time: Seconds): FrameContext = 
    new FrameContext(
      GameTime.is(time),
      Dice.loaded(sides),
      InputState.default,
      new BoundaryLocator(new AnimationsRegister, new FontRegister)
    )

  def context(sides: Int, time: Seconds, delta: Seconds): FrameContext = 
    new FrameContext(
      GameTime.withDelta(time, delta),
      Dice.loaded(sides),
      InputState.default,
      new BoundaryLocator(new AnimationsRegister, new FontRegister)
    )

}
