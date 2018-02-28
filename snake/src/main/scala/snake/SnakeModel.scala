package snake

import com.purplekingdomgames.indigo.gameengine._
import snake.MapElement._

object SnakeModel {

  def initialModel(startupData: SnakeStartupData): SnakeModel =
    SnakeModel(
      running = true,
      staticAssets = startupData.staticAssets,
      player1 = Player(
        snake = Snake(
          startupData.gridSize.centre.x,
          startupData.gridSize.centre.y
        ).grow.grow,
        tickDelay = 100,
        lastUpdated = 0
      ),
      gameMap = genLevel(startupData.gridSize)
    )

  private def genLevel(gridSize: GridSize): GameMap =
    GameMap(gridSize)
      .insertElement(Player1Start(gridSize.centre))
      .insertElement(Apple(gridSize.centre + ((3, 2))))
      .insertElements(topEdgeWall(gridSize))
      .insertElements(rightEdgeWall(gridSize))
      .insertElements(bottomEdgeWall(gridSize))
      .insertElements(leftEdgeWall(gridSize))
      .removeElement(GridPoint(gridSize.width / 2, 0))
      .removeElement(GridPoint(gridSize.width / 2, gridSize.height - 1))
      .removeElement(GridPoint(0, gridSize.height / 2))
      .removeElement(GridPoint(gridSize.width - 1, gridSize.height / 2))
      .optimise

  private def topEdgeWall(gridSize: GridSize): List[Wall] =
    GridPoint.fillIncrementally(gridSize.topLeft, gridSize.topRight).map(Wall.apply)

  private def rightEdgeWall(gridSize: GridSize): List[Wall] =
    GridPoint.fillIncrementally(gridSize.topRight, gridSize.bottomRight).map(Wall.apply)

  private def bottomEdgeWall(gridSize: GridSize): List[Wall] =
    GridPoint.fillIncrementally(gridSize.bottomLeft, gridSize.bottomRight).map(Wall.apply)

  private def leftEdgeWall(gridSize: GridSize): List[Wall] =
    GridPoint.fillIncrementally(gridSize.topLeft, gridSize.bottomLeft).map(Wall.apply)

  private val hitTest: GameMap => List[SnakePoint] => SnakePoint => CollisionCheckOutcome = gameMap => body => pt =>
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

        case Some(Player3Start(_)) =>
          CollisionCheckOutcome.NoCollision(pt)

        case Some(Player4Start(_)) =>
          CollisionCheckOutcome.NoCollision(pt)

        case None =>
          CollisionCheckOutcome.NoCollision(pt)
      }
    }

  def updateModel(gameTime: GameTime, state: SnakeModel): GameEvent => SnakeModel = {
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
              .removeElement(GridPoint(pt.x, pt.y))
              .insertElement(
                Apple(
                  state
                    .gameMap
                    .findEmptySpace(GridPoint(pt.x, pt.y) :: state.player1.snake.givePath.map(p => GridPoint(p.x, p.y)))
                )
              )
          )

        case (player, CollisionCheckOutcome.NoCollision(_)) =>
          state.copy(
            player1 = player
          )
      }

    case KeyDown(Keys.LEFT_ARROW) =>
      state.copy(
        player1 = state.player1.turnLeft
      )

    case KeyDown(Keys.RIGHT_ARROW) =>
      state.copy(
        player1 = state.player1.turnRight
      )

    case _ =>
      state
  }

}

case class SnakeModel(running: Boolean, staticAssets: StaticAssets, player1: Player, gameMap: GameMap)

case class Player(snake: Snake, tickDelay: Int, lastUpdated: Double) {

  def update(gameTime: GameTime, gridSize: GridSize, collisionCheck: SnakePoint => CollisionCheckOutcome): (Player, CollisionCheckOutcome) =
    snake.update(gridSize, collisionCheck) match {
      case (s, outcome) if gameTime.running >= lastUpdated + tickDelay =>
        (this.copy(snake = s, lastUpdated = gameTime.running), outcome)

      case (_, outcome) =>
        (this, outcome)
    }

  def turnLeft: Player =
    this.copy(snake = snake.turnLeft)

  def turnRight: Player =
    this.copy(snake = snake.turnRight)

}
