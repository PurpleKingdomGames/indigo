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
    gridSize: BoundingBox,
    snake: Snake,
    gameState: GameState,
    gameMap: GameMap,
    score: Int,
    tickDelay: Seconds,
    controlScheme: ControlScheme,
    lastUpdated: Seconds
) {

  def resetLastUpdated(time: Seconds): GameModel =
    this.copy(lastUpdated = time)

  def update(
      gameTime: GameTime,
      gridSize: BoundingBox,
      collisionCheck: Vertex => CollisionCheckOutcome
  ): (GameModel, CollisionCheckOutcome) =
    snake.update(gridSize, collisionCheck) match {
      case (s, outcome) =>
        (this.copy(snake = s, gameState = gameState.updateNow(gameTime.running, snake.direction)), outcome)
    }
}

object GameModel {

  val ScoreIncrement: Int = 100

  def initialModel(gridSize: BoundingBox, controlScheme: ControlScheme): GameModel =
    GameModel(
      gridSize = gridSize,
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

  def updateBasedOnCollision(
      gameTime: GameTime,
      dice: Dice,
      gridSquareSize: Int
  ): ((GameModel, CollisionCheckOutcome)) => Outcome[GameModel] = {
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
