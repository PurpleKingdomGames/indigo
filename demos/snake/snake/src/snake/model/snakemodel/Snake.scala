package snake.model.snakemodel

import indigoexts.grids._

case class Snake(start: GridPoint, body: List[GridPoint], direction: SnakeDirection, status: SnakeStatus) {

  def goUp: Snake =
    Snake.goUp(this)

  def goDown: Snake =
    Snake.goDown(this)

  def goLeft: Snake =
    Snake.goLeft(this)

  def goRight: Snake =
    Snake.goRight(this)

  def turnLeft: Snake =
    Snake.turnLeft(this)

  def turnRight: Snake =
    Snake.turnRight(this)

  def update(gridSize: GridSize, collisionCheck: GridPoint => CollisionCheckOutcome): (Snake, CollisionCheckOutcome) =
    Snake.update(this, gridSize, collisionCheck)

  def end: GridPoint =
    Snake.end(this)

  def grow: Snake =
    Snake.grow(this)

  def shrink: Snake =
    Snake.shrink(this)

  def crash: Snake =
    Snake.crash(this)

  def length: Int =
    1 + body.length

  def givePath: List[GridPoint] =
    start :: body

  def givePathList: List[(Int, Int)] =
    (start :: body).map(p => (p.x, p.y))

}
object Snake {

  def apply(start: GridPoint): Snake =
    Snake(start, Nil, SnakeDirection.Up, SnakeStatus.Alive)

  def apply(x: Int, y: Int): Snake =
    Snake(GridPoint(x, y), Nil, SnakeDirection.Up, SnakeStatus.Alive)

  def turnLeft(snake: Snake): Snake =
    snake.copy(direction = snake.direction.turnLeft)

  def turnRight(snake: Snake): Snake =
    snake.copy(direction = snake.direction.turnRight)

  def goUp(snake: Snake): Snake =
    snake.copy(direction = snake.direction.goUp)

  def goDown(snake: Snake): Snake =
    snake.copy(direction = snake.direction.goDown)

  def goLeft(snake: Snake): Snake =
    snake.copy(direction = snake.direction.goLeft)

  def goRight(snake: Snake): Snake =
    snake.copy(direction = snake.direction.goRight)

  def end(snake: Snake): GridPoint =
    snake.body.reverse.headOption.getOrElse(snake.start)

  def grow(snake: Snake): Snake =
    snake.copy(body = snake.body :+ end(snake))

  def shrink(snake: Snake): Snake =
    snake.copy(body = snake.body.dropRight(1))

  def crash(snake: Snake): Snake =
    snake.copy(status = SnakeStatus.Dead)

  def update(
      snake: Snake,
      gridSize: GridSize,
      collisionCheck: GridPoint => CollisionCheckOutcome
  ): (Snake, CollisionCheckOutcome) =
    (nextPosition(gridSize) andThen collisionCheck andThen snakeUpdate(snake))(snake)

  def nextPosition(gridSize: GridSize): Snake => GridPoint =
    snake =>
      snake.direction
        .oneSquareForward(snake.start)
        .wrap(gridSize)

  def snakeUpdate(snake: Snake): CollisionCheckOutcome => (Snake, CollisionCheckOutcome) = {
    case oc @ CollisionCheckOutcome.NoCollision(pt) =>
      (moveToPosition(snake, pt), oc)

    case oc @ CollisionCheckOutcome.PickUp(pt) =>
      (moveToPosition(snake.grow, pt), oc)

    case oc @ CollisionCheckOutcome.Crashed(_) =>
      (snake.crash, oc)
  }

  def moveToPosition(snake: Snake, snakePoint: GridPoint): Snake =
    snake match {
      case Snake(_, Nil, d, s) =>
        Snake(snakePoint, Nil, d, s)

      case Snake(h, l, d, s) =>
        Snake(snakePoint, h :: l.reverse.drop(1).reverse, d, s)
    }

}
