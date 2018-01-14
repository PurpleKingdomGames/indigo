package snake

import snake.CollisionCheckOutcome.{Crashed, NoCollision, PickUp}
import snake.SnakeDirection.Up
import snake.SnakeStatus.{Alive, Dead}
import snake.TurnDirection.{TurnLeft, TurnRight}

case class GridSize(columns: Int, rows: Int, gridSquareSize: Int) {
  def asPowerOf2: Int =
    GridSize.asPowerOf2(this)

  val centre: GridPoint =
    GridPoint(columns / 2, rows / 2)
}

object GridSize {
  def asPowerOf2(gridSize: GridSize): Int = {
    def rec(maxLength: Int, size: Int): Int =
      if(size >= maxLength) size else rec(maxLength, size * 2)

    rec(if(gridSize.columns > gridSize.rows) gridSize.columns else gridSize.rows, 2)
  }
}

sealed trait CollisionCheckOutcome {
  val snakePoint: SnakePoint
}
object CollisionCheckOutcome {

  case class NoCollision(snakePoint: SnakePoint) extends CollisionCheckOutcome

  case class PickUp(snakePoint: SnakePoint) extends CollisionCheckOutcome

  case class Crashed(snakePoint: SnakePoint) extends CollisionCheckOutcome

}

sealed trait SnakeStatus
object SnakeStatus {

  case object Alive extends SnakeStatus

  case object Dead extends SnakeStatus

}

case class Snake(start: SnakePoint, body: List[SnakePoint], direction: SnakeDirection, status: SnakeStatus) {

  def turnLeft: Snake =
    Snake.turnLeft(this)

  def turnRight: Snake =
    Snake.turnRight(this)

  def update(gridSize: GridSize, collisionCheck: SnakePoint => CollisionCheckOutcome): (Snake, CollisionCheckOutcome) =
    Snake.update(this, gridSize, collisionCheck)

  def end: SnakePoint =
    Snake.end(this)

  def grow: Snake =
    Snake.grow(this)

  def crash: Snake =
    Snake.crash(this)

  def length: Int =
    1 + body.length

  def givePath: List[SnakePoint] =
    start :: body

  def givePathList: List[(Int, Int)] =
    (start :: body).map(p => (p.x, p.y))

}
object Snake {

  def apply(start: SnakePoint): Snake =
    Snake(start, Nil, Up, Alive)

  def apply(x: Int, y: Int): Snake =
    Snake(SnakePoint(x, y), Nil, Up, Alive)

  def turnLeft(snake: Snake): Snake =
    snake.copy(direction = snake.direction.turnLeft)

  def turnRight(snake: Snake): Snake =
    snake.copy(direction = snake.direction.turnRight)

  def end(snake: Snake): SnakePoint =
    snake.body.reverse.headOption.getOrElse(snake.start)

  def grow(snake: Snake): Snake =
    snake.copy(body = snake.body :+ end(snake))

  def crash(snake: Snake): Snake =
    snake.copy(status = Dead)

  def update(snake: Snake, gridSize: GridSize, collisionCheck: SnakePoint => CollisionCheckOutcome): (Snake, CollisionCheckOutcome) =
    (nextPosition(gridSize) andThen collisionCheck andThen snakeUpdate(snake)) (snake)

  def nextPosition(gridSize: GridSize): Snake => SnakePoint = snake =>
    snake.direction
      .oneSquareForward(snake.start)
      .wrap(gridSize)

  def snakeUpdate(snake: Snake): CollisionCheckOutcome => (Snake, CollisionCheckOutcome) = {
    case oc @ NoCollision(pt) =>
      (moveToPosition(snake, pt), oc)

    case oc @ PickUp(pt) =>
      (moveToPosition(snake.grow, pt), oc)

    case oc @ Crashed(_) =>
      (snake.crash, oc)
  }

  def moveToPosition(snake: Snake, snakePoint: SnakePoint): Snake =
    snake match {
      case Snake(_, Nil, d, s) =>
        Snake(snakePoint, Nil, d, s)

      case Snake(h, l, d, s) =>
        Snake(snakePoint, h :: l.reverse.tail.reverse, d, s)
    }

}

case class SnakePoint(x: Int, y: Int) {
  def +(other: SnakePoint): SnakePoint =
    SnakePoint.append(this, other)

  def wrap(gridSize: GridSize): SnakePoint =
    SnakePoint.wrap(this, gridSize)

  def asString: String =
    s"""($x, $y)"""
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
      x = if(snakePoint.x < 0) gridSize.columns else snakePoint.x % gridSize.columns,
      y = if(snakePoint.y < 0) gridSize.rows else snakePoint.y % gridSize.rows
    )

}

sealed trait TurnDirection
object TurnDirection {

  case object TurnLeft extends TurnDirection

  case object TurnRight extends TurnDirection

}

sealed trait SnakeDirection {

  def turnLeft: SnakeDirection =
    SnakeDirection.turn(this, TurnLeft)

  def turnRight: SnakeDirection =
    SnakeDirection.turn(this, TurnRight)

  def oneSquareForward(current: SnakePoint): SnakePoint =
    SnakeDirection.oneSquareForward(this, current)

}
object SnakeDirection {

  case object Up extends SnakeDirection
  case object Down extends SnakeDirection
  case object Left extends SnakeDirection
  case object Right extends SnakeDirection

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