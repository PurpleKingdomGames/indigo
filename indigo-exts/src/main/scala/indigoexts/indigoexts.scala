import indigo._
import scala.concurrent.Future

package object indigoexts {

  // lenses
  type Lens[A, B] = indigoexts.lenses.Lens[A, B]
  val Lens: indigoexts.lenses.Lens.type = indigoexts.lenses.Lens

  // entry
  val Indigo: indigoexts.entry.Indigo.type = indigoexts.entry.Indigo
  type IndigoGameBasic[StartupData, Model, ViewModel]      = indigoexts.entry.IndigoGameBasic[StartupData, Model, ViewModel]
  type IndigoGameWithScenes[StartupData, Model, ViewModel] = indigoexts.entry.IndigoGameWithScenes[StartupData, Model, ViewModel]

  implicit val emptyConfigAsync: Future[Option[GameConfig]] = indigoexts.entry.emptyConfigAsync

  implicit val emptyAssetsAsync: Future[Set[AssetType]] = indigoexts.entry.emptyAssetsAsync

  val defaultGameConfig: GameConfig = indigoexts.entry.defaultGameConfig

  val noRender: SceneUpdateFragment = indigoexts.entry.noRender

  // scenes
  type Scene[GameModel, ViewModel, SceneModel, SceneViewModel]                                                             = indigoexts.scenemanager.Scene[GameModel, ViewModel, SceneModel, SceneViewModel]
  type ScenesList[GameModel, ViewModel, S1 <: Scene[GameModel, ViewModel, _, _], +S2 <: Scene[GameModel, ViewModel, _, _]] = indigoexts.scenemanager.ScenesList[GameModel, ViewModel, S1, S2]
  type ScenesNil[GameModel, ViewModel]                                                                                     = indigoexts.scenemanager.ScenesNil[GameModel, ViewModel]
  val ScenesNil: indigoexts.scenemanager.ScenesNil.type = indigoexts.scenemanager.ScenesNil
  type Scenes[GameModel, ViewModel, +T <: indigoexts.scenemanager.Scene[GameModel, ViewModel, _, _]] =
    indigoexts.scenemanager.Scenes[GameModel, ViewModel, T]
  type SceneName = indigoexts.scenemanager.SceneName
  val SceneName: indigoexts.scenemanager.SceneName.type = indigoexts.scenemanager.SceneName
  type SceneEvent    = indigoexts.scenemanager.SceneEvent
  type NextScene     = indigoexts.scenemanager.NextScene.type
  type PreviousScene = indigoexts.scenemanager.PreviousScene.type
  type JumpToScene   = indigoexts.scenemanager.JumpToScene
  val JumpToScene: indigoexts.scenemanager.JumpToScene.type = indigoexts.scenemanager.JumpToScene

}
