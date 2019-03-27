package indigoexts.scenemanager

import indigo.Outcome
import indigo.gameengine.GameTime
import indigo.gameengine.events.{FrameInputEvents, GlobalEvent}
import indigo.gameengine.scenegraph.SceneUpdateFragment
import indigo.shared.EqualTo
import indigoexts.lenses.Lens

import indigo.EqualTo._

trait Scene[GameModel, ViewModel] {
  type SceneModel
  type SceneViewModel

  val name: SceneName
  val sceneModelLens: Lens[GameModel, SceneModel]
  val sceneViewModelLens: Lens[ViewModel, SceneViewModel]

  def updateSceneModel(gameTime: GameTime, sceneModel: SceneModel): GlobalEvent => Outcome[SceneModel]
  def updateSceneViewModel(gameTime: GameTime, sceneModel: SceneModel, sceneViewModel: SceneViewModel, frameInputEvents: FrameInputEvents): Outcome[SceneViewModel]
  def updateSceneView(gameTime: GameTime, sceneModel: SceneModel, sceneViewModel: SceneViewModel, frameInputEvents: FrameInputEvents): SceneUpdateFragment

}
object Scene {

  def updateModel[GM, VM](scene: Scene[GM, VM], gameTime: GameTime, gameModel: GM): GlobalEvent => Outcome[GM] =
    e => {
      val next = scene.updateSceneModel(gameTime, scene.sceneModelLens.get(gameModel))(e)
      new Outcome(
        scene.sceneModelLens.set(gameModel, next.state),
        next.globalEvents
      )
    }

  def updateViewModel[GM, VM](scene: Scene[GM, VM], gameTime: GameTime, model: GM, viewModel: VM, frameInputEvents: FrameInputEvents): Outcome[VM] = {
    val next = scene.updateSceneViewModel(gameTime, scene.sceneModelLens.get(model), scene.sceneViewModelLens.get(viewModel), frameInputEvents)
    new Outcome(
      scene.sceneViewModelLens.set(
        viewModel,
        next.state
      ),
      next.globalEvents
    )
  }

  def updateView[GM, VM](scene: Scene[GM, VM], gameTime: GameTime, model: GM, viewModel: VM, frameInputEvents: FrameInputEvents): SceneUpdateFragment =
    scene.updateSceneView(gameTime, scene.sceneModelLens.get(model), scene.sceneViewModelLens.get(viewModel), frameInputEvents)

}

final case class SceneName(name: String) extends AnyVal {
  def ===(other: SceneName): Boolean =
    name === other.name
}
object SceneName {

  implicit val EqSceneName: EqualTo[SceneName] =
    EqualTo.create[SceneName] { (a, b) =>
      a === b
    }

}
