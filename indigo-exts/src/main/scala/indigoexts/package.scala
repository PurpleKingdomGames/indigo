
import scala.concurrent.Future

package object indigoexts {

  object entrypoint {
    val Indigo: entry.Indigo.type = entry.Indigo

    type IndigoGameBasic[StartupData, Model, ViewModel]      = entry.IndigoGameBasic[StartupData, Model, ViewModel]
    type IndigoGameWithScenes[StartupData, Model, ViewModel] = entry.IndigoGameWithScenes[StartupData, Model, ViewModel]

    type IndigoGame[StartupData, StartupError, GameModel, ViewModel] = entry.IndigoGameBase.IndigoGame[StartupData, StartupError, GameModel, ViewModel]

    implicit val emptyConfigAsync: Future[Option[indigo.shared.GameConfig]] = entry.emptyConfigAsync

    implicit val emptyAssetsAsync: Future[Set[indigo.shared.AssetType]] = entry.emptyAssetsAsync

    val defaultGameConfig: indigo.shared.GameConfig = entry.defaultGameConfig

    val noRender: indigo.gameengine.scenegraph.SceneUpdateFragment = entry.noRender
  }

  object lens {
    type Lens[A, B] = lenses.Lens[A, B]
    val Lens: lenses.Lens.type = lenses.Lens
  }

  object grids {
    type GridPoint = grid.GridPoint
    val GridPoint: grid.GridPoint.type = grid.GridPoint

    type GridSize = grid.GridSize
    val GridSize: grid.GridSize.type = grid.GridSize

    object pathfinding {
      type Coords = indigoexts.pathfinding.Coords
      val Coords: indigoexts.pathfinding.Coords.type = indigoexts.pathfinding.Coords

      type GridSquare = indigoexts.pathfinding.GridSquare
      val GridSquare: indigoexts.pathfinding.GridSquare.type = indigoexts.pathfinding.GridSquare

      type SearchGrid = indigoexts.pathfinding.SearchGrid
      val SearchGrid: indigoexts.pathfinding.SearchGrid.type = indigoexts.pathfinding.SearchGrid
    }
  }

  object lines {
    type LineSegment = line.LineSegment
    val LineSegment: line.LineSegment.type = line.LineSegment

    type LineProperties = line.LineProperties
    val LineProperties: line.LineProperties.type = line.LineProperties

    type IntersectionResult = line.IntersectionResult
    val IntersectionResult: line.IntersectionResult.type = line.IntersectionResult
  }

  object quadtree {
    type QuadBounds = quadtrees.QuadBounds
    val QuadBounds: quadtrees.QuadBounds.type = quadtrees.QuadBounds

    type QuadTree[T] = quadtrees.QuadTree[T]
    val QuadTree: quadtrees.QuadTree.type = quadtrees.QuadTree
  }

  object scenes {
    type Scene[GameModel, ViewModel] = scenemanager.Scene[GameModel, ViewModel]

    type ScenesList[GameModel, ViewModel] = scenemanager.ScenesList[GameModel, ViewModel]
    val ScenesList: scenemanager.ScenesList.type = scenemanager.ScenesList

    type ScenesNil[GameModel, ViewModel] = scenemanager.ScenesNil[GameModel, ViewModel]
    val ScenesNil: scenemanager.ScenesNil.type = scenemanager.ScenesNil

    type Scenes[GameModel, ViewModel] =
      scenemanager.Scenes[GameModel, ViewModel]

    type SceneName = scenemanager.SceneName
    val SceneName: scenemanager.SceneName.type = scenemanager.SceneName

    type SceneEvent = scenemanager.SceneEvent
    val SceneEvent: scenemanager.SceneEvent.type = scenemanager.SceneEvent
  }

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
