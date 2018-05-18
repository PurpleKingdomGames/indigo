package com.purplekingdomgames.ninjaassault.view

import com.purplekingdomgames.indigo.gameengine.events.{FrameInputEvents, GlobalSignals, ViewEvent}
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.{AlignCenter, Point}
import com.purplekingdomgames.ninjaassault.model._
import com.purplekingdomgames.ninjaassault.settings.{Assets, Settings}
import com.purplekingdomgames.ninjaassault.{JumpToGame, JumpToMenu}

object View {

  def draw(gameModel: NinjaAssaultGameModel, frameInputEvents: FrameInputEvents): SceneUpdateFragment =
    gameModel.activeScene match {
      case Logo(_) => LogoView.draw(frameInputEvents) |+| SceneUpdateFragment.empty.addUiLayerNodes(cursor)
      case Menu(_) => MenuView.draw(frameInputEvents) |+| SceneUpdateFragment.empty.addUiLayerNodes(cursor)
      case Game(_) => GameView.draw() |+| SceneUpdateFragment.empty.addUiLayerNodes(cursor)
    }

  def cursor: Graphic =
    Graphic(
      GlobalSignals.MousePosition.x,
      GlobalSignals.MousePosition.y,
      32,
      32,
      1000,
      Assets.cursorRef
    )

}

object LogoView {

  def draw(frameInputEvents: FrameInputEvents): SceneUpdateFragment = {
    val events: List[ViewEvent] =
      if (frameInputEvents.wasMouseClickedWithin(Settings.screenArea)) List(JumpToMenu)
      else Nil

    SceneUpdateFragment.empty
      .addGameLayerNodes(gameLayer)
      .addViewEvents(events)
  }

  private def gameLayer: List[SceneGraphNode] =
    List(
      Graphic(0, 0, 256, 256, 1, Assets.logoRef).moveTo(Settings.activeScreenCenter - Point(128, 128))
    )

}

object MenuView {

  def draw(frameInputEvents: FrameInputEvents): SceneUpdateFragment = {
    val events: List[ViewEvent] =
      if (frameInputEvents.wasMouseClickedWithin(Settings.screenArea)) List(JumpToGame)
      else Nil

    SceneUpdateFragment.empty
      .addGameLayerNodes(gameLayer)
      .addViewEvents(events)
  }

  private def gameLayer: List[SceneGraphNode] =
    List(
      Text("CLICK TO START",
           Settings.activeHorizontalCenter,
           Settings.activeScreenHeight - 10 - Assets.fontInfo.unknownChar.bounds.height,
           1,
           Assets.fontKey).withAlignment(AlignCenter)
    )
}

object GameView {

  def draw(): SceneUpdateFragment =
    SceneUpdateFragment.empty.addGameLayerNodes(gameLayer)

  private def gameLayer: List[SceneGraphNode] =
    List(
      Group(
        drawGrid(LevelModel.initialLevel.grid) ++ drawActors(LevelModel.initialLevel.gameActors)
      ).moveTo(
        (Settings.activeScreenWidth - LevelModel.initialLevel.grid.levelWidthInPixels) / 2,
        -(LevelModel.initialLevel.grid.levelHeightInPixels - Settings.activeScreenHeight)
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
