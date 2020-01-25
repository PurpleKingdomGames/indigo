package indigoexts.scenemanager

import indigo.shared.time.GameTime
import indigo.shared.events.{FrameTick, GlobalEvent}
import indigo.shared.collections.NonEmptyList
import indigo.shared.dice.Dice

import utest._
import indigo.shared.events.InputState

object SceneManagerTests extends TestSuite {

  import TestScenes._

  val scenes: NonEmptyList[Scene[TestGameModel, TestViewModel]] =
    NonEmptyList(sceneA, sceneB)

  val sceneFinder: SceneFinder = SceneFinder.fromScenes(scenes)

  val gameModel = TestGameModel(TestSceneModelA(0), TestSceneModelB(0))

  val tests: Tests =
    Tests {
      "A journey through the SceneManager" - {

        "Should be able to update a model on frametick" - {

          val sceneManager = new SceneManager[TestGameModel, TestViewModel](scenes, sceneFinder)

          val events = List(FrameTick)

          val expected = TestGameModel(TestSceneModelA(1), TestSceneModelB(0))

          val actual = runModel(events, gameModel, sceneManager)

          actual ==> expected

        }

        "Should be able to change scenes and update on frametick" - {

          val sceneManager = new SceneManager[TestGameModel, TestViewModel](scenes, sceneFinder)

          val events = List(SceneEvent.Next, FrameTick)

          val expected = TestGameModel(TestSceneModelA(0), TestSceneModelB(10))

          val actual = runModel(events, gameModel, sceneManager)

          actual ==> expected
        }

        "should be able to move between scenes and update the model accordingly" - {

          val sceneManager = new SceneManager[TestGameModel, TestViewModel](scenes, sceneFinder)

          // A = 2, B = 40
          val events = List(
            FrameTick,                      // update scene A - 1
            SceneEvent.Next,                // move to scene B
            FrameTick,                      // update scene B - 10
            SceneEvent.Next,                // do nothing
            FrameTick,                      // update scene B - 20
            SceneEvent.Previous,            // move to scene A
            SceneEvent.Next,                // move to scene B,
            FrameTick,                      // update scene B - 30
            SceneEvent.Previous,            // move to scene A
            FrameTick,                      // update scene A - 2
            SceneEvent.JumpTo(sceneB.name), // jump to scene B
            FrameTick                       // update scene B - 40
          )

          val expected = TestGameModel(TestSceneModelA(2), TestSceneModelB(40))

          val actual = runModel(events, gameModel, sceneManager)

          actual ==> expected

        }

      }
    }

  private def runModel(events: List[GlobalEvent], model: TestGameModel, sceneManager: SceneManager[TestGameModel, TestViewModel]): TestGameModel =
    events.foldLeft(model)((m, e) => sceneManager.updateModel(GameTime.zero, m, InputState.default, Dice.loaded(0))(e).state)

}
