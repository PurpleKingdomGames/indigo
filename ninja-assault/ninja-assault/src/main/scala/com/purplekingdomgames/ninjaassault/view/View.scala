package com.purplekingdomgames.ninjaassault.view

import com.purplekingdomgames.indigo.gameengine.events.{FrameInputEvents, GlobalSignals, ViewEvent}
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.{AlignCenter, AmbientLight}
import com.purplekingdomgames.ninjaassault.JumpToMenu
import com.purplekingdomgames.ninjaassault.model._
import com.purplekingdomgames.ninjaassault.settings.{Assets, Settings}

object View {

//  private val drawGrid: Grid => SceneGraphNodeBranch = grid =>
//    SceneGraphNodeBranch {
//      grid.rows.flatMap { gridRow =>
//        gridRow.columns.map {
//          case GridSquare(row, column, Floor) =>
//            Some(
//              Graphic(column * grid.gridSize, row * grid.gridSize, 512, 512, 1, Assets.spriteSheetRef)
//                .withCrop(0, 64, grid.gridSize, grid.gridSize)
//            )
//
//          case GridSquare(row, column, Column) =>
//            Some(
//              Graphic(column * grid.gridSize, row * grid.gridSize - 8, 512, 512, 1, Assets.spriteSheetRef)
//                .withCrop(96, 64 - 8, grid.gridSize, grid.gridSize + 8)
//            )
//          case GridSquare(row, column, Wall) =>
//            Some(
//              Graphic(column * grid.gridSize, row * grid.gridSize - 16, 512, 512, 1, Assets.spriteSheetRef)
//                .withCrop(32, 16, grid.gridSize, grid.gridSize + 16)
//            )
//
//          case _ =>
//            None
//
//        }.collect { case Some(s) => s }
//      }
//    }
//
//  private val drawActor: GameActor => SceneGraphNodeLeaf = {
//    case Guard(row, column) =>
//      Graphic(column * 32, row * 32, 512, 512, 1, Assets.spriteSheetRef)
//        .withCrop(32, 96, 32, 32)
//        .withRef(-5, -3)
//
//    case Ninja(row, column) =>
//      Graphic(column * 32, row * 32, 512, 512, 1, Assets.spriteSheetRef)
//        .withCrop(0, 96, 32, 32)
//        .withRef(-10, -3)
//
//  }
//
//  private val drawActors: List[GameActor] => SceneGraphNodeBranch = gameActors =>
//    SceneGraphNodeBranch(gameActors.map(drawActor))
//
//  private def gameLayer(currentState: NinjaAssaultGameModel): SceneGraphGameLayer =
//    SceneGraphGameLayer(
//      drawGrid(currentState.level.grid),
//      drawActors(currentState.level.gameActors)
//    )

  def generate(gameModel: NinjaAssaultGameModel, frameInputEvents: FrameInputEvents): SceneUpdateFragment =
    gameModel.activeScene match {
      case Logo(_) => LogoView.sceneGraph(frameInputEvents)
      case Menu(_) => MenuView.sceneGraph()
    }

  def cursor: Graphic =
    Graphic(
      GlobalSignals.MousePosition.x / Settings.gameSetup.magnification,
      GlobalSignals.MousePosition.y / Settings.gameSetup.magnification,
      32,
      32,
      1000,
      Assets.cursorRef
    ) // Hide cursor with -->  `* {cursor: none;}`

}

object LogoView {

  def sceneGraph(frameInputEvents: FrameInputEvents): SceneUpdateFragment = {
    val events: List[ViewEvent] =
      if (frameInputEvents.wasMouseClickedWithin(Settings.screenArea)) List(JumpToMenu)
      else Nil

    SceneUpdateFragment(
      gameLayer = gameLayer,
      lightingLayer = Nil,
      uiLayer = uiLayer,
      ambientLight = AmbientLight.Normal,
      viewEvents = events,
      audio = SceneAudio.None
    )
  }

  private def gameLayer: List[SceneGraphNode] =
    List(
      Graphic(72, 22, 256, 256, 1, Assets.logoRef)
    )

  private def uiLayer: List[SceneGraphNode] =
    List(
      View.cursor
    )

}

object MenuView {

  def sceneGraph(): SceneUpdateFragment =
    SceneUpdateFragment(
      gameLayer = gameLayer,
      lightingLayer = Nil,
      uiLayer = uiLayer,
      ambientLight = AmbientLight.Normal,
      viewEvents = Nil,
      audio = SceneAudio.None
    )

  private def gameLayer: List[SceneGraphNode] =
    List(
      Graphic(72, 22, 256, 256, 1, Assets.logoRef),
      Text("NINJA ASSAULT!", Settings.activeHorizontalCenter, 10, 1, Assets.fontKey).withAlignment(AlignCenter),
      Text("CLICK TO START",
           Settings.activeHorizontalCenter,
           Settings.activeScreenHeight - 10 - Assets.fontInfo.unknownChar.bounds.height,
           1,
           Assets.fontKey).withAlignment(AlignCenter)
    )

  private def uiLayer: List[SceneGraphNode] =
    List(
      View.cursor
    )
}
