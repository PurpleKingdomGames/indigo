import indigo._

import scala.concurrent.Future

package object indigoexts {

  object datatypes {
    type NonEmptyList[A] = indigoexts.collections.NonEmptyList[A]
    val NonEmptyList: indigoexts.collections.NonEmptyList.type = indigoexts.collections.NonEmptyList
  }

  object automaton {
    type Automaton = indigoexts.automata.Automaton

    type AutomataEvent = indigoexts.automata.AutomataEvent
    val AutomataEvent: indigoexts.automata.AutomataEvent.type = indigoexts.automata.AutomataEvent

    type AutomataModifier = indigoexts.automata.AutomataModifier
    val AutomataModifier: indigoexts.automata.AutomataModifier.type = indigoexts.automata.AutomataModifier

    val AutomataFarm: indigoexts.automata.AutomataFarm.type               = indigoexts.automata.AutomataFarm
    val SpawnedAutomaton: indigoexts.automata.SpawnedAutomaton.type       = indigoexts.automata.SpawnedAutomaton
    val AutomatonSeedValues: indigoexts.automata.AutomatonSeedValues.type = indigoexts.automata.AutomatonSeedValues
    val GraphicAutomaton: indigoexts.automata.GraphicAutomaton.type       = indigoexts.automata.GraphicAutomaton
    val SpriteAutomaton: indigoexts.automata.SpriteAutomaton.type         = indigoexts.automata.SpriteAutomaton
    val TextAutomaton: indigoexts.automata.TextAutomaton.type             = indigoexts.automata.TextAutomaton
    val AutomataPoolKey: indigoexts.automata.AutomataPoolKey.type         = indigoexts.automata.AutomataPoolKey
    val AutomataLifeSpan: indigoexts.automata.AutomataLifeSpan.type       = indigoexts.automata.AutomataLifeSpan
  }

  object entrypoint {
    val Indigo: entry.Indigo.type = entry.Indigo

    type IndigoGameBasic[StartupData, Model, ViewModel]      = entry.IndigoGameBasic[StartupData, Model, ViewModel]
    type IndigoGameWithScenes[StartupData, Model, ViewModel] = entry.IndigoGameWithScenes[StartupData, Model, ViewModel]

    type IndigoGame[StartupData, StartupError, GameModel, ViewModel] = entry.IndigoGameBase.IndigoGame[StartupData, StartupError, GameModel, ViewModel]
    val IndigoGameBase: gameengine.GameTime.type = gameengine.GameTime

    implicit val emptyConfigAsync: Future[Option[GameConfig]] = entry.emptyConfigAsync

    implicit val emptyAssetsAsync: Future[Set[AssetType]] = entry.emptyAssetsAsync

    val defaultGameConfig: GameConfig = entry.defaultGameConfig

    val noRender: SceneUpdateFragment = entry.noRender
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
    type Scene[GameModel, ViewModel, SceneModel, SceneViewModel] = scenemanager.Scene[GameModel, ViewModel, SceneModel, SceneViewModel]

    type ScenesList[GameModel, ViewModel, S1 <: Scene[GameModel, ViewModel, _, _], +S2 <: Scene[GameModel, ViewModel, _, _]] = scenemanager.ScenesList[GameModel, ViewModel, S1, S2]

    type ScenesNil[GameModel, ViewModel] = scenemanager.ScenesNil[GameModel, ViewModel]
    val ScenesNil: scenemanager.ScenesNil.type = scenemanager.ScenesNil

    type Scenes[GameModel, ViewModel, +T <: scenemanager.Scene[GameModel, ViewModel, _, _]] =
      scenemanager.Scenes[GameModel, ViewModel, T]

    type SceneName = scenemanager.SceneName
    val SceneName: scenemanager.SceneName.type = scenemanager.SceneName

    type SceneEvent = scenemanager.SceneEvent
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
