package snake.datatypes

import com.purplekingdomgames.indigoat.grid.{GridPoint, GridSize}
import snake.datatypes.CollisionCheckOutcome.{Crashed, NoCollision, PickUp}
import snake.datatypes.SnakeDirection.Up
import snake.datatypes.SnakeStatus.{Alive, Dead}
import snake.datatypes.TurnDirection.{TurnLeft, TurnRight}

sealed trait CollisionCheckOutcome {
  val gridPoint: GridPoint
}
object CollisionCheckOutcome {

  case class NoCollision(gridPoint: GridPoint) extends CollisionCheckOutcome

  case class PickUp(gridPoint: GridPoint) extends CollisionCheckOutcome

  case class Crashed(gridPoint: GridPoint) extends CollisionCheckOutcome

}

sealed trait SnakeStatus
object SnakeStatus {

  case object Alive extends SnakeStatus

  case object Dead extends SnakeStatus

}

case class Snake(start: GridPoint, body: List[GridPoint], direction: SnakeDirection, status: SnakeStatus) {

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
    Snake(start, Nil, Up, Alive)

  def apply(x: Int, y: Int): Snake =
    Snake(GridPoint(x, y), Nil, Up, Alive)

  def turnLeft(snake: Snake): Snake =
    snake.copy(direction = snake.direction.turnLeft)

  def turnRight(snake: Snake): Snake =
    snake.copy(direction = snake.direction.turnRight)

  def end(snake: Snake): GridPoint =
    snake.body.reverse.headOption.getOrElse(snake.start)

  def grow(snake: Snake): Snake =
    snake.copy(body = snake.body :+ end(snake))

  def crash(snake: Snake): Snake =
    snake.copy(status = Dead)

  def update(snake: Snake, gridSize: GridSize, collisionCheck: GridPoint => CollisionCheckOutcome): (Snake, CollisionCheckOutcome) =
    (nextPosition(gridSize) andThen collisionCheck andThen snakeUpdate(snake)) (snake)

  def nextPosition(gridSize: GridSize): Snake => GridPoint = snake =>
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

  def moveToPosition(snake: Snake, snakePoint: GridPoint): Snake =
    snake match {
      case Snake(_, Nil, d, s) =>
        Snake(snakePoint, Nil, d, s)

      case Snake(h, l, d, s) =>
        Snake(snakePoint, h :: l.reverse.tail.reverse, d, s)
    }

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

  def oneSquareForward(current: GridPoint): GridPoint =
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

  def oneSquareForward(snakeDirection: SnakeDirection, current: GridPoint): GridPoint =
    snakeDirection match {
      case Up =>
        current + GridPoint.Up

      case Down =>
        current + GridPoint.Down

      case Left =>
        current + GridPoint.Left

      case Right =>
        current + GridPoint.Right
    }

}