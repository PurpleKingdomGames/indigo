package snake.model.snakemodel

enum SnakeStatus derives CanEqual:
  case Alive, Dead

object SnakeStatus:
  extension (ss: SnakeStatus)
    def isDead: Boolean =
      ss match
        case SnakeStatus.Alive => false
        case SnakeStatus.Dead  => true
