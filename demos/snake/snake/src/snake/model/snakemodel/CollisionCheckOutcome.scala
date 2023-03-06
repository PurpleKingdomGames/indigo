package snake.model.snakemodel

import indigoextras.geometry.Vertex

enum CollisionCheckOutcome(val gridPoint: Vertex):
  case NoCollision(gridPosition: Vertex) extends CollisionCheckOutcome(gridPosition)
  case PickUp(gridPosition: Vertex) extends CollisionCheckOutcome(gridPosition)
  case Crashed(gridPosition: Vertex) extends CollisionCheckOutcome(gridPosition)
