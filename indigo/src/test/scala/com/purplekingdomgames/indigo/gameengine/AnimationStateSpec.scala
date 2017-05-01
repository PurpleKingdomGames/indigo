package com.purplekingdomgames.indigo.gameengine

import com.purplekingdomgames.indigo.gameengine.scenegraph.SceneGraphSamples
import org.scalatest.{FunSpec, Matchers}

class AnimationStateSpec extends FunSpec with Matchers {

  describe("Using animation states") {

    it("Should be able to extract animation states") {

      val states = AnimationState.extractAnimationStates(SceneGraphSamples.api)

      states.states.length shouldEqual 1

    }

  }

}
