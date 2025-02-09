package indigo.gameengine

class AnimationStateTests extends munit.FunSuite {

  // TODO: Needs to be update to test the AnimationsRegister
//  test("Using animation states") {
//
//    test("Should be able to extract animation states") {
//
//      val states = AnimationState.extractAnimationStates(SceneGraphSamples.api.flatten)
//
// assertEquals(//      states.states.length, 1)
//
//    }
//
//    test("should be able to update, persist and restore animations") {
//
//      val animations = Animations(
//        spriteSheetSize = Point(256, 256),
//        currentCycleLabel = CycleLabel("test-cycle"),
//        cycle = Cycle(
//          label = "test-cycle",
//          frame = Frame(0, 0, 64, 64, 1),
//          frames = List(
//            Frame(1, 0, 64, 64, 1),
//            Frame(2, 0, 64, 64, 1),
//            Frame(3, 0, 64, 64, 1),
//            Frame(4, 0, 64, 64, 1),
//            Frame(5, 0, 64, 64, 1),
//            Frame(6, 0, 64, 64, 1)
//          )
//        ),
//        cycles = Map(
//          CycleLabel("test-cycle2") -> Cycle(
//            label = "test-cycle2",
//            frame = Frame(0, 0, 64, 64, 1),
//            frames = List(
//              Frame(1, 0, 64, 64, 1),
//              Frame(2, 0, 64, 64, 1),
//              Frame(3, 0, 64, 64, 1),
//              Frame(4, 0, 64, 64, 1),
//              Frame(5, 0, 64, 64, 1),
//              Frame(6, 0, 64, 64, 1)
//            )
//          )
//        ),
//        actions = Nil
//      )
//
//      val sprite = Sprite(
//        bindingKey = BindingKey("test"),
//        x = 0,
//        y = 0,
//        width = 10,
//        height = 10,
//        imageAssetRef = "...",
//        animations = animations
//      )
//
//      test("Simple operations") {
// assertEquals(//        sprite.animations.currentFrame.bounds.x, 0)
//
// assertEquals(//        sprite.jumpToLastFrame().animations.runActions(GameTime.is(10, 10)).currentFrame.bounds.x, 6)
//
// assertEquals(//        sprite.play().animations.runActions(GameTime.is(10, 10)).currentFrame.bounds.x, 1)
//      }
//
//      test("Check memento save") {
//        val s = sprite
//          .play()
//
//        val sa = s.animations
//          .runActions(GameTime.is(10, 10))
//
// assertEquals(//        sa.currentCycleName, "test-cycle")
// assertEquals(//        sa.currentFrame.bounds.x, 1)
//
//        val memento = sa.saveMemento(s.bindingKey)
//
// assertEquals(//        memento, AnimationMemento(BindingKey("test"), CycleLabel("test-cycle"), CycleMemento(1, 10)))
//      }
//
//      test("Check memento save and apply with cycle change") {
//        val s2 = sprite
//          .changeCycle("test-cycle2")
//          .play()
//
//        val sa2 = s2.animations
//          .runActions(GameTime.is(10, 10))
//
// assertEquals(//        sa2.currentCycleName, "test-cycle2")
// assertEquals(//        sa2.currentFrame.bounds.x, 1)
//
//        val memento2 = sa2.saveMemento(s2.bindingKey)
//
// assertEquals(//        memento2, AnimationMemento(BindingKey("test"), CycleLabel("test-cycle2"), CycleMemento(1, 10)))
//
//        val s3 = sprite.applyAnimationMemento(AnimationStates(List(memento2)))
//
// assertEquals(//        s3.animations.currentCycleName, "test-cycle2")
// assertEquals(//        s3.animations.currentFrame.bounds.x, 1)
//
//      }
//
//    }
//
//  }
}
