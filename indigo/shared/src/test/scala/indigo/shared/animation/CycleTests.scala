package indigo.shared.animation

import indigo.shared.time._
import indigo.shared.datatypes._
import indigo.shared.collections.NonEmptyList

class CycleTests extends munit.FunSuite {

  val frame1: Frame =
    Frame(Rectangle(Point(0, 0), Point(10, 10)), Millis(10))

  val frame2: Frame =
    Frame(Rectangle(0, 0, 20, 10), Millis(10))

  val frame3: Frame =
    Frame(Rectangle(0, 0, 30, 10), Millis(10))

  val cycle: Cycle =
    Cycle.create("test", NonEmptyList(frame1, frame2, frame3))

  test("adding a frame") {
    assertEquals(
      Cycle.create("test", NonEmptyList(frame1)).addFrame(frame2),
      Cycle.create("test", NonEmptyList(frame1, frame2))
    )
  }

}
