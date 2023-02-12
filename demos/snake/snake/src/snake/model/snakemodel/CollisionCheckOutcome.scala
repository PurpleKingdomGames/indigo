package snake.model.snakemodel

import indigoextras.geometry.Vertex

sealed trait CollisionCheckOutcome {
  val gridPoint: Vertex
}
object CollisionCheckOutcome {
  final case class NoCollision(gridPoint: Vertex) extends CollisionCheckOutcome
  final case class PickUp(gridPoint: Vertex)      extends CollisionCheckOutcome
  final case class Crashed(gridPoint: Vertex)     extends CollisionCheckOutcome
}
