
package object indigoexts {

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

  object shapes {
    type LineSegment = geometry.LineSegment
    val LineSegment: geometry.LineSegment.type = geometry.LineSegment

    type LineProperties = geometry.LineProperties
    val LineProperties: geometry.LineProperties.type = geometry.LineProperties

    type IntersectionResult = geometry.IntersectionResult
    val IntersectionResult: geometry.IntersectionResult.type = geometry.IntersectionResult
  }

  object quadtree {
    type QuadBounds = quadtrees.QuadBounds
    val QuadBounds: quadtrees.QuadBounds.type = quadtrees.QuadBounds

    type QuadTree[T] = quadtrees.QuadTree[T]
    val QuadTree: quadtrees.QuadTree.type = quadtrees.QuadTree
  }

  object scenes {
    type Scene[GameModel, ViewModel] = scenemanager.Scene[GameModel, ViewModel]
    val Scene: scenemanager.Scene.type = scenemanager.Scene

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
    type ButtonAssets = indigoexts.uicomponents.ButtonAssets
    val ButtonAssets: indigoexts.uicomponents.ButtonAssets.type = indigoexts.uicomponents.ButtonAssets

    type InputField = indigoexts.uicomponents.InputField
    val InputField: indigoexts.uicomponents.InputField.type = indigoexts.uicomponents.InputField
    type InputFieldEvent = indigoexts.uicomponents.InputFieldEvent
    val InputFieldEvent: indigoexts.uicomponents.InputFieldEvent.type = indigoexts.uicomponents.InputFieldEvent
  }

}
