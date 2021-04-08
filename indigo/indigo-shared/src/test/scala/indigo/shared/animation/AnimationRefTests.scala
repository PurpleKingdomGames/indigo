package indigo.shared.animation

import indigo.shared.datatypes._
import indigo.shared.assets.AssetName
import indigo.shared.time.Millis
import indigo.shared.materials.StandardMaterial

class AnimationRefTests extends munit.FunSuite {

  val frame1: Frame =
    Frame(Rectangle(Point(0, 0), Point(10, 10)), Millis(10))

  val frame2: Frame =
    Frame(Rectangle(0, 0, 20, 10), Millis(10))

  val frame3: Frame =
    Frame(Rectangle(0, 0, 30, 10), Millis(10))

  val frame4: Frame =
    Frame(Rectangle(0, 0, 40, 10), Millis(10))

  val frame5: Frame =
    Frame(Rectangle(0, 0, 50, 10), Millis(10))

  val frame6: Frame =
    Frame(Rectangle(0, 0, 60, 10), Millis(10))

  val cycleLabel1 =
    CycleLabel("cycle 1")

  val cycleLabel2 =
    CycleLabel("cycle 2")

  val cycle1: CycleRef =
    CycleRef.create(cycleLabel1, List(frame1, frame2, frame3))

  val cycle2: CycleRef =
    CycleRef.create(cycleLabel2, List(frame4, frame5, frame6))

  val cycles: Map[CycleLabel, CycleRef] =
    Map(cycle1.label -> cycle1, cycle2.label -> cycle2)

  val animation: AnimationRef =
    AnimationRef(
      AnimationKey("test anim"),
      StandardMaterial.Blit(AssetName("imageAssetRef")),
      cycles.head._1,
      cycles
    )

  val bindingKey: BindingKey =
    BindingKey("test")

  test("Can record a memento") {

    val expected =
      AnimationMemento(
        bindingKey,
        cycleLabel1,
        CycleMemento(0, Millis(0))
      )

    val actual = animation.saveMemento(bindingKey)

    assertEquals(expected == actual, true)

  }

  test("Can apply a memeto") {

    val expected =
      AnimationMemento(
        bindingKey,
        cycleLabel2,
        CycleMemento(3, Millis(300))
      )

    val updated = animation.applyMemento(expected)

    val actual = updated.saveMemento(bindingKey)

    assertEquals(updated.currentCycleLabel, cycleLabel2)
    assertEquals(updated.currentCycle.playheadPosition, 3)
    assertEquals(updated.currentCycle.lastFrameAdvance, Millis(300))

    assertEquals(expected == actual, true)

  }

}
