package indigoexts.scenemanager

import indigo.shared.time.GameTime
import indigo.shared.events.{InputState, GlobalEvent}
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.Outcome
import indigoexts.lenses.Lens
import indigo.shared.dice.Dice
import indigoexts.subsystems.SubSystem

object TestScenes {

  val sceneA: TestSceneA = TestSceneA()
  val sceneB: TestSceneB = TestSceneB()

  val sceneNameA: SceneName = sceneA.name
  val sceneNameB: SceneName = sceneB.name

}

final case class TestGameModel(sceneA: TestSceneModelA, sceneB: TestSceneModelB)
final case class TestViewModel(sceneA: TestSceneViewModelA, sceneB: TestSceneViewModelB)

final case class TestSceneA() extends Scene[TestGameModel, TestViewModel] {
  type SceneModel     = TestSceneModelA
  type SceneViewModel = TestSceneViewModelA

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

  val sceneSubSystems: Set[SubSystem] = Set()

  def updateSceneModel(gameTime: GameTime, sceneModel: TestSceneModelA, dice: Dice): GlobalEvent => Outcome[TestSceneModelA] =
    _ => Outcome(sceneModel.copy(count = sceneModel.count + 1))

  def updateSceneViewModel(gameTime: GameTime, sceneModel: TestSceneModelA, sceneViewModel: TestSceneViewModelA, inputState: InputState, dice: Dice): Outcome[TestSceneViewModelA] =
    Outcome(TestSceneViewModelA())

  def updateSceneView(gameTime: GameTime, sceneModel: TestSceneModelA, sceneViewModel: TestSceneViewModelA, inputState: InputState): SceneUpdateFragment =
    SceneUpdateFragment.empty
}

final case class TestSceneModelA(count: Int)
final case class TestSceneViewModelA()

final case class TestSceneB() extends Scene[TestGameModel, TestViewModel] {
  type SceneModel     = TestSceneModelB
  type SceneViewModel = TestSceneViewModelB

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

  val sceneSubSystems: Set[SubSystem] = Set()

  def updateSceneModel(gameTime: GameTime, sceneModel: TestSceneModelB, dice: Dice): GlobalEvent => Outcome[TestSceneModelB] =
    _ => Outcome(sceneModel.copy(count = sceneModel.count + 10))

  def updateSceneViewModel(gameTime: GameTime, sceneModel: TestSceneModelB, sceneViewModel: TestSceneViewModelB, inputState: InputState, dice: Dice): Outcome[TestSceneViewModelB] =
    Outcome(TestSceneViewModelB())

  def updateSceneView(gameTime: GameTime, sceneModel: TestSceneModelB, sceneViewModel: TestSceneViewModelB, inputState: InputState): SceneUpdateFragment =
    SceneUpdateFragment.empty
}

final case class TestSceneModelB(count: Int)
final case class TestSceneViewModelB()
