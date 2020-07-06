package indigo.scenes

import indigo.shared.time.GameTime
import indigo.shared.events.{InputState, GlobalEvent}
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.Outcome
import indigo.shared.dice.Dice
import indigo.shared.subsystems.SubSystem
import indigo.shared.BoundaryLocator
import indigo.shared.FrameContext

object TestScenes {

  val sceneA: TestSceneA = TestSceneA()
  val sceneB: TestSceneB = TestSceneB()

  val sceneNameA: SceneName = sceneA.name
  val sceneNameB: SceneName = sceneB.name

}

final case class TestGameModel(sceneA: TestSceneModelA, sceneB: TestSceneModelB)
final case class TestViewModel(sceneA: TestSceneViewModelA, sceneB: TestSceneViewModelB)

final case class TestSceneA() extends Scene[Unit, TestGameModel, TestViewModel] {
  type SceneModel     = TestSceneModelA
  type SceneViewModel = TestSceneViewModelA

  val name: SceneName = SceneName("test scene a")

  val modelLens: Lens[TestGameModel, TestSceneModelA] =
    Lens(
      m => m.sceneA,
      (m, mm) => m.copy(sceneA = mm)
    )
  val viewModelLens: Lens[TestViewModel, TestSceneViewModelA] =
    Lens(
      m => m.sceneA,
      (m, mm) => m.copy(sceneA = mm)
    )

  val subSystems: Set[SubSystem] = Set()

  def updateModel(context: FrameContext[Unit], sceneModel: TestSceneModelA): GlobalEvent => Outcome[TestSceneModelA] =
    _ => Outcome(sceneModel.copy(count = sceneModel.count + 1))

  def updateViewModel(context: FrameContext[Unit], sceneModel: TestSceneModelA, sceneViewModel: TestSceneViewModelA): GlobalEvent => Outcome[TestSceneViewModelA] =
    _ => Outcome(TestSceneViewModelA())

  def present(context: FrameContext[Unit], sceneModel: TestSceneModelA, sceneViewModel: TestSceneViewModelA): SceneUpdateFragment =
    SceneUpdateFragment.empty
}

final case class TestSceneModelA(count: Int)
final case class TestSceneViewModelA()

final case class TestSceneB() extends Scene[Unit, TestGameModel, TestViewModel] {
  type SceneModel     = TestSceneModelB
  type SceneViewModel = TestSceneViewModelB

  val name: SceneName = SceneName("test scene b")

  val modelLens: Lens[TestGameModel, TestSceneModelB] =
    Lens(
      m => m.sceneB,
      (m, mm) => m.copy(sceneB = mm)
    )

  val viewModelLens: Lens[TestViewModel, TestSceneViewModelB] =
    Lens(
      m => m.sceneB,
      (m, mm) => m.copy(sceneB = mm)
    )

  val subSystems: Set[SubSystem] = Set()

  def updateModel(context: FrameContext[Unit], sceneModel: TestSceneModelB): GlobalEvent => Outcome[TestSceneModelB] =
    _ => Outcome(sceneModel.copy(count = sceneModel.count + 10))

  def updateViewModel(context: FrameContext[Unit], sceneModel: TestSceneModelB, sceneViewModel: TestSceneViewModelB): GlobalEvent => Outcome[TestSceneViewModelB] =
    _ => Outcome(TestSceneViewModelB())

  def present(context: FrameContext[Unit], sceneModel: TestSceneModelB, sceneViewModel: TestSceneViewModelB): SceneUpdateFragment =
    SceneUpdateFragment.empty
}

final case class TestSceneModelB(count: Int)
final case class TestSceneViewModelB()
