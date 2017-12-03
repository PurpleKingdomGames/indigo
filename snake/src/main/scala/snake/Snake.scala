package snake

case class GridSize(x: Int, y: Int)

sealed trait CollisionCheckOutcome {
  val snakePoint: SnakePoint
}
case class NoCollision(snakePoint: SnakePoint) extends CollisionCheckOutcome
case class PickUp(snakePoint: SnakePoint) extends CollisionCheckOutcome
case class Crashed(snakePoint: SnakePoint) extends CollisionCheckOutcome

case class Snake(head: SnakePoint, body: List[SnakePoint], direction: SnakeDirection) {

  def turnLeft: Snake =
    Snake.turnLeft(this)

  def turnRight: Snake =
    Snake.turnRight(this)

  def update(gridSize: GridSize, collisionCheck: SnakePoint => CollisionCheckOutcome): Snake =
    Snake.update(this, gridSize, collisionCheck)

  def end: SnakePoint =
    Snake.end(this)

  def grow: Snake =
    Snake.grow(this)

}
object Snake {

  def turnLeft(snake: Snake): Snake =
    snake.copy(direction = snake.direction.turnLeft)

  def turnRight(snake: Snake): Snake =
    snake.copy(direction = snake.direction.turnRight)

  def end(snake: Snake): SnakePoint =
    snake.body.reverse.headOption.getOrElse(snake.head)

  def grow(snake: Snake): Snake =
    snake.copy(body = snake.body :+ end(snake))

  def update(snake: Snake, gridSize: GridSize, collisionCheck: SnakePoint => CollisionCheckOutcome): Snake =
    (nextPosition(gridSize) andThen collisionCheck andThen snakeUpdate(snake))(snake)

  def nextPosition(gridSize: GridSize): Snake => SnakePoint = snake =>
    snake.direction
      .oneSquareForward(snake.head)
      .wrap(gridSize)

  def snakeUpdate(snake: Snake): CollisionCheckOutcome => Snake = {
    case NoCollision(pt) =>
      moveToPosition(snake, pt)

    case PickUp(pt) =>
      moveToPosition(snake.grow, pt)

    case Crashed(_) =>
      snake
  }

  def moveToPosition(snake: Snake, snakePoint: SnakePoint): Snake =
    snake match {
      case Snake(_, Nil, d) =>
        Snake(snakePoint, Nil, d)

      case Snake(h, l, d) =>
        Snake(snakePoint, h :: l.reverse.tail.reverse, d)
    }

}

case class SnakePoint(x: Int, y: Int) {
  def +(other: SnakePoint): SnakePoint =
    SnakePoint.append(this, other)

  def wrap(gridSize: GridSize): SnakePoint =
    SnakePoint.wrap(this, gridSize)
}
object SnakePoint {

  val Up: SnakePoint = SnakePoint(0, 1)
  val Down: SnakePoint = SnakePoint(0, -1)
  val Left: SnakePoint = SnakePoint(-1, 0)
  val Right: SnakePoint = SnakePoint(1, 0)

  def identity: SnakePoint = SnakePoint(0, 0)

  def append(a: SnakePoint, b: SnakePoint): SnakePoint =
    SnakePoint(a.x + b.x, a.y + b.y)

  def wrap(snakePoint: SnakePoint, gridSize: GridSize): SnakePoint =
    snakePoint.copy(
      x = snakePoint.x % gridSize.x,
      y = snakePoint.y % gridSize.y
    )
}

sealed trait TurnDirection
case object TurnLeft extends TurnDirection
case object TurnRight extends TurnDirection

sealed trait SnakeDirection {

  def turnLeft: SnakeDirection =
    SnakeDirection.turn(this, TurnLeft)

  def turnRight: SnakeDirection =
    SnakeDirection.turn(this, TurnRight)

  def oneSquareForward(current: SnakePoint): SnakePoint =
    SnakeDirection.oneSquareForward(this, current)

}
object SnakeDirection {

  def turn(snakeDirection: SnakeDirection, turnDirection: TurnDirection): SnakeDirection =
    (snakeDirection, turnDirection) match {
      case (Up, TurnLeft) =>
        Left

      case (Up, TurnRight) =>
        Right

      case (Down, TurnLeft) =>
        Right

      case (Down, TurnRight) =>
        Left

      case (Left, TurnLeft) =>
        Down

      case (Left, TurnRight) =>
        Up

      case (Right, TurnLeft) =>
        Up

      case (Right, TurnRight) =>
        Down
    }

  def oneSquareForward(snakeDirection: SnakeDirection, current: SnakePoint): SnakePoint =
    snakeDirection match {
      case Up =>
        current + SnakePoint.Up

      case Down =>
        current + SnakePoint.Down

      case Left =>
        current + SnakePoint.Left

      case Right =>
        current + SnakePoint.Right
    }

}

case object Up extends SnakeDirection
case object Down extends SnakeDirection
case object Left extends SnakeDirection
case object Right extends SnakeDirection