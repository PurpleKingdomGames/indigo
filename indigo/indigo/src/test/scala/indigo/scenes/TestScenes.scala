package indigo.scenes

import indigo.*
import indigo.shared.events.EventFilters
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.subsystems.SubSystem

object TestScenes {

  val sceneA: TestSceneA = TestSceneA("test scene a")
  val sceneB: TestSceneB = TestSceneB("test scene b")

  val sceneNameA: SceneName = sceneA.name
  val sceneNameB: SceneName = sceneB.name

}

final case class TestGameModel(sceneA: TestSceneModelA, sceneB: TestSceneModelB)
final case class TestViewModel(sceneA: TestSceneViewModelA, sceneB: TestSceneViewModelB)

final case class TestSceneA(id: String) extends Scene[Unit, TestGameModel, TestViewModel] {
  type SceneModel     = TestSceneModelA
  type SceneViewModel = TestSceneViewModelA

  val name: SceneName = SceneName(id)

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

  private val modelEventFilter: GlobalEvent => Option[GlobalEvent] = {
    case TestSceneEvent1 => Some(TestSceneEvent1)
    case TestSceneEvent2 => None
    case TestSceneEvent3 => None
    case TestSceneEvent4 => None
    case e               => Some(e)
  }

  private val viewModelEventFilter: GlobalEvent => Option[GlobalEvent] = {
    case TestSceneEvent1 => None
    case TestSceneEvent2 => Some(TestSceneEvent2)
    case TestSceneEvent3 => None
    case TestSceneEvent4 => None
    case e               => Some(e)
  }

  val eventFilters: EventFilters =
    EventFilters(
      modelEventFilter,
      viewModelEventFilter
    )

  val subSystems: Set[SubSystem[TestGameModel]] = Set()

  def updateModel(context: SceneContext[Unit], sceneModel: TestSceneModelA): GlobalEvent => Outcome[TestSceneModelA] =
    _ =>
      // println(s"A - before: ${sceneModel.count}, after: ${sceneModel.count + 1}")
      Outcome(sceneModel.copy(count = sceneModel.count + 1))

  def updateViewModel(
      context: SceneContext[Unit],
      sceneModel: TestSceneModelA,
      sceneViewModel: TestSceneViewModelA
  ): GlobalEvent => Outcome[TestSceneViewModelA] =
    _ => Outcome(TestSceneViewModelA())

  def present(
      context: SceneContext[Unit],
      sceneModel: TestSceneModelA,
      sceneViewModel: TestSceneViewModelA
  ): Outcome[SceneUpdateFragment] =
    Outcome(SceneUpdateFragment.empty)
}

final case class TestSceneModelA(count: Int)
final case class TestSceneViewModelA()

final case class TestSceneB(id: String) extends Scene[Unit, TestGameModel, TestViewModel] {
  type SceneModel     = TestSceneModelB
  type SceneViewModel = TestSceneViewModelB

  val name: SceneName = SceneName(id)

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

  private val modelEventFilter: GlobalEvent => Option[GlobalEvent] = {
    case TestSceneEvent1 => None
    case TestSceneEvent2 => Some(TestSceneEvent2)
    case TestSceneEvent3 => Some(TestSceneEvent3)
    case TestSceneEvent4 => None
    case e               => Some(e)
  }

  private val viewModelEventFilter: GlobalEvent => Option[GlobalEvent] = {
    case TestSceneEvent1 => Some(TestSceneEvent1)
    case TestSceneEvent2 => None
    case TestSceneEvent3 => None
    case TestSceneEvent4 => Some(TestSceneEvent4)
    case e               => Some(e)
  }

  val eventFilters: EventFilters =
    EventFilters(
      modelEventFilter,
      viewModelEventFilter
    )

  val subSystems: Set[SubSystem[TestGameModel]] = Set()

  def updateModel(context: SceneContext[Unit], sceneModel: TestSceneModelB): GlobalEvent => Outcome[TestSceneModelB] =
    _ =>
      // println(s"B - before: ${sceneModel.count}, after: ${sceneModel.count + 10}")
      Outcome(sceneModel.copy(count = sceneModel.count + 10))

  def updateViewModel(
      context: SceneContext[Unit],
      sceneModel: TestSceneModelB,
      sceneViewModel: TestSceneViewModelB
  ): GlobalEvent => Outcome[TestSceneViewModelB] =
    _ => Outcome(TestSceneViewModelB())

  def present(
      context: SceneContext[Unit],
      sceneModel: TestSceneModelB,
      sceneViewModel: TestSceneViewModelB
  ): Outcome[SceneUpdateFragment] =
    Outcome(SceneUpdateFragment.empty)
}

final case class TestSceneModelB(count: Int)
final case class TestSceneViewModelB()

case object TestSceneEvent1 extends GlobalEvent
case object TestSceneEvent2 extends GlobalEvent
case object TestSceneEvent3 extends GlobalEvent
case object TestSceneEvent4 extends GlobalEvent
