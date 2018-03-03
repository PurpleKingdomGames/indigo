package snake.arenas

import com.purplekingdomgames.indigoat.grid.{GridPoint, GridSize}

import scala.util.Random

sealed trait MapElement {

  val gridPoint: GridPoint

  def renderAsString: String

}

object MapElement {

  case class Wall(gridPoint: GridPoint) extends MapElement {
    def pure[A <: MapElement]: GridPoint => Wall = (gridPoint: GridPoint) => Wall.apply(gridPoint)
    def renderAsString: String = "Wall"
  }

  case class Apple(gridPoint: GridPoint) extends MapElement {
    def pure[A <: MapElement]: GridPoint => Apple = (gridPoint: GridPoint) => Apple.apply(gridPoint)
    def renderAsString: String = "Apple"
  }

  object Apple {
    def spawn(gridSize: GridSize): Apple = {
      def rand(max: Int, border: Int): Int =
        ((max - (border * 2)) * Random.nextFloat()).toInt + border

      Apple(GridPoint(rand(gridSize.columns, 1), rand(gridSize.rows, 1)))
    }
  }

  case class Player1Start(gridPoint: GridPoint) extends MapElement {
    def pure[A <: MapElement]: GridPoint => Player1Start = (gridPoint: GridPoint) => Player1Start.apply(gridPoint)
    def renderAsString: String = "Player 1 Start"
  }

  case class Player2Start(gridPoint: GridPoint) extends MapElement {
    def pure[A <: MapElement]: GridPoint => Player2Start = (gridPoint: GridPoint) => Player2Start.apply(gridPoint)
    def renderAsString: String = "Player 2 Start"
  }
}
