package snake.gamelogic

import indigo._
import indigo.scenes._
import snake.init.GameAssets
import snake.model._
import snake.model.arena.MapElement.Apple
import snake.model.arena.{Arena, GameMap, MapElement}
import snake.model.snakemodel.{CollisionCheckOutcome, Snake}
import snake.scenes.GameOverScene
import indigoextras.geometry.BoundingBox
import indigoextras.geometry.Vertex

object ModelLogic {

  val ScoreIncrement: Int = 100

  def initialModel(gridSize: BoundingBox, controlScheme: ControlScheme): GameModel =
    GameModel(
      gridSize = gridSize,
      snake = Snake(
        gridSize.center.x.toInt,
        gridSize.center.y.toInt - (gridSize.center.y / 2).toInt
      ).grow.grow,
      gameState = GameState.Running.start,
      gameMap = Arena.genLevel(gridSize),
      score = 0,
      tickDelay = Seconds(0.1),
      controlScheme = controlScheme,
      lastUpdated = Seconds.zero
    )

  def update(gameTime: GameTime, dice: Dice, state: GameModel, gridSquareSize: Int): GlobalEvent => Outcome[GameModel] = {
    case FrameTick if gameTime.running < state.lastUpdated + state.tickDelay =>
      Outcome(state)

    case FrameTick =>
      state.gameState match {
        case s @ GameState.Running(_, _) =>
          updateRunning(
            gameTime,
            dice,
            state.resetLastUpdated(gameTime.running),
            s,
            gridSquareSize
          )(FrameTick)

        case s @ GameState.Crashed(_, _, _, _) =>
          updateCrashed(
            gameTime,
            state.resetLastUpdated(gameTime.running),
            s
          )(FrameTick)
      }

    case gameEvent =>
      state.gameState match {
        case s @ GameState.Running(_, _) =>
          updateRunning(gameTime, dice, state, s, gridSquareSize)(gameEvent)

        case s @ GameState.Crashed(_, _, _, _) =>
          updateCrashed(gameTime, state, s)(gameEvent)
      }
  }

  def updateRunning(
      gameTime: GameTime,
      dice: Dice,
      state: GameModel,
      runningDetails: GameState.Running,
      gridSquareSize: Int
  ): GlobalEvent => Outcome[GameModel] = {
    case FrameTick =>
      updateBasedOnCollision(gameTime, dice, gridSquareSize)(
        normalUpdate(gameTime, state)
      )

    case e: KeyboardEvent =>
      Outcome(
        state.copy(
          snake = state.controlScheme.instructSnake(e, state.snake, runningDetails.lastSnakeDirection)
        )
      )

    case _ =>
      Outcome(state)
  }

  def normalUpdate(
      gameTime: GameTime,
      state: GameModel
  ): (GameModel, CollisionCheckOutcome) =
    state.update(
      gameTime,
      state.gameMap.gridSize,
      hitTest(
        state.gameMap,
        state.snake.givePath
      )
    )

  def hitTest(
      gameMap: GameMap,
      body: List[Vertex]
  ): Vertex => CollisionCheckOutcome = pt => {
    if (body.contains(pt)) CollisionCheckOutcome.Crashed(pt)
    else
      gameMap.fetchElementAt(pt) match {
        case Some(MapElement.Apple(_)) =>
          CollisionCheckOutcome.PickUp(pt)

        case Some(MapElement.Wall(_)) =>
          CollisionCheckOutcome.Crashed(pt)

        case None =>
          CollisionCheckOutcome.NoCollision(pt)
      }
  }

  def updateBasedOnCollision(gameTime: GameTime, dice: Dice, gridSquareSize: Int): ((GameModel, CollisionCheckOutcome)) => Outcome[GameModel] = {
    case (gameModel, CollisionCheckOutcome.Crashed(_)) =>
      Outcome(
        gameModel.copy(
          gameState = gameModel.gameState match {
            case c @ GameState.Crashed(_, _, _, _) =>
              c

            case r @ GameState.Running(_, _) =>
              r.crash(gameTime.running, gameModel.snake.length)
          }
        )
      ).addGlobalEvents(PlaySound(GameAssets.soundLose, Volume.Max))

    case (gameModel, CollisionCheckOutcome.PickUp(pt)) =>
      Outcome(
        gameModel.copy(
          snake = gameModel.snake.grow,
          gameMap = gameModel.gameMap
            .removeElement(pt)
            .insertElement(
              Apple(
                gameModel.gameMap
                  .findEmptySpace(dice, pt :: gameModel.snake.givePath)
              )
            ),
          score = gameModel.score + ScoreIncrement
        )
      ).addGlobalEvents(
        PlaySound(GameAssets.soundPoint, Volume.Max),
        Score.spawnEvent(ViewLogic.gridPointToPoint(pt, gameModel.gameMap.gridSize, gridSquareSize))
      )

    case (gameModel, CollisionCheckOutcome.NoCollision(_)) =>
      Outcome(gameModel)
  }

  def updateCrashed(
      gameTime: GameTime,
      state: GameModel,
      crashDetails: GameState.Crashed
  ): GlobalEvent => Outcome[GameModel] = {
    case FrameTick if gameTime.running <= crashDetails.crashedAt + Seconds(0.75) =>
      //Pause briefly on collision
      Outcome(state)

    case FrameTick if state.snake.length > 1 =>
      Outcome(
        state.copy(
          snake = state.snake.shrink,
          gameState = state.gameState.updateNow(gameTime.running, state.gameState.lastSnakeDirection)
        )
      )

    case FrameTick if state.snake.length == 1 =>
      Outcome(state)
        .addGlobalEvents(SceneEvent.JumpTo(GameOverScene.name))

    case _ =>
      Outcome(state)
  }

}
