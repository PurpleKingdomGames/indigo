package indigoexts.scenemanager

import utest._

import indigo.collections.NonEmptyList

object SceneFinderTests extends TestSuite {

  import TestScenes._

  val scenes: ScenesList[TestGameModel, TestViewModel] = sceneA :: sceneB :: ScenesNil[TestGameModel, TestViewModel]()

  val sceneFinder: SceneFinder =
    SceneFinder(
      Nil,
      ScenePosition(0, sceneA.name),
      List(ScenePosition(1, sceneB.name))
    )

  val tests: Tests =
    Tests {
      "managing the scenes list" - {

        "should be able to construct a SceneFinder from a Scenes object" - {
          SceneFinder.fromScenes(scenes) ==> sceneFinder
        }

        "should report the correct number of scenes" - {
          SceneFinder.fromScenes(scenes).sceneCount ==> 2
        }

        "should be able to produce a list of ScenePositions" - {
          SceneFinder.fromScenes(scenes).toList ==> List(ScenePosition(0, sceneA.name), ScenePosition(1, sceneB.name))
        }

        "should be able to produce a non-empty list of ScenePositions" - {
          val a = SceneFinder.fromScenes(scenes).toNel
          val b = NonEmptyList(ScenePosition(0, sceneA.name), ScenePosition(1, sceneB.name))

          NonEmptyList.equality(a, b) ==> true
        }

        "should be able give the current scene" - {
          SceneFinder.fromScenes(scenes).current ==> ScenePosition(0, sceneA.name)
        }

        "should be able go forward" - {
          SceneFinder.fromScenes(scenes).forward.current ==> ScenePosition(1, sceneB.name)
        }

        "should be able go backward" - {
          SceneFinder.fromScenes(scenes).forward.backward.current ==> ScenePosition(0, sceneA.name)
        }

        "should be able go forward, backward, and forward again" - {
          SceneFinder.fromScenes(scenes).forward.backward.forward.current ==> ScenePosition(1, sceneB.name)
        }

        "should be able to jump to a scene by index" - {
          sceneFinder.jumpToSceneByPosition(1).current ==> ScenePosition(1, sceneB.name)
        }

        "should reject invalid index numbers to jump to" - {
          sceneFinder.jumpToSceneByPosition(10).current ==> ScenePosition(0, sceneA.name)
          sceneFinder.jumpToSceneByPosition(-1).current ==> ScenePosition(0, sceneA.name)
        }

        "should be able to jump to a scene by name" - {
          sceneFinder.jumpToSceneByName(sceneB.name).current ==> ScenePosition(1, sceneB.name)
        }

        "should reject invalid scene name to jump to" - {
          sceneFinder.jumpToSceneByName(SceneName("foo")).current ==> ScenePosition(0, sceneA.name)
        }

      }
    }

}
