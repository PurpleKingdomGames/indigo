package snake.model.snakemodel

import indigoextras.geometry.Vertex

enum SnakeDirection derives CanEqual:
  case Up    extends SnakeDirection
  case Down  extends SnakeDirection
  case Left  extends SnakeDirection
  case Right extends SnakeDirection

object SnakeDirection:
  extension (sd: SnakeDirection)
    def makeLegalTurn(snake: Snake): Option[Snake] =
      (sd, snake.direction) match {
        case (SnakeDirection.Up, SnakeDirection.Up)       => Some(snake)
        case (SnakeDirection.Up, SnakeDirection.Left)     => Some(snake)
        case (SnakeDirection.Up, SnakeDirection.Right)    => Some(snake)
        case (SnakeDirection.Down, SnakeDirection.Down)   => Some(snake)
        case (SnakeDirection.Down, SnakeDirection.Left)   => Some(snake)
        case (SnakeDirection.Down, SnakeDirection.Right)  => Some(snake)
        case (SnakeDirection.Left, SnakeDirection.Left)   => Some(snake)
        case (SnakeDirection.Left, SnakeDirection.Up)     => Some(snake)
        case (SnakeDirection.Left, SnakeDirection.Down)   => Some(snake)
        case (SnakeDirection.Right, SnakeDirection.Right) => Some(snake)
        case (SnakeDirection.Right, SnakeDirection.Up)    => Some(snake)
        case (SnakeDirection.Right, SnakeDirection.Down)  => Some(snake)
        case _                                            => None
      }

    def turnLeft: SnakeDirection =
      SnakeDirection.turn(sd, TurnDirection.Left)

    def turnRight: SnakeDirection =
      SnakeDirection.turn(sd, TurnDirection.Right)

    def goUp: SnakeDirection =
      SnakeDirection.go(sd, SnakeDirection.Up)

    def goDown: SnakeDirection =
      SnakeDirection.go(sd, SnakeDirection.Down)

    def goLeft: SnakeDirection =
      SnakeDirection.go(sd, SnakeDirection.Left)

    def goRight: SnakeDirection =
      SnakeDirection.go(sd, SnakeDirection.Right)

    def oneSquareForward(current: Vertex): Vertex =
      SnakeDirection.moveOneSquareForward(sd, current)

  def go(snakeDirection: SnakeDirection, goDirection: SnakeDirection): SnakeDirection =
    (snakeDirection, goDirection) match {
      case (Up, Left) =>
        Left

      case (Up, Right) =>
        Right

      case (Down, Left) =>
        Left

      case (Down, Right) =>
        Right

      case (Left, Up) =>
        Up

      case (Left, Down) =>
        Down

      case (Right, Up) =>
        Up

      case (Right, Down) =>
        Down

      case (current, _) =>
        current
    }

  def turn(snakeDirection: SnakeDirection, turnDirection: TurnDirection): SnakeDirection =
    (snakeDirection, turnDirection) match {
      case (Up, TurnDirection.Left) =>
        Left

      case (Up, TurnDirection.Right) =>
        Right

      case (Down, TurnDirection.Left) =>
        Right

      case (Down, TurnDirection.Right) =>
        Left

      case (Left, TurnDirection.Left) =>
        Down

      case (Left, TurnDirection.Right) =>
        Up

      case (Right, TurnDirection.Left) =>
        Up

      case (Right, TurnDirection.Right) =>
        Down
    }

  def moveOneSquareForward(snakeDirection: SnakeDirection, current: Vertex): Vertex =
    snakeDirection match {
      case Up =>
        current + MoveUp

      case Down =>
        current + MoveDown

      case Left =>
        current + MoveLeft

      case Right =>
        current + MoveRight
    }

  val MoveUp: Vertex    = Vertex(0, 1)
  val MoveDown: Vertex  = Vertex(0, -1)
  val MoveLeft: Vertex  = Vertex(-1, 0)
  val MoveRight: Vertex = Vertex(1, 0)
