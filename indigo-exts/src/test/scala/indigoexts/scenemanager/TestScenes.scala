package indigoexts.scenemanager

import indigo.gameengine.GameTime
import indigo.gameengine.events.{FrameInputEvents, GlobalEvent}
import indigo.gameengine.scenegraph.SceneUpdateFragment
import indigo.{UpdatedModel, UpdatedViewModel}
import indigoexts.lenses.Lens

object TestScenes {

  val sceneA: TestSceneA = TestSceneA()
  val sceneB: TestSceneB = TestSceneB()

  val sceneNameA: SceneName = sceneA.name
  val sceneNameB: SceneName = sceneB.name

}

final case class TestGameModel(sceneA: TestSceneModelA, sceneB: TestSceneModelB)
final case class TestViewModel(sceneA: TestSceneViewModelA, sceneB: TestSceneViewModelB)

final case class TestSceneA() extends Scene[TestGameModel, TestViewModel, TestSceneModelA, TestSceneViewModelA] {
  val name: SceneName = SceneName("test scene a")

  val sceneModelLens: Lens[TestGameModel, TestSceneModelA] =
    Lens(
      m => m.sceneA,
      (m, mm) => m.copy(sceneA = mm)
    )
  val sceneViewModelLens: Lens[TestViewModel, TestSceneViewModelA] =
    Lens(
      m => m.sceneA,
      (m, mm) => m.copy(sceneA = mm)
    )

  def updateSceneModel(gameTime: GameTime, sceneModel: TestSceneModelA): GlobalEvent => UpdatedModel[TestSceneModelA] =
    _ => UpdatedModel(sceneModel.copy(count = sceneModel.count + 1))

  def updateSceneViewModel(gameTime: GameTime, sceneModel: TestSceneModelA, sceneViewModel: TestSceneViewModelA, frameInputEvents: FrameInputEvents): UpdatedViewModel[TestSceneViewModelA] =
    UpdatedViewModel(TestSceneViewModelA())

  def updateSceneView(gameTime: GameTime, sceneModel: TestSceneModelA, sceneViewModel: TestSceneViewModelA, frameInputEvents: FrameInputEvents): SceneUpdateFragment =
    SceneUpdateFragment.empty
}

final case class TestSceneModelA(count: Int)
final case class TestSceneViewModelA()

final case class TestSceneB() extends Scene[TestGameModel, TestViewModel, TestSceneModelB, TestSceneViewModelB] {
  val name: SceneName = SceneName("test scene b")

  val sceneModelLens: Lens[TestGameModel, TestSceneModelB] =
    Lens(
      m => m.sceneB,
      (m, mm) => m.copy(sceneB = mm)
    )

  val sceneViewModelLens: Lens[TestViewModel, TestSceneViewModelB] =
    Lens(
      m => m.sceneB,
      (m, mm) => m.copy(sceneB = mm)
    )

  def updateSceneModel(gameTime: GameTime, sceneModel: TestSceneModelB): GlobalEvent => UpdatedModel[TestSceneModelB] =
    _ => UpdatedModel(sceneModel.copy(count = sceneModel.count + 10))

  def updateSceneViewModel(gameTime: GameTime, sceneModel: TestSceneModelB, sceneViewModel: TestSceneViewModelB, frameInputEvents: FrameInputEvents): UpdatedViewModel[TestSceneViewModelB] =
    UpdatedViewModel(TestSceneViewModelB())

  def updateSceneView(gameTime: GameTime, sceneModel: TestSceneModelB, sceneViewModel: TestSceneViewModelB, frameInputEvents: FrameInputEvents): SceneUpdateFragment =
    SceneUpdateFragment.empty
}

final case class TestSceneModelB(count: Int)
final case class TestSceneViewModelB()
