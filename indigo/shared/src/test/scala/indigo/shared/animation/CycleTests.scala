package indigo.shared.animation

import utest._

import indigo.shared.time._
import indigo.shared.datatypes._
import indigo.shared.collections.NonEmptyList
import indigo.shared.temporal.Signal
import indigo.shared.EqualTo._
import indigo.shared.AsString._

object CycleTests extends TestSuite {

  val frame1: Frame =
    Frame(Rectangle(Point(0, 0), Point(10, 10)), 10)

  val frame2: Frame =
    Frame(Rectangle(0, 0, 20, 10), 10)

  val frame3: Frame =
    Frame(Rectangle(0, 0, 30, 10), 10)

  val cycle: Cycle =
    Cycle.create("test", NonEmptyList(frame1, frame2, frame3))

  val tests: Tests =
    Tests {

      "General functions" - {

        "adding a frame" - {
          Cycle.create("test", NonEmptyList(frame1)).addFrame(frame2) === Cycle.create("test", NonEmptyList(frame1, frame2)) ==> true
        }

        "calculate next play head position" - {
          val actual: CycleMemento =
            Cycle
              .calculateNextPlayheadPosition(
                currentPosition = 2,
                frameDuration = 30,
                frameCount = 10,
                lastFrameAdvance = Millis(60)
              )
              .at(Millis(90).toSeconds)

          val expected: CycleMemento =
            CycleMemento(3, Millis(90))

          actual === expected ==> true
        }

        "get the current frame" - {
          cycle.currentFrame === frame1 ==> true
        }

        "save a memento" - {
          cycle.saveMemento === CycleMemento(0, Millis(0)) ==> true
          cycle.updatePlayheadAndLastAdvance(3, Millis(10)).saveMemento === CycleMemento(3, Millis(10)) ==> true
        }

        "apply a memento" - {
          cycle.applyMemento(CycleMemento(2, Millis(0))).currentFrame === frame3 ==> true
        }

      }

      "Running actions" - {
        import AnimationAction._

        "Play" - {
          val actual =
            cycle
              .runActions(GameTime.is(Seconds(0)), List(Play))
              .runActions(GameTime.is(Seconds(10)), List(Play))
              .currentFrame

          val expected =
            frame2

          actual === expected ==> true
        }

        "ChangeCycle" - {
          //no op
          cycle.runActions(GameTime.zero, List(ChangeCycle(CycleLabel("fish")))).currentFrame === frame1 ==> true
        }

        "JumpToFirstFrame" - {
          cycle.applyMemento(CycleMemento(2, Millis(0))).runActions(GameTime.zero, List(JumpToFirstFrame)).currentFrame === frame1 ==> true
        }

        "JumpToLastFrame" - {
          cycle.runActions(GameTime.zero, List(JumpToLastFrame)).currentFrame === frame3 ==> true
        }

        "JumpToFrame" - {
          cycle.runActions(GameTime.zero, List(JumpToFrame(1))).currentFrame === frame2 ==> true
        }

        "JumpToFrame (capped at max)" - {
          cycle.runActions(GameTime.zero, List(JumpToFrame(10))).currentFrame === frame3 ==> true
        }

      }

    }

}
