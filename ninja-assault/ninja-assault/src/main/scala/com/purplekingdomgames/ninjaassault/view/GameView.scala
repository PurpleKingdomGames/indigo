package com.purplekingdomgames.ninjaassault.view

import com.purplekingdomgames.indigo.gameengine.scenegraph.{Graphic, Group, SceneGraphNode, SceneUpdateFragment}
import com.purplekingdomgames.ninjaassault.model._
import com.purplekingdomgames.ninjaassault.settings.{Assets, Settings}

object GameView {

  def draw(): SceneUpdateFragment =
    SceneUpdateFragment.empty.addGameLayerNodes(gameLayer)

  private def gameLayer: List[SceneGraphNode] =
    List(
      Group(
        drawGrid(Level.testLevel.grid) ++ drawActors(Level.testLevel.gameActors)
      ).moveTo(
        (Settings.activeScreenWidth - Level.testLevel.grid.levelWidthInPixels) / 2,
        -(Level.testLevel.grid.levelHeightInPixels - Settings.activeScreenHeight)
      )
    )

  private val drawGrid: Grid => List[Graphic] = grid =>
    grid.rows.flatMap { gridRow =>
      gridRow.columns
        .map {
          case GridSquare(row, column, Floor) =>
            Some(
              Graphic(column * grid.gridSize, row * grid.gridSize, 512, 512, 1, Assets.spriteSheetRef)
                .withCrop(0, 64, grid.gridSize, grid.gridSize)
            )

          case GridSquare(row, column, Column) =>
            Some(
              Graphic(column * grid.gridSize, row * grid.gridSize - 8, 512, 512, 1, Assets.spriteSheetRef)
                .withCrop(96, 64 - 8, grid.gridSize, grid.gridSize + 8)
            )
          case GridSquare(row, column, Wall) =>
            Some(
              Graphic(column * grid.gridSize, row * grid.gridSize - 16, 512, 512, 1, Assets.spriteSheetRef)
                .withCrop(32, 16, grid.gridSize, grid.gridSize + 16)
            )

          case _ =>
            None

        }
        .collect { case Some(s) => s }
  }

  private val drawActor: GameActor => Graphic = {
    case Guard(row, column) =>
      Graphic(column * 32, row * 32, 512, 512, 1, Assets.spriteSheetRef)
        .withCrop(32, 96, 32, 32)
        .withRef(-5, -3)

    case Ninja(row, column) =>
      Graphic(column * 32, row * 32, 512, 512, 1, Assets.spriteSheetRef)
        .withCrop(0, 96, 32, 32)
        .withRef(-10, -3)

  }

  private val drawActors: List[GameActor] => List[Graphic] = gameActors => gameActors.map(drawActor)

}
