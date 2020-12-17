package indigo.scenes

import indigo.shared.events.{FrameTick, GlobalEvent}
import indigo.shared.collections.NonEmptyList
import indigo.shared.time.Seconds
import indigo.shared.Outcome

class SceneManagerTests extends munit.FunSuite {

  import indigo.scenes.FakeFrameContext._

  import TestScenes._

  val scenes: NonEmptyList[Scene[Unit, TestGameModel, TestViewModel]] =
    NonEmptyList(sceneA, sceneB)

  val sceneFinder: SceneFinder = SceneFinder.fromScenes(scenes)

  val gameModel = TestGameModel(TestSceneModelA(0), TestSceneModelB(0))

  test("A journey through the SceneManager.Should be able to return a scenes modelEventFilter") {
    val events: List[GlobalEvent] =
      List(TestSceneEvent1, TestSceneEvent2, TestSceneEvent3, TestSceneEvent4)

    val sceneManager1 = new SceneManager[Unit, TestGameModel, TestViewModel](scenes, sceneFinder)
    val actual1       = events.map(sceneManager1.eventFilters.modelFilter).collect { case Some(e) => e }
    assertEquals(actual1.length, 1)
    assertEquals(actual1.head, TestSceneEvent1)

    val sceneManager2 = new SceneManager[Unit, TestGameModel, TestViewModel](scenes, sceneFinder.forward)
    val actual2       = events.map(sceneManager2.eventFilters.modelFilter).collect { case Some(e) => e }
    assertEquals(actual2.length, 2)
    assertEquals(actual2, List(TestSceneEvent2, TestSceneEvent3))
  }

  test("A journey through the SceneManager.Should be able to return a scenes viewModelEventFilter") {
    val events: List[GlobalEvent] =
      List(TestSceneEvent1, TestSceneEvent2, TestSceneEvent3, TestSceneEvent4)

    val sceneManager1 = new SceneManager[Unit, TestGameModel, TestViewModel](scenes, sceneFinder)
    val actual1       = events.map(sceneManager1.eventFilters.viewModelFilter).collect { case Some(e) => e }
    assertEquals(actual1.length, 1)
    assertEquals(actual1.head, TestSceneEvent2)

    val sceneManager2 = new SceneManager[Unit, TestGameModel, TestViewModel](scenes, sceneFinder.forward)
    val actual2       = events.map(sceneManager2.eventFilters.viewModelFilter).collect { case Some(e) => e }
    assertEquals(actual2.length, 2)
    assertEquals(actual2, List(TestSceneEvent1, TestSceneEvent4))
  }

  test("A journey through the SceneManager.Should be able to update a model on frametick") {

    val sceneManager = new SceneManager[Unit, TestGameModel, TestViewModel](scenes, sceneFinder)

    val events = List(FrameTick)

    val expected = TestGameModel(TestSceneModelA(1), TestSceneModelB(0))

    val actual = runModel(events, gameModel, sceneManager).unsafeGet

    assertEquals(actual, expected)

  }

  test("A journey through the SceneManager.Should be able to change scenes and update on frametick") {

    val sceneManager = new SceneManager[Unit, TestGameModel, TestViewModel](scenes, sceneFinder)

    val events = List(SceneEvent.Next, FrameTick)

    val expected = TestGameModel(TestSceneModelA(0), TestSceneModelB(10))

    val actual = runModel(events, gameModel, sceneManager).unsafeGet

    assertEquals(actual, expected)
  }

  test("A journey through the SceneManager.should be able to move between scenes and update the model accordingly") {

    val sceneManager = new SceneManager[Unit, TestGameModel, TestViewModel](scenes, sceneFinder)

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

    val expected =
      Outcome(TestGameModel(TestSceneModelA(2), TestSceneModelB(40)))
        .addGlobalEvents(
          List(
            SceneEvent.SceneChange(TestScenes.sceneNameA, TestScenes.sceneNameB, Seconds.zero),
            SceneEvent.SceneChange(TestScenes.sceneNameB, TestScenes.sceneNameA, Seconds.zero),
            SceneEvent.SceneChange(TestScenes.sceneNameA, TestScenes.sceneNameB, Seconds.zero),
            SceneEvent.SceneChange(TestScenes.sceneNameB, TestScenes.sceneNameA, Seconds.zero),
            SceneEvent.SceneChange(TestScenes.sceneNameA, TestScenes.sceneNameB, Seconds.zero)
          )
        )

    val actual = runModel(events, gameModel, sceneManager)

    assertEquals(actual, expected)

  }

  private def runModel(events: List[GlobalEvent], model: TestGameModel, sceneManager: SceneManager[Unit, TestGameModel, TestViewModel]): Outcome[TestGameModel] =
    events.foldLeft(Outcome(model))((m, e) => m.flatMap(mm => sceneManager.updateModel(context(6), mm)(e)))

}
