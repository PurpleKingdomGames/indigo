import indigo._
import indigoexts.automata.AutomataAliases
import indigoexts.collections.CollectionsAliases

import scala.concurrent.Future

package object indigoexts extends AutomataAliases with CollectionsAliases {

  // lenses
  type Lens[A, B] = lenses.Lens[A, B]
  val Lens: lenses.Lens.type = lenses.Lens

  // entry
  val Indigo: entry.Indigo.type = entry.Indigo
  type IndigoGameBasic[StartupData, Model, ViewModel]              = entry.IndigoGameBasic[StartupData, Model, ViewModel]
  type IndigoGameWithScenes[StartupData, Model, ViewModel]         = entry.IndigoGameWithScenes[StartupData, Model, ViewModel]
  type IndigoGame[StartupData, StartupError, GameModel, ViewModel] = entry.IndigoGameBase.IndigoGame[StartupData, StartupError, GameModel, ViewModel]

  val IndigoGameBase: gameengine.GameTime.type = gameengine.GameTime

  // useful
  implicit val emptyConfigAsync: Future[Option[GameConfig]] = entry.emptyConfigAsync

  implicit val emptyAssetsAsync: Future[Set[AssetType]] = entry.emptyAssetsAsync

  val defaultGameConfig: GameConfig = entry.defaultGameConfig

  val noRender: SceneUpdateFragment = entry.noRender

  // scenes
  object scenes {
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
  }

  //ui
  object ui {
    type Button = indigoexts.uicomponents.Button
    val Button: indigoexts.uicomponents.Button.type = indigoexts.uicomponents.Button
    type ButtonState = indigoexts.uicomponents.ButtonState
    val ButtonState: indigoexts.uicomponents.ButtonState.type = indigoexts.uicomponents.ButtonState
    type ButtonEvent = indigoexts.uicomponents.ButtonEvent
    val ButtonEvent: indigoexts.uicomponents.ButtonEvent.type = indigoexts.uicomponents.ButtonEvent
    type ButtonViewUpdate = indigoexts.uicomponents.ButtonViewUpdate
    val ButtonViewUpdate: indigoexts.uicomponents.ButtonViewUpdate.type = indigoexts.uicomponents.ButtonViewUpdate
    type ButtonAssets = indigoexts.uicomponents.ButtonAssets
    val ButtonAssets: indigoexts.uicomponents.ButtonAssets.type = indigoexts.uicomponents.ButtonAssets

    type InputField = indigoexts.uicomponents.InputField
    val InputField: indigoexts.uicomponents.InputField.type = indigoexts.uicomponents.InputField

    type InputFieldOptions = indigoexts.uicomponents.InputFieldOptions
    val InputFieldOptions: indigoexts.uicomponents.InputFieldOptions.type = indigoexts.uicomponents.InputFieldOptions

    type RenderedInputFieldElements = indigoexts.uicomponents.RenderedInputFieldElements
    val RenderedInputFieldElements: indigoexts.uicomponents.RenderedInputFieldElements.type = indigoexts.uicomponents.RenderedInputFieldElements

    type InputFieldAssets = indigoexts.uicomponents.InputFieldAssets
    val InputFieldAssets: indigoexts.uicomponents.InputFieldAssets.type = indigoexts.uicomponents.InputFieldAssets

    type InputFieldViewUpdate = indigoexts.uicomponents.InputFieldViewUpdate
    val InputFieldViewUpdate: indigoexts.uicomponents.InputFieldViewUpdate.type = indigoexts.uicomponents.InputFieldViewUpdate

    type InputFieldEvent = indigoexts.uicomponents.InputFieldEvent
    val InputFieldEvent: indigoexts.uicomponents.InputFieldEvent.type = indigoexts.uicomponents.InputFieldEvent

    type InputFieldState = indigoexts.uicomponents.InputFieldState
    val InputFieldState: indigoexts.uicomponents.InputFieldState.type = indigoexts.uicomponents.InputFieldState
  }

}
