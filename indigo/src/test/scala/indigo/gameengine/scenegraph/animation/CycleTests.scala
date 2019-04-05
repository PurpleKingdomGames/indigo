package indigo.gameengine.scenegraph.animation

import utest._

import indigo._
import indigo.collections.NonEmptyList
import indigo.temporal.Signal
import indigo.EqualTo._
import indigo.AsString._

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
                lastFrameAdvance = 60
              )
              .at(GameTime.is(Millis(90)).running)

          val expected: CycleMemento =
            CycleMemento(3, 90)

          actual === expected ==> true
        }

        "get the current frame" - {
          cycle.currentFrame === frame1 ==> true
        }

        "save a memento" - {
          cycle.saveMemento === CycleMemento(0, 0) ==> true
          cycle.updatePlayheadAndLastAdvance(3, 10).saveMemento === CycleMemento(3, 10) ==> true
        }

        "apply a memento" - {
          cycle.applyMemento(CycleMemento(2, 0)).currentFrame === frame3 ==> true
        }

      }

      "Running actions" - {
        import AnimationAction._

        "Play" - {
          val actual =
            cycle
              .runActions(GameTime.is(Millis(0)), List(Play))
              .runActions(GameTime.is(Millis(10)), List(Play))
              .currentFrame

          val expected =
            frame2

          actual === expected ==> true
        }

        "ChangeCycle" - {
          //no op
          cycle.runActions(GameTime.zero, List(ChangeCycle("fish"))).currentFrame === frame1 ==> true
        }

        "JumpToFirstFrame" - {
          cycle.applyMemento(CycleMemento(2, 0)).runActions(GameTime.zero, List(JumpToFirstFrame)).currentFrame === frame1 ==> true
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
