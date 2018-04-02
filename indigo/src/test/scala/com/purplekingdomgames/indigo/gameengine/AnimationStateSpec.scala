package com.purplekingdomgames.indigo.gameengine

import com.purplekingdomgames.indigo.gameengine.assets.{AnimationState, AnimationStates}
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.{BindingKey, Point}
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import org.scalatest.{FunSpec, Matchers}

class AnimationStateSpec extends FunSpec with Matchers {

  describe("Using animation states") {

    it("Should be able to extract animation states") {

      val states = AnimationState.extractAnimationStates(SceneGraphSamples.api.flatten)

      states.states.length shouldEqual 1

    }

    it("should be able to update, persist and restore animations") {

      val animations = Animations(
        spriteSheetSize = Point(256, 256),
        currentCycleLabel = CycleLabel("test-cycle"),
        cycle = Cycle(
          label = "test-cycle",
          frame = Frame(0, 0, 64, 64, 1),
          frames = List(
            Frame(1, 0, 64, 64, 1),
            Frame(2, 0, 64, 64, 1),
            Frame(3, 0, 64, 64, 1),
            Frame(4, 0, 64, 64, 1),
            Frame(5, 0, 64, 64, 1),
            Frame(6, 0, 64, 64, 1)
          )
        ),
        cycles = Map(
          CycleLabel("test-cycle2") -> Cycle(
            label = "test-cycle2",
            frame = Frame(0, 0, 64, 64, 1),
            frames = List(
              Frame(1, 0, 64, 64, 1),
              Frame(2, 0, 64, 64, 1),
              Frame(3, 0, 64, 64, 1),
              Frame(4, 0, 64, 64, 1),
              Frame(5, 0, 64, 64, 1),
              Frame(6, 0, 64, 64, 1)
            )
          )
        ),
        actions = Nil
      )

      val sprite = Sprite(
        bindingKey = BindingKey("test"),
        x = 0,
        y = 0,
        width = 10,
        height = 10,
        depth = 1,
        imageAssetRef = "...",
        animations = animations
      )

      withClue("Simple operations") {
        sprite.animations.currentFrame.bounds.x shouldEqual 0

        sprite.jumpToLastFrame().animations.runActions(GameTime.is(10, 10)).currentFrame.bounds.x shouldEqual 6

        sprite.play().animations.runActions(GameTime.is(10, 10)).currentFrame.bounds.x shouldEqual 1
      }

      withClue("Check memento save") {
        val s = sprite
          .play()

        val sa = s.animations
          .runActions(GameTime.is(10, 10))

        sa.currentCycleName shouldEqual "test-cycle"
        sa.currentFrame.bounds.x shouldBe 1

        val memento = sa.saveMemento(s.bindingKey)

        memento shouldEqual AnimationMemento(BindingKey("test"), CycleLabel("test-cycle"), CycleMemento(1, 10))
      }

      withClue("Check memento save and apply with cycle change") {
        val s2 = sprite
          .changeCycle("test-cycle2")
          .play()

        val sa2 = s2.animations
          .runActions(GameTime.is(10, 10))

        sa2.currentCycleName shouldEqual "test-cycle2"
        sa2.currentFrame.bounds.x shouldEqual 1

        val memento2 = sa2.saveMemento(s2.bindingKey)

        memento2 shouldEqual AnimationMemento(BindingKey("test"), CycleLabel("test-cycle2"), CycleMemento(1, 10))

        val s3 = sprite.applyAnimationMemento(AnimationStates(List(memento2)))

        s3.animations.currentCycleName shouldEqual "test-cycle2"
        s3.animations.currentFrame.bounds.x shouldEqual 1

      }

    }

  }

}
