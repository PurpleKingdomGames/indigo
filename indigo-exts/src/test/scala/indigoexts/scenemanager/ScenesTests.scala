package indigoexts.scenemanager

import utest._

import indigo.collections.NonEmptyList
import indigo.EqualTo._

object ScenesTests extends TestSuite {

  import TestScenes._

  val scenes: ScenesList[TestGameModel, TestViewModel] = sceneA :: sceneB :: ScenesNil[TestGameModel, TestViewModel]()

  val tests: Tests =
    Tests {
      "Scenes" - {

        "should build a heterogeneous Scenes instance" - {
          scenes.head ==> sceneA
          scenes.nextScene.head ==> sceneB
        }

        "should allow you to ask for the next scene" - {
          scenes.head ==> sceneA
          scenes.nextScene.head ==> sceneB
          scenes.nextScene.nextScene.head ==> sceneB // Reached the end, there is no next.
        }

        "should allow you to ask for a specific scene" - {
          scenes.findScene(sceneNameA) ==> Some(sceneA)
          scenes.findScene(sceneNameB) ==> Some(sceneB)
          scenes.findScene(SceneName("test scene c")) ==> None
        }

        "should allow you to list all scene names" - {
          scenes.listSceneNames === NonEmptyList(sceneNameA, sceneNameB) ==> true
        }

      }
    }

}
