package indigoexts.scenemanager

import indigo.collections.NonEmptyList
import org.scalatest.{FunSpec, Matchers}

class SceneFinderSpec extends FunSpec with Matchers {

  import TestScenes._

  val scenes: ScenesList[TestGameModel, TestViewModel] = sceneA :: sceneB :: ScenesNil[TestGameModel, TestViewModel]()

  val sceneFinder: SceneFinder =
    SceneFinder(
      Nil,
      ScenePosition(0, sceneA.name),
      List(ScenePosition(1, sceneB.name))
    )

  describe("managing the scenes list") {

    it("should be able to construct a SceneFinder from a Scenes object") {
      SceneFinder.fromScenes(scenes) shouldEqual sceneFinder
    }

    it("should report the correct number of scenes") {
      SceneFinder.fromScenes(scenes).sceneCount shouldEqual 2
    }

    it("should be able to produce a list of ScenePositions") {
      SceneFinder.fromScenes(scenes).toList shouldEqual List(ScenePosition(0, sceneA.name), ScenePosition(1, sceneB.name))
    }

    it("should be able to produce a non-empty list of ScenePositions") {
      val a = SceneFinder.fromScenes(scenes).toNel
      val b = NonEmptyList(ScenePosition(0, sceneA.name), List(ScenePosition(1, sceneB.name)))

      NonEmptyList.equality(a, b) shouldEqual true
    }

    it("should be able give the current scene") {
      SceneFinder.fromScenes(scenes).current shouldEqual ScenePosition(0, sceneA.name)
    }

    it("should be able go forward") {
      SceneFinder.fromScenes(scenes).forward.current shouldEqual ScenePosition(1, sceneB.name)
    }

    it("should be able go backward") {
      SceneFinder.fromScenes(scenes).forward.backward.current shouldEqual ScenePosition(0, sceneA.name)
    }

    it("should be able go forward, backward, and forward again") {
      SceneFinder.fromScenes(scenes).forward.backward.forward.current shouldEqual ScenePosition(1, sceneB.name)
    }

    it("should be able to jump to a scene by index") {
      sceneFinder.jumpToSceneByPosition(1).current shouldEqual ScenePosition(1, sceneB.name)
    }

    it("should reject invalid index numbers to jump to") {
      sceneFinder.jumpToSceneByPosition(10).current shouldEqual ScenePosition(0, sceneA.name)
      sceneFinder.jumpToSceneByPosition(-1).current shouldEqual ScenePosition(0, sceneA.name)
    }

    it("should be able to jump to a scene by name") {
      sceneFinder.jumpToSceneByName(sceneB.name).current shouldEqual ScenePosition(1, sceneB.name)
    }

    it("should reject invalid scene name to jump to") {
      sceneFinder.jumpToSceneByName(SceneName("foo")).current shouldEqual ScenePosition(0, sceneA.name)
    }

  }

}
