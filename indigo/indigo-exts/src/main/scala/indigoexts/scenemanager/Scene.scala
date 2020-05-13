package indigoexts.scenemanager

import indigo.shared.Outcome
import indigo.shared.time.GameTime
import indigo.shared.events.{InputState, GlobalEvent}
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.EqualTo
import indigoexts.lenses.Lens
import indigo.shared.dice.Dice
import indigoexts.subsystems.SubSystem
import indigo.shared.BoundaryLocator

trait Scene[GameModel, ViewModel] {
  type SceneModel
  type SceneViewModel

  val name: SceneName
  val sceneModelLens: Lens[GameModel, SceneModel]
  val sceneViewModelLens: Lens[ViewModel, SceneViewModel]

  val sceneSubSystems: Set[SubSystem]

  def updateSceneModel(gameTime: GameTime, sceneModel: SceneModel, inputState: InputState, dice: Dice): GlobalEvent => Outcome[SceneModel]
  def updateSceneViewModel(gameTime: GameTime, sceneModel: SceneModel, sceneViewModel: SceneViewModel, inputState: InputState, dice: Dice): Outcome[SceneViewModel]
  def updateSceneView(gameTime: GameTime, sceneModel: SceneModel, sceneViewModel: SceneViewModel, inputState: InputState, boundaryLocator: BoundaryLocator): SceneUpdateFragment

}
object Scene {

  def updateModel[GM, VM](scene: Scene[GM, VM], gameTime: GameTime, gameModel: GM, inputState: InputState, dice: Dice): GlobalEvent => Outcome[GM] =
    e =>
      scene
        .updateSceneModel(gameTime, scene.sceneModelLens.get(gameModel), inputState, dice)(e)
        .mapState(scene.sceneModelLens.set(gameModel, _))

  def updateViewModel[GM, VM](scene: Scene[GM, VM], gameTime: GameTime, model: GM, viewModel: VM, inputState: InputState, dice: Dice): Outcome[VM] =
    scene
      .updateSceneViewModel(gameTime, scene.sceneModelLens.get(model), scene.sceneViewModelLens.get(viewModel), inputState, dice)
      .mapState(scene.sceneViewModelLens.set(viewModel, _))

  def updateView[GM, VM](scene: Scene[GM, VM], gameTime: GameTime, model: GM, viewModel: VM, inputState: InputState, boundaryLocator: BoundaryLocator): SceneUpdateFragment =
    scene.updateSceneView(gameTime, scene.sceneModelLens.get(model), scene.sceneViewModelLens.get(viewModel), inputState, boundaryLocator)

}

final class SceneName(val name: String) extends AnyVal
object SceneName {

  def apply(name: String): SceneName =
    new SceneName(name)

  implicit def EqSceneName(implicit eq: EqualTo[String]): EqualTo[SceneName] =
    EqualTo.create { (a, b) =>
      eq.equal(a.name, b.name)
    }

}
