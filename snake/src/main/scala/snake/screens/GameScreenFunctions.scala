package snake.screens

import com.purplekingdomgames.indigo.gameengine._
import com.purplekingdomgames.indigo.gameengine.events.{FrameTick, GameEvent, KeyboardEvent}
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.{AmbientLight, Point}
import com.purplekingdomgames.indigoexts.grid.{GridPoint, GridSize}
import snake._
import snake.arenas.{Arena, GameMap, MapElement}
import snake.arenas.MapElement.{Apple, Player1Start, Player2Start}
import snake.datatypes.{CollisionCheckOutcome, Snake}

object GameScreenFunctions {

  object Model {

    def initialModel(startupData: SnakeStartupData): GameScreenModel = {
      val snake = Snake(
        startupData.gridSize.centre.x,
        startupData.gridSize.centre.y
      ).grow.grow

      GameScreenModel(
        running = true,
        gridSize = startupData.gridSize,
        staticAssets = startupData.staticAssets,
        player1 = Player(
          snake = snake,
          tickDelay = 200,
          lastUpdated = 0,
          previousSnakePath = snake.givePath,
          playerType = Human,
          controlScheme = ControlScheme.directed
        ),
        gameMap = Arena.genLevel(startupData.gridSize)
      )
    }

    private def hitTest(gameMap: GameMap, body: List[GridPoint], lastPosition: GridPoint, useNextPosition: Boolean): GridPoint => CollisionCheckOutcome = nextPosition => {
      val pt: GridPoint = if (useNextPosition) nextPosition else lastPosition

      if (useNextPosition && body.contains(pt)) CollisionCheckOutcome.Crashed(pt)
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
    }

    def update(gameTime: GameTime, state: GameScreenModel): GameEvent => GameScreenModel = gameEvent =>
      if(state.running) {
        gameEvent match {
          case FrameTick =>
            state.player1.update(
              gameTime,
              state.gameMap.gridSize,
              hitTest(
                state.gameMap,
                state.player1.snake.givePath,
                state.player1.snake.start,
                useNextPosition = (gameTime.running - state.player1.lastUpdated) > state.player1.tickDelay
              )
            ) match {
              case (player, CollisionCheckOutcome.Crashed(_)) =>
                state.copy(
                  player1 = player,
                  running = false
                )

              case (player, CollisionCheckOutcome.PickUp(pt)) =>
                state.copy(
                  player1 = player.copy(snake = player.snake.grow),
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

    def coordsToPoint(point: GridPoint, gridSize: GridSize): Point =
      Point(point.x * gridSize.gridSquareSize, ((gridSize.rows - 1) - point.y) * gridSize.gridSquareSize)

    def update(gameTime: GameTime, model: GameScreenModel): SceneUpdateFragment =
      SceneUpdateFragment(
        gameLayer(gameTime, model, if(model.running) model.staticAssets.gameScreen.player4.alive else model.staticAssets.gameScreen.player4.dead),
        Nil,
        Nil,
        AmbientLight.Normal,
        Nil,
        SceneAudio.None
      )

    def gameLayer(gameTime: GameTime, currentState: GameScreenModel, snakeAsset: Graphic): List[SceneGraphNode] = {
      List(currentState.staticAssets.gameScreen.background) ++
        currentState.gameMap.findApples.map(a => currentState.staticAssets.gameScreen.apple.moveTo(coordsToPoint(a.gridPoint, currentState.gameMap.gridSize))) ++
        drawSnake(gameTime, currentState, snakeAsset)
    }

    def drawSnake(gameTime: GameTime, currentState: GameScreenModel, snakeAsset: Graphic): List[Graphic] = {
      currentState.player1.previousSnakePath.zip(currentState.player1.snake.givePath).map { case (start, end) =>
        snakeAsset.moveTo(
          Point.linearInterpolation(
            coordsToPoint(start, currentState.gameMap.gridSize),
            coordsToPoint(end, currentState.gameMap.gridSize),
            currentState.player1.tickDelay.toDouble,
            gameTime.running - currentState.player1.lastUpdated
          )
        )
      }
    }

  }

}
