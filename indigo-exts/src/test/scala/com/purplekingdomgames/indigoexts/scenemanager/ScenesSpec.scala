package com.purplekingdomgames.indigoexts.scenemanager
import com.purplekingdomgames.indigoexts.collections.NonEmptyList
import org.scalatest.{FunSpec, Matchers}

class ScenesSpec extends FunSpec with Matchers {

  import TestScenes._

  val scenes: ScenesList[TestGameModel, TestViewModel, TestSceneA, _] = sceneA :: sceneB :: ScenesNil[TestGameModel, TestViewModel]()

  describe("Scenes") {

    it("should build a heterogeneous Scenes instance") {
      scenes.head shouldEqual sceneA
      scenes.nextScene.head shouldEqual sceneB
    }

    it("should allow you to ask for the next scene") {
      scenes.head shouldEqual sceneA
      scenes.nextScene.head shouldEqual sceneB
      scenes.nextScene.nextScene.head shouldEqual sceneB // Reached the end, there is no next.
    }

    it("should allow you to ask for a specific scene") {
      scenes.findScene(sceneNameA) shouldEqual Some(sceneA)
      scenes.findScene(sceneNameB) shouldEqual Some(sceneB)
      scenes.findScene(SceneName("test scene c")) shouldEqual None
    }

    it("should allow you to list all scene names") {
      NonEmptyList.equality(scenes.listSceneNames, NonEmptyList(sceneNameA, List(sceneNameB))) shouldEqual true
    }

  }

}
