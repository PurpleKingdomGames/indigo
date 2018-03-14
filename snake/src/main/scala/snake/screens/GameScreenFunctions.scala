package snake.screens

import com.purplekingdomgames.indigo.gameengine._
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.Point
import com.purplekingdomgames.indigoat.grid.{GridPoint, GridSize}
import snake._
import snake.arenas.{Arena, GameMap, MapElement}
import snake.arenas.MapElement.{Apple, Player1Start, Player2Start}
import snake.datatypes.{CollisionCheckOutcome, Snake}

object GameScreenFunctions {

  object Model {

    def initialModel(startupData: SnakeStartupData): GameScreenModel =
      GameScreenModel(
        running = true,
        gridSize = startupData.gridSize,
        staticAssets = startupData.staticAssets,
        player1 = Player(
          snake = Snake(
            startupData.gridSize.centre.x,
            startupData.gridSize.centre.y
          ).grow.grow,
          tickDelay = 100,
          lastUpdated = 0,
          playerType = Human,
          controlScheme = ControlScheme.directed
        ),
        gameMap = Arena.genLevel(startupData.gridSize)
      )

    private val hitTest: GameMap => List[GridPoint] => GridPoint => CollisionCheckOutcome = gameMap => body => pt =>
      if(body.contains(pt)) CollisionCheckOutcome.Crashed(pt)
      else {
        gameMap.fetchElementAt(pt.x, pt.y) match {
          case Some(MapElement.Apple(_)) =>
            CollisionCheckOutcome.PickUp(pt)

          case Some(MapElement.Wall(_)) =>
            CollisionCheckOutcome.Crashed(pt)

          case Some(Player1Start(_)) =>
            CollisionCheckOutcome.NoCollision(pt)

          case Some(Player2Start(_)) =>
            CollisionCheckOutcome.NoCollision(pt)

          case None =>
            CollisionCheckOutcome.NoCollision(pt)
        }
      }

    def update(gameTime: GameTime, state: GameScreenModel): GameEvent => GameScreenModel = gameEvent =>
      if(state.running) {
        gameEvent match {
          case FrameTick =>
            state.player1.update(gameTime, state.gameMap.gridSize, hitTest(state.gameMap)(state.player1.snake.givePath)) match {
              case (player, CollisionCheckOutcome.Crashed(_)) =>
                state.copy(
                  player1 = player,
                  running = false
                )

              case (player, CollisionCheckOutcome.PickUp(pt)) =>
                state.copy(
                  player1 = player.copy(snake = player.snake.grow, tickDelay = player.tickDelay - 5),
                  gameMap = state.gameMap
                    .removeElement(pt)
                    .insertElement(
                      Apple(
                        state
                          .gameMap
                          .findEmptySpace(pt :: state.player1.snake.givePath)
                      )
                    )
                )

              case (player, CollisionCheckOutcome.NoCollision(_)) =>
                state.copy(
                  player1 = player
                )
            }

          case e: KeyboardEvent =>
            state.copy(
              player1 = state.player1.controlScheme.instructPlayer(e, state.player1)
            )

          case _ =>
            state
        }
      } else {
        state
      }

  }

  object View {

    def coordsToGridPoint(gridPoint: GridPoint, gridSize: GridSize): Point =
      Point(gridPoint.x * gridSize.gridSquareSize, ((gridSize.rows - 1) - gridPoint.y) * gridSize.gridSquareSize)

    def update(model: GameScreenModel): SceneGraphUpdate =
      SceneGraphUpdate(
        SceneGraphRootNode(
          game = gameLayer(model, if(model.running) model.staticAssets.gameScreen.player4.alive else model.staticAssets.gameScreen.player4.dead),
          lighting = SceneGraphLightingLayer.empty,
          ui = SceneGraphUiLayer.empty
        ),
        Nil
      )

    def gameLayer(currentState: GameScreenModel, snakeAsset: Graphic): SceneGraphGameLayer =
      SceneGraphGameLayer(currentState.staticAssets.gameScreen.background)
        .addChildren {
          currentState.gameMap.findApples.map(a => currentState.staticAssets.gameScreen.apple.moveTo(coordsToGridPoint(a.gridPoint, currentState.gameMap.gridSize)))
        }
        .addChildren(currentState.player1.snake.givePath.map(pt => snakeAsset.moveTo(coordsToGridPoint(pt, currentState.gameMap.gridSize))))

  }

}
