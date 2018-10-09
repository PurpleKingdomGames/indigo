package indigoexts.scenemanager

import indigo.gameengine.GameTime
import indigo.gameengine.events.{FrameTick, GameEvent}
import org.scalatest.{FunSpec, Matchers}

class SceneManagerSpec extends FunSpec with Matchers {

  import TestScenes._

  val scenes: ScenesList[TestGameModel, TestViewModel, TestSceneA, _] = sceneA :: sceneB :: ScenesNil[TestGameModel, TestViewModel]()
  val sceneFinder: SceneFinder                                        = SceneFinder.fromScenes(scenes)

  val gameModel = TestGameModel(TestSceneModelA(0), TestSceneModelB(0))

  describe("A journey through the SceneManager") {

    it("Should be able to update a model on frametick") {

      val sceneManager = new SceneManager[TestGameModel, TestViewModel](scenes, sceneFinder)

      val events = List(FrameTick)

      val expected = TestGameModel(TestSceneModelA(1), TestSceneModelB(0))

      val actual = runModel(events, gameModel, sceneManager)

      actual shouldEqual expected

    }

    it("Should be able to change scenes and update on frametick") {

      val sceneManager = new SceneManager[TestGameModel, TestViewModel](scenes, sceneFinder)

      val events = List(SceneEvent.Next, FrameTick)

      val expected = TestGameModel(TestSceneModelA(0), TestSceneModelB(10))

      val actual = runModel(events, gameModel, sceneManager)

      actual shouldEqual expected
    }

    it("should be able to move between scenes and update the model accordingly") {

      val sceneManager = new SceneManager[TestGameModel, TestViewModel](scenes, sceneFinder)

      // A = 2, B = 40
      val events = List(
        FrameTick, // update scene A - 1
        SceneEvent.Next, // move to scene B
        FrameTick, // update scene B - 10
        SceneEvent.Next, // do nothing
        FrameTick, // update scene B - 20
        SceneEvent.Previous, // move to scene A
        SceneEvent.Next, // move to scene B,
        FrameTick, // update scene B - 30
        SceneEvent.Previous, // move to scene A
        FrameTick, // update scene A - 2
        SceneEvent.JumpTo(sceneB.name), // jump to scene B
        FrameTick // update scene B - 40
      )

      val expected = TestGameModel(TestSceneModelA(2), TestSceneModelB(40))

      val actual = runModel(events, gameModel, sceneManager)

      actual shouldEqual expected

    }

  }

  private def runModel(events: List[GameEvent], model: TestGameModel, sceneManager: SceneManager[TestGameModel, TestViewModel]): TestGameModel =
    events.foldLeft(model)((m, e) => sceneManager.updateModel(GameTime.now(16), m)(e))

}
