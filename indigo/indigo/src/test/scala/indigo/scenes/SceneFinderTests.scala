package indigo.scenes

import indigo.shared.collections.NonEmptyList

class SceneFinderTests extends munit.FunSuite {

  import TestScenes._

  val scenes: NonEmptyList[Scene[Unit, TestGameModel, TestViewModel]] =
    NonEmptyList(sceneA, sceneB)

  val sceneFinder: SceneFinder =
    SceneFinder(
      Nil,
      ScenePosition(0, sceneA.name),
      List(ScenePosition(1, sceneB.name))
    )

  test("managing the scenes list.should be able to construct a SceneFinder from a Scenes object") {
    assertEquals(SceneFinder.fromScenes(scenes) == sceneFinder, true)
  }

  test("managing the scenes list.should report the correct number of scenes") {
    assertEquals(SceneFinder.fromScenes(scenes).sceneCount, 2)
  }

  test("managing the scenes list.should be able to produce a list of ScenePositions") {
    assertEquals(
      SceneFinder.fromScenes(scenes).toList == List(ScenePosition(0, sceneA.name), ScenePosition(1, sceneB.name)),
      true
    )
  }

  test("managing the scenes list.should be able to produce a non-empty list of ScenePositions") {
    val a = SceneFinder.fromScenes(scenes).toNel
    val b = NonEmptyList(ScenePosition(0, sceneA.name), ScenePosition(1, sceneB.name))

    assertEquals(a == b, true)
  }

  test("managing the scenes list.should be able give the current scene") {
    assertEquals(SceneFinder.fromScenes(scenes).current == ScenePosition(0, sceneA.name), true)
  }

  test("managing the scenes list.should be able go forward") {
    assertEquals(SceneFinder.fromScenes(scenes).forward.current == ScenePosition(1, sceneB.name), true)
  }

  test("managing the scenes list.should be able go backward") {
    assertEquals(SceneFinder.fromScenes(scenes).forward.backward.current == ScenePosition(0, sceneA.name), true)
  }

  test("managing the scenes list.should be able go forward, backward, and forward again") {
    assertEquals(SceneFinder.fromScenes(scenes).forward.backward.forward.current == ScenePosition(1, sceneB.name), true)
  }

  test("managing the scenes list.should be able to jump to a scene by index") {
    assertEquals(sceneFinder.jumpToSceneByPosition(1).current == ScenePosition(1, sceneB.name), true)
  }

  test("managing the scenes list.should clamp invalid index numbers to jump to") {
    assertEquals(sceneFinder.jumpToSceneByPosition(10).current, ScenePosition(1, sceneB.name))
    assertEquals(sceneFinder.jumpToSceneByPosition(-1).current, ScenePosition(0, sceneA.name))
    assertEquals(sceneFinder.jumpToSceneByPosition(2).current, ScenePosition(1, sceneB.name))
  }

  test("managing the scenes list.should be able to jump to a scene by name") {
    assertEquals(sceneFinder.jumpToSceneByName(sceneB.name).current == ScenePosition(1, sceneB.name), true)
  }

  test("managing the scenes list.should reject invalid scene name to jump to") {
    assertEquals(sceneFinder.jumpToSceneByName(SceneName("foo")).current == ScenePosition(0, sceneA.name), true)
  }

}
