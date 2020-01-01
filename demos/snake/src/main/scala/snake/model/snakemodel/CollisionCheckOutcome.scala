package snake.model.snakemodel

import indigoexts.grids._

sealed trait CollisionCheckOutcome {
  val gridPoint: GridPoint
}
object CollisionCheckOutcome {
  case class NoCollision(gridPoint: GridPoint) extends CollisionCheckOutcome
  case class PickUp(gridPoint: GridPoint)      extends CollisionCheckOutcome
  case class Crashed(gridPoint: GridPoint)     extends CollisionCheckOutcome
}