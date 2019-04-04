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
          val signal: Signal[CycleMemento] =
            Cycle.calculateNextPlayheadPosition(0, 30, 10, 0)

          (0 to 10).toList
            .map(i => (GameTime.is(Millis(i * 30)), CycleMemento(i, 0)))
            .map {
              case (gameTime, res) =>
                signal.at(gameTime.running) === res ==> true
            }
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
          cycle.runActions(GameTime.zero, List(Play)).currentFrame === frame2 ==> true
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

      }

    }

}
