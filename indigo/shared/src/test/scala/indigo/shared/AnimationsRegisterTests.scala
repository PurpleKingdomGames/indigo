package indigo.shared

import utest._
import indigo.shared.animation.Frame
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Point
import indigo.shared.collections.NonEmptyList
import indigo.shared.animation.Cycle
import indigo.shared.animation.Animation
import indigo.shared.animation.AnimationKey
import indigo.shared.datatypes.Material
import indigo.shared.assets.AssetName
import indigo.shared.time.Seconds
import indigo.shared.datatypes.BindingKey
import indigo.shared.time.GameTime
import indigo.shared.animation.CycleLabel
import indigo.shared.animation.AnimationAction.ChangeCycle
import indigo.shared.time.Millis
import indigo.shared.animation.AnimationAction.Play
import indigo.shared.EqualTo._
import indigo.shared.AsString._

object AnimationsRegisterTests extends TestSuite {

  val tests: Tests =
    Tests {

      "can look up an animation by key" - {
        val register = new AnimationsRegister()
        register.register(AnimationSample.animation)

        val actual = register.findByAnimationKey(AnimationSample.key)

        actual.isDefined ==> true
        actual.get.animationKey ==> AnimationSample.key

        register.findByAnimationKey(AnimationKey("nope")).isEmpty ==> true
      }

      "can look up a memento by key" - {

        val bindingKey = BindingKey("sprite 1")

        val register = new AnimationsRegister()
        register.register(AnimationSample.animation)

        register.findMementoByBindingKey(bindingKey).isEmpty ==> true

        register.fetchAnimationForSprite(
          GameTime.is(Seconds(0)),
          bindingKey,
          AnimationSample.key,
          Nil
        )

        val actual = register.findMementoByBindingKey(bindingKey)

        actual.isDefined ==> true
        actual.get.bindingKey ==> bindingKey
        actual.get.currentCycleLabel ==> AnimationSample.cycleLabel1
        actual.get.currentCycleMemento.playheadPosition ==> 0
        actual.get.currentCycleMemento.lastFrameAdvance ==> Millis.zero
      }

      "can apply and store animations mementos" - {

        val bindingKey = BindingKey("sprite 1")

        // Fetch default animation
        val register = new AnimationsRegister()
        register.register(AnimationSample.animation)

        // ------------
        // Round 1 - control, do nothing.
        val updatedAnim1 = register.fetchAnimationForSprite(
          GameTime.is(Seconds(0)),
          bindingKey,
          AnimationSample.key,
          Nil
        )

        updatedAnim1.isDefined ==> true
        updatedAnim1.get.animationKey ==> AnimationSample.key
        updatedAnim1.get.currentCycle.label ==> AnimationSample.cycleLabel1
        updatedAnim1.get.currentCycle.playheadPosition ==> 0
        updatedAnim1.get.currentCycle.lastFrameAdvance ==> Millis.zero

        val memento1 = register.findMementoByBindingKey(bindingKey)

        memento1.isDefined ==> true
        memento1.get.bindingKey ==> bindingKey
        memento1.get.currentCycleLabel ==> AnimationSample.cycleLabel1
        memento1.get.currentCycleMemento.playheadPosition ==> 0
        memento1.get.currentCycleMemento.lastFrameAdvance ==> Millis.zero
        // ------------

        // ------------
        // Round 2
        val updatedAnim2 = register.fetchAnimationForSprite(
          GameTime.is(Millis(100).toSeconds),
          bindingKey,
          AnimationSample.key,
          List(ChangeCycle(AnimationSample.cycleLabel2), Play)
        )

        updatedAnim2.isDefined ==> true
        updatedAnim2.get.animationKey ==> AnimationSample.key
        updatedAnim2.get.currentCycle.label ==> AnimationSample.cycleLabel2
        updatedAnim2.get.currentCycle.playheadPosition ==> 1
        updatedAnim2.get.currentCycle.lastFrameAdvance ==> Millis(100)

        val memento2 = register.findMementoByBindingKey(bindingKey)

        memento2.isDefined ==> true
        memento2.get.bindingKey ==> bindingKey
        memento2.get.currentCycleLabel ==> AnimationSample.cycleLabel2
        memento2.get.currentCycleMemento.playheadPosition ==> 1
        memento2.get.currentCycleMemento.lastFrameAdvance ==> Millis(100)
        // ------------

        // ------------
        // Round 3
        val updatedAnim3 = register.fetchAnimationForSprite(
          GameTime.is(Millis(200).toSeconds),
          bindingKey,
          AnimationSample.key,
          List(Play)
        )

        updatedAnim3.isDefined ==> true
        updatedAnim3.get.animationKey ==> AnimationSample.key
        updatedAnim3.get.currentCycle.label ==> AnimationSample.cycleLabel2
        updatedAnim3.get.currentCycle.playheadPosition ==> 2
        updatedAnim3.get.currentCycle.lastFrameAdvance ==> Millis(200)

        val memento3 = register.findMementoByBindingKey(bindingKey)

        memento3.isDefined ==> true
        memento3.get.bindingKey ==> bindingKey
        memento3.get.currentCycleLabel ==> AnimationSample.cycleLabel2
        memento3.get.currentCycleMemento.playheadPosition ==> 2
        memento3.get.currentCycleMemento.lastFrameAdvance ==> Millis(200)
        // ------------

        // ------------
        // Round 4
        val updatedAnim4 = register.fetchAnimationForSprite(
          GameTime.is(Millis(400).toSeconds),
          bindingKey,
          AnimationSample.key,
          List(Play)
        )

        updatedAnim4.isDefined ==> true
        updatedAnim4.get.animationKey ==> AnimationSample.key
        updatedAnim4.get.currentCycle.label ==> AnimationSample.cycleLabel2
        updatedAnim4.get.currentCycle.playheadPosition ==> 1
        updatedAnim4.get.currentCycle.lastFrameAdvance ==> Millis(400)

        val memento4 = register.findMementoByBindingKey(bindingKey)

        memento4.isDefined ==> true
        memento4.get.bindingKey ==> bindingKey
        memento4.get.currentCycleLabel ==> AnimationSample.cycleLabel2
        memento4.get.currentCycleMemento.playheadPosition ==> 1
        memento4.get.currentCycleMemento.lastFrameAdvance ==> Millis(400)
        // ------------

        memento1 === memento2 ==> false
        memento2 === memento3 ==> false
        memento3 === memento4 ==> false

      }

    }

}

object AnimationSample {

  val frame1: Frame =
    Frame(Rectangle(Point(0, 0), Point(10, 10)), Millis(100))

  val frame2: Frame =
    Frame(Rectangle(0, 0, 20, 10), Millis(100))

  val frame3: Frame =
    Frame(Rectangle(0, 0, 30, 10), Millis(100))

  val frame4: Frame =
    Frame(Rectangle(0, 0, 40, 10), Millis(100))

  val frame5: Frame =
    Frame(Rectangle(0, 0, 50, 10), Millis(100))

  val frame6: Frame =
    Frame(Rectangle(0, 0, 60, 10), Millis(100))

  val cycleLabel1: CycleLabel =
    CycleLabel("cycle 1")

  val cycleLabel2: CycleLabel =
    CycleLabel("cycle 2")

  val cycle1: Cycle =
    Cycle.create(cycleLabel1.value, NonEmptyList(frame1, frame2, frame3))

  val cycle2: Cycle =
    Cycle.create(cycleLabel2.value, NonEmptyList(frame4, frame5, frame6))

  val cycles: NonEmptyList[Cycle] =
    NonEmptyList(cycle1, cycle2)

  val key: AnimationKey =
    AnimationKey("test anim")

  val animation: Animation =
    Animation(
      key,
      Material.Textured(AssetName("imageAssetRef")),
      cycles.head.label,
      cycles
    )

}
