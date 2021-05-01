package indigoextras.pathfinding

sealed trait GridSquare extends Product with Serializable derives CanEqual {
  val index: Int
  val coords: Coords
  val name: String
  val isStart: Boolean
  val isEnd: Boolean
  val score: Option[Int]
  def withScore(score: Int): GridSquare
}

object GridSquare {
  val max: Int = 99999999

  final case class EmptySquare(index: Int, coords: Coords, score: Option[Int]) extends GridSquare {
    val name: String                       = "empty"
    val isStart: Boolean                   = false
    val isEnd: Boolean                     = false
    def withScore(score: Int): EmptySquare = this.copy(score = Option(score))
  }
  final case class ImpassableSquare(index: Int, coords: Coords) extends GridSquare {
    val name: String                            = "impassable"
    val isStart: Boolean                        = false
    val isEnd: Boolean                          = false
    val score: Option[Int]                      = Some(GridSquare.max)
    def withScore(score: Int): ImpassableSquare = this
  }
  final case class StartSquare(index: Int, coords: Coords) extends GridSquare {
    val name: String                       = "start"
    val isStart: Boolean                   = true
    val isEnd: Boolean                     = false
    val score: Option[Int]                 = Some(GridSquare.max)
    def withScore(score: Int): StartSquare = this
  }
  final case class EndSquare(index: Int, coords: Coords) extends GridSquare {
    val name: String                     = "end"
    val isStart: Boolean                 = false
    val isEnd: Boolean                   = true
    val score: Option[Int]               = Some(0)
    def withScore(score: Int): EndSquare = this
  }
}
