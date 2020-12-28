package snake.model.arena

import indigoextras.geometry.Vertex

sealed trait MapElement {
  val gridPoint: Vertex
}

object MapElement {
  final case class Wall(gridPoint: Vertex)  extends MapElement
  final case class Apple(gridPoint: Vertex) extends MapElement
}
