package indigo.gameengine.scenegraph.animation

import utest._

import indigo._
import indigo.collections.NonEmptyList

import indigo.shared.EqualTo._

object CycleTests extends TestSuite {

  val frame1: Frame =
    Frame(Rectangle(Point(0, 0), Point(10, 10)), 10)

  // val frame2: Frame =
  //   Frame(Rectangle(0, 0, 20, 10), 10)

  // val frame3: Frame =
  //   Frame(Rectangle(0, 0, 30, 10), 10)

  // val cycle: Cycle =
  //   Cycle.create("test", NonEmptyList(frame1))

  val tests: Tests =
    Tests {

      "Cycle tests" - {

        "adding a frame" - {
          //cycle.addFrame(frame2) === Cycle.create("test", NonEmptyList(frame1, frame2)) ==> true
          1 ==> 2
        }

      }

    }

}
