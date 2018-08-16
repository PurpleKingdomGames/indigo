package com.purplekingdomgames.indigoexts.scenemanager

import com.purplekingdomgames.indigo.gameengine.GameTime
import com.purplekingdomgames.indigo.gameengine.events.{FrameInputEvents, GameEvent}
import com.purplekingdomgames.indigo.gameengine.scenegraph.SceneUpdateFragment
import com.purplekingdomgames.indigoexts.lenses.Lens

object TestScenes {

  val sceneA: TestSceneA = TestSceneA()
  val sceneB: TestSceneB = TestSceneB()

  val sceneNameA: SceneName = sceneA.name
  val sceneNameB: SceneName = sceneB.name

}

case class TestGameModel(sceneA: TestSceneModelA, sceneB: TestSceneModelB)
case class TestViewModel(sceneA: TestSceneViewModelA, sceneB: TestSceneViewModelB)

case class TestSceneA() extends Scene[TestGameModel, TestViewModel, TestSceneModelA, TestSceneViewModelA] {
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

  def updateSceneModel(gameTime: GameTime, sceneModel: TestSceneModelA): GameEvent => TestSceneModelA =
    _ => sceneModel.copy(count = sceneModel.count + 1)

  def updateSceneViewModel(gameTime: GameTime,
                           sceneModel: TestSceneModelA,
                           sceneViewModel: TestSceneViewModelA,
                           frameInputEvents: FrameInputEvents): TestSceneViewModelA =
    TestSceneViewModelA()

  def updateSceneView(gameTime: GameTime,
                      sceneModel: TestSceneModelA,
                      sceneViewModel: TestSceneViewModelA,
                      frameInputEvents: FrameInputEvents): SceneUpdateFragment =
    SceneUpdateFragment.empty
}

case class TestSceneModelA(count: Int)
case class TestSceneViewModelA()

case class TestSceneB() extends Scene[TestGameModel, TestViewModel, TestSceneModelB, TestSceneViewModelB] {
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

  def updateSceneModel(gameTime: GameTime, sceneModel: TestSceneModelB): GameEvent => TestSceneModelB =
    _ => sceneModel.copy(count = sceneModel.count + 10)

  def updateSceneViewModel(gameTime: GameTime,
                           sceneModel: TestSceneModelB,
                           sceneViewModel: TestSceneViewModelB,
                           frameInputEvents: FrameInputEvents): TestSceneViewModelB =
    TestSceneViewModelB()

  def updateSceneView(gameTime: GameTime,
                      sceneModel: TestSceneModelB,
                      sceneViewModel: TestSceneViewModelB,
                      frameInputEvents: FrameInputEvents): SceneUpdateFragment =
    SceneUpdateFragment.empty
}

case class TestSceneModelB(count: Int)
case class TestSceneViewModelB()
