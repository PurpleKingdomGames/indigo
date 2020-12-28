package snake.model.arena

import indigoextras.geometry.Vertex

sealed trait MapElement {
  val gridPoint: Vertex
}

object MapElement {
  case class Wall(gridPoint: Vertex)  extends MapElement
  case class Apple(gridPoint: Vertex) extends MapElement
}
