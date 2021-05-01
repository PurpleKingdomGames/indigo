package snake.model

import indigo._
import snake.model.GameMap
import snake.model.snakemodel.{CollisionCheckOutcome, Snake}
import indigoextras.geometry.Vertex
import indigoextras.geometry.BoundingBox
import snake.model.MapElement
import snake.init.GameAssets
import snake.scenes.GameView
import snake.Score
import indigo.scenes.SceneEvent
import snake.scenes.GameOverScene

final case class GameModel(
    snake: Snake,
    gameState: GameState,
    gameMap: GameMap,
    score: Int,
    tickDelay: Seconds,
    controlScheme: ControlScheme,
    lastUpdated: Seconds
) {

  def update(gameTime: GameTime, dice: Dice, gridSquareSize: Int): GlobalEvent => Outcome[GameModel] = {
    case FrameTick if gameTime.running < lastUpdated + tickDelay =>
      Outcome(this)

    case FrameTick =>
      gameState match {
        case s @ GameState.Running(_, _) =>
          GameModel.updateRunning(
            gameTime,
            dice,
            this.copy(lastUpdated = gameTime.running),
            s,
            gridSquareSize
          )(FrameTick)

        case s @ GameState.Crashed(_, _, _, _) =>
          GameModel.updateCrashed(
            gameTime,
            this.copy(lastUpdated = gameTime.running),
            s
          )(FrameTick)
      }

    case e =>
      gameState match {
        case s @ GameState.Running(_, _) =>
          GameModel.updateRunning(gameTime, dice, this, s, gridSquareSize)(e)

        case s @ GameState.Crashed(_, _, _, _) =>
          GameModel.updateCrashed(gameTime, this, s)(e)
      }
  }

}

object GameModel {

  val ScoreIncrement: Int = 100

  def initialModel(gridSize: BoundingBox, controlScheme: ControlScheme): GameModel =
    GameModel(
      snake = Snake(
        gridSize.center.x.toInt,
        gridSize.center.y.toInt - (gridSize.center.y / 2).toInt
      ).grow.grow,
      gameState = GameState.Running.start,
      gameMap = GameMap.genLevel(gridSize),
      score = 0,
      tickDelay = Seconds(0.1),
      controlScheme = controlScheme,
      lastUpdated = Seconds.zero
    )

  def updateRunning(
      gameTime: GameTime,
      dice: Dice,
      state: GameModel,
      runningDetails: GameState.Running,
      gridSquareSize: Int
  ): GlobalEvent => Outcome[GameModel] = {
    case FrameTick =>
      val (updatedModel, collisionResult) =
        state.snake.update(state.gameMap.gridSize, hitTest(state.gameMap, state.snake.givePath)) match {
          case (s, outcome) =>
            (state.copy(snake = s, gameState = state.gameState.updateNow(gameTime.running, state.snake.direction)), outcome)
        }

      updateBasedOnCollision(gameTime, dice, gridSquareSize, updatedModel, collisionResult)

    case e: KeyboardEvent =>
      Outcome(
        state.copy(
          snake = state.controlScheme.instructSnake(e, state.snake, runningDetails.lastSnakeDirection)
        )
      )

    case _ =>
      Outcome(state)
  }

  def hitTest(gameMap: GameMap, body: List[Vertex]): Vertex => CollisionCheckOutcome =
    given CanEqual[Option[MapElement], Option[MapElement]] = CanEqual.derived
    pt => {
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

  def updateBasedOnCollision(
      gameTime: GameTime,
      dice: Dice,
      gridSquareSize: Int,
      gameModel: GameModel,
      collisionResult: CollisionCheckOutcome
  ): Outcome[GameModel] =
    collisionResult match {
      case CollisionCheckOutcome.Crashed(_) =>
        Outcome(
          gameModel.copy(
            gameState = gameModel.gameState match {
              case c @ GameState.Crashed(_, _, _, _) =>
                c

              case r @ GameState.Running(_, _) =>
                r.crash(gameTime.running, gameModel.snake.length)
            },
            tickDelay = gameModel.snake.length match {
              case l if l < 5  => Seconds(0.1)
              case l if l < 10 => Seconds(0.05)
              case l if l < 25 => Seconds(0.025)
              case _           => Seconds(0.015)
            }
          )
        ).addGlobalEvents(PlaySound(GameAssets.soundLose, Volume.Max))

      case CollisionCheckOutcome.PickUp(pt) =>
        Outcome(
          gameModel.copy(
            snake = gameModel.snake.grow,
            gameMap = gameModel.gameMap
              .removeApple(pt)
              .insertApple(
                MapElement.Apple(
                  gameModel.gameMap
                    .findEmptySpace(dice, pt :: gameModel.snake.givePath)
                )
              ),
            score = gameModel.score + ScoreIncrement
          )
        ).addGlobalEvents(
          PlaySound(GameAssets.soundPoint, Volume.Max),
          Score.spawnEvent(GameView.gridPointToPoint(pt, gameModel.gameMap.gridSize, gridSquareSize))
        )

      case CollisionCheckOutcome.NoCollision(_) =>
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
