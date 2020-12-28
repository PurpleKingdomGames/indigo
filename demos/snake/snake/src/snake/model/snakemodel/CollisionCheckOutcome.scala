package snake.model.snakemodel

import indigoextras.geometry.Vertex


sealed trait CollisionCheckOutcome {
  val gridPoint: Vertex
}
object CollisionCheckOutcome {
  case class NoCollision(gridPoint: Vertex) extends CollisionCheckOutcome
  case class PickUp(gridPoint: Vertex)      extends CollisionCheckOutcome
  case class Crashed(gridPoint: Vertex)     extends CollisionCheckOutcome
}
