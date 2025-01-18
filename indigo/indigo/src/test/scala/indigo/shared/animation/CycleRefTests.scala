package indigo.shared.animation

import indigo.shared.collections.Batch
import indigo.shared.datatypes.*
import indigo.shared.time.*

class CycleRefTests extends munit.FunSuite {

  val frame1: Frame =
    Frame(Rectangle(Point(0, 0), Size(10, 10)), Millis(10))

  val frame2: Frame =
    Frame(Rectangle(0, 0, 20, 10), Millis(10))

  val frame3: Frame =
    Frame(Rectangle(0, 0, 30, 10), Millis(10))

  val cycle: CycleRef =
    CycleRef.create(CycleLabel("test"), Batch(frame1, frame2, frame3))

  test("General functions.calculate next play head position") {
    val actual: CycleMemento =
      CycleRef
        .calculateNextPlayheadPosition(
          currentPosition = 2,
          frameDuration = Millis(30),
          frameCount = 10,
          lastFrameAdvance = Millis(60)
        )
        .at(Millis(90).toSeconds)

    val expected: CycleMemento =
      CycleMemento(3, Millis(90))

    assertEquals(actual == expected, true)
  }

  test("General functions.calculate next play head position, skip several") {
    val actual: CycleMemento =
      CycleRef
        .calculateNextPlayheadPosition(
          currentPosition = 0,
          frameDuration = Millis(100),
          frameCount = 10,
          lastFrameAdvance = Millis(0)
        )
        .at(Millis(300).toSeconds)

    val expected: CycleMemento =
      CycleMemento(3, Millis(300))

    assertEquals(actual, expected)
  }

  test("General functions.get the current frame") {
    assertEquals(cycle.currentFrame == frame1, true)
  }

  test("General functions.save a memento") {
    assertEquals(cycle.saveMemento == CycleMemento(0, Millis(0)), true)
    assertEquals(cycle.updatePlayheadAndLastAdvance(3, Millis(10)).saveMemento == CycleMemento(3, Millis(10)), true)
  }

  test("General functions.apply a memento") {
    assertEquals(cycle.applyMemento(CycleMemento(2, Millis(0))).currentFrame == frame3, true)
  }

  import AnimationAction._

  test("Running actions.Play") {
    val actual =
      cycle
        .runActions(GameTime.is(Seconds(0)), Batch(Play))
        .runActions(GameTime.is(Seconds(10)), Batch(Play))
        .currentFrame

    val expected =
      frame2

    assertEquals(actual == expected, true)
  }

  test("Running actions.ChangeCycle") {
    // no op
    assertEquals(cycle.runActions(GameTime.zero, Batch(ChangeCycle(CycleLabel("fish")))).currentFrame == frame1, true)
  }

  test("Running actions.JumpToFirstFrame") {
    assertEquals(
      cycle
        .applyMemento(CycleMemento(2, Millis(0)))
        .runActions(GameTime.zero, Batch(JumpToFirstFrame))
        .currentFrame == frame1,
      true
    )
  }

  test("Running actions.JumpToLastFrame") {
    assertEquals(cycle.runActions(GameTime.zero, Batch(JumpToLastFrame)).currentFrame == frame3, true)
  }

  test("Running actions.JumpToFrame") {
    assertEquals(cycle.runActions(GameTime.zero, Batch(JumpToFrame(1))).currentFrame == frame2, true)
  }

  test("Running actions.JumpToFrame (capped at max)") {
    assertEquals(cycle.runActions(GameTime.zero, Batch(JumpToFrame(10))).currentFrame == frame3, true)
  }

}
