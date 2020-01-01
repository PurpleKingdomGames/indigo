package snake.model.snakemodel

sealed trait SnakeStatus {
  def isDead: Boolean =
    this match {
      case SnakeStatus.Alive => false
      case SnakeStatus.Dead  => true
    }
}
object SnakeStatus {
  case object Alive extends SnakeStatus
  case object Dead  extends SnakeStatus
}
