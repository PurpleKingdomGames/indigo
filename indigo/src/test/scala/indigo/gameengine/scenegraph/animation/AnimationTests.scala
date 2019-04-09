package indigo.gameengine.scenegraph.animation

import utest._

import indigo._
import indigo.collections.NonEmptyList
import indigo.EqualTo._
import indigo.AsString._

object AnimationTests extends TestSuite {

  val frame1: Frame =
    Frame(Rectangle(Point(0, 0), Point(10, 10)), 10)

  val frame2: Frame =
    Frame(Rectangle(0, 0, 20, 10), 10)

  val frame3: Frame =
    Frame(Rectangle(0, 0, 30, 10), 10)

  val frame4: Frame =
    Frame(Rectangle(0, 0, 40, 10), 10)

  val frame5: Frame =
    Frame(Rectangle(0, 0, 50, 10), 10)

  val frame6: Frame =
    Frame(Rectangle(0, 0, 60, 10), 10)

  val cycle1: Cycle =
    Cycle.create("cycle 1", NonEmptyList(frame1, frame2, frame3))

  val cycle2: Cycle =
    Cycle.create("cycle 2", NonEmptyList(frame4, frame5, frame6))

  val cycles: NonEmptyList[Cycle] =
    NonEmptyList(cycle1, cycle2)

  val animation: Animation =
    Animation(
      AnimationKey("test anim"),
      "imageAssetRef",
      Point.zero,
      cycles.head.label,
      cycles,
      Nil
    )

  val tests: Tests =
    Tests {

      import AnimationAction._

      "Running actions" - {

        "Can play the current frame of the current cycle" - {
          val actual: Animation =
            animation
              .addAction(Play)
              .runActions(GameTime.is(Millis(0)))

          val expected: Animation =
            animation

          println(actual.show)
          println(expected.show)

          actual === expected ==> true
        }

        "repeatedly changing to the same animation and playing it advances the animations" - {

          // val actual: Animation =
          //   animation
          //   .addAction(ChangeCycle())

          1 ==> 2

        }

      }

    }

}
