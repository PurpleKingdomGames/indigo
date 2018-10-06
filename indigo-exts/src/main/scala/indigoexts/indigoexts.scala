import indigo._
import indigoexts.automata.AutomataAliases

import scala.concurrent.Future

package object indigoexts extends AutomataAliases {

  // lenses
  type Lens[A, B] = lenses.Lens[A, B]
  val Lens: lenses.Lens.type = lenses.Lens

  // entry
  val Indigo: entry.Indigo.type = entry.Indigo
  type IndigoGameBasic[StartupData, Model, ViewModel]      = entry.IndigoGameBasic[StartupData, Model, ViewModel]
  type IndigoGameWithScenes[StartupData, Model, ViewModel] = entry.IndigoGameWithScenes[StartupData, Model, ViewModel]

  implicit val emptyConfigAsync: Future[Option[GameConfig]] = entry.emptyConfigAsync

  implicit val emptyAssetsAsync: Future[Set[AssetType]] = entry.emptyAssetsAsync

  val defaultGameConfig: GameConfig = entry.defaultGameConfig

  val noRender: SceneUpdateFragment = entry.noRender

  // scenes
  type Scene[GameModel, ViewModel, SceneModel, SceneViewModel]                                                             = scenemanager.Scene[GameModel, ViewModel, SceneModel, SceneViewModel]
  type ScenesList[GameModel, ViewModel, S1 <: Scene[GameModel, ViewModel, _, _], +S2 <: Scene[GameModel, ViewModel, _, _]] = scenemanager.ScenesList[GameModel, ViewModel, S1, S2]
  type ScenesNil[GameModel, ViewModel]                                                                                     = scenemanager.ScenesNil[GameModel, ViewModel]
  val ScenesNil: scenemanager.ScenesNil.type = scenemanager.ScenesNil
  type Scenes[GameModel, ViewModel, +T <: scenemanager.Scene[GameModel, ViewModel, _, _]] =
    scenemanager.Scenes[GameModel, ViewModel, T]
  type SceneName = scenemanager.SceneName
  val SceneName: scenemanager.SceneName.type = scenemanager.SceneName
  type SceneEvent    = scenemanager.SceneEvent
  type NextScene     = scenemanager.NextScene.type
  type PreviousScene = scenemanager.PreviousScene.type
  type JumpToScene   = scenemanager.JumpToScene
  val JumpToScene: scenemanager.JumpToScene.type = scenemanager.JumpToScene

  //ui
  type Button = ui.Button
  val Button: ui.Button.type = ui.Button
  type ButtonState = ui.ButtonState
  val ButtonState: ui.ButtonState.type = ui.ButtonState
  type ButtonEvent = ui.ButtonEvent
  val ButtonEvent: ui.ButtonEvent.type = ui.ButtonEvent
  type ButtonViewUpdate = ui.ButtonViewUpdate
  val ButtonViewUpdate: ui.ButtonViewUpdate.type = ui.ButtonViewUpdate
  type ButtonAssets = ui.ButtonAssets
  val ButtonAssets: ui.ButtonAssets.type = ui.ButtonAssets

  type InputField = ui.InputField
  val InputField: ui.InputField.type = ui.InputField

  type InputFieldOptions = ui.InputFieldOptions
  val InputFieldOptions: ui.InputFieldOptions.type = ui.InputFieldOptions

  type RenderedInputFieldElements = ui.RenderedInputFieldElements
  val RenderedInputFieldElements: ui.RenderedInputFieldElements.type = ui.RenderedInputFieldElements

  type InputFieldAssets = ui.InputFieldAssets
  val InputFieldAssets: ui.InputFieldAssets.type = ui.InputFieldAssets

  type InputFieldViewUpdate = ui.InputFieldViewUpdate
  val InputFieldViewUpdate: ui.InputFieldViewUpdate.type = ui.InputFieldViewUpdate

  type InputFieldEvent = ui.InputFieldEvent
  val InputFieldEvent: ui.InputFieldEvent.type = ui.InputFieldEvent

  type InputFieldState = ui.InputFieldState
  val InputFieldState: ui.InputFieldState.type = ui.InputFieldState

}
