package com.purplekingdomgames.indigoexts.scenemanager

import com.purplekingdomgames.indigo.gameengine.GameTime
import com.purplekingdomgames.indigo.gameengine.events.{FrameInputEvents, GameEvent}
import com.purplekingdomgames.indigo.gameengine.scenegraph.SceneUpdateFragment
import com.purplekingdomgames.indigoexts.lenses.Lens

trait Scene[GameModel, ViewModel, SceneModel, SceneViewModel] {

  val name: SceneName

  val sceneModelLens: Lens[GameModel, SceneModel]
  val sceneViewModelLens: Lens[ViewModel, SceneViewModel]

  def updateSceneModel(gameTime: GameTime, sceneModel: SceneModel): GameEvent => SceneModel

  def updateSceneViewModel(gameTime: GameTime,
                           sceneModel: SceneModel,
                           sceneViewModel: SceneViewModel,
                           frameInputEvents: FrameInputEvents): SceneViewModel

  def updateSceneView(gameTime: GameTime,
                      sceneModel: SceneModel,
                      sceneViewModel: SceneViewModel,
                      frameInputEvents: FrameInputEvents): SceneUpdateFragment

  def updateModelDelegate(gameTime: GameTime, gameModel: GameModel): GameEvent => GameModel =
    e => sceneModelLens.set(gameModel, updateSceneModel(gameTime, sceneModelLens.get(gameModel))(e))

  def updateViewModelDelegate(gameTime: GameTime,
                              model: GameModel,
                              viewModel: ViewModel,
                              frameInputEvents: FrameInputEvents): ViewModel =
    sceneViewModelLens.set(
      viewModel,
      updateSceneViewModel(gameTime, sceneModelLens.get(model), sceneViewModelLens.get(viewModel), frameInputEvents)
    )

  def updateViewDelegate(gameTime: GameTime,
                         model: GameModel,
                         viewModel: ViewModel,
                         frameInputEvents: FrameInputEvents): SceneUpdateFragment =
    updateSceneView(gameTime, sceneModelLens.get(model), sceneViewModelLens.get(viewModel), frameInputEvents)

//  def ::(
//      scenes: ScenesList[GameModel, ViewModel, _, _]
//  ): Scenes[GameModel, ViewModel, Scene[GameModel, ViewModel, SceneModel, SceneViewModel]] =
//    ScenesList[GameModel, ViewModel, Scene[GameModel, ViewModel, SceneModel, SceneViewModel], scenes.Repr](this, scenes)
//    Scenes.cons[GameModel, ViewModel, Scene[GameModel, ViewModel, SceneModel, SceneViewModel], scenes.Repr](this, scenes)
}

case class SceneName(name: String) extends AnyVal {
  def ===(other: SceneName): Boolean =
    name == other.name
}
