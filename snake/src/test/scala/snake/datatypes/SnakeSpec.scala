package snake.datatypes

import com.purplekingdomgames.indigoexts.grid.{GridPoint, GridSize}
import org.scalatest.{FunSpec, Matchers}
import snake.datatypes.CollisionCheckOutcome.{Crashed, NoCollision, PickUp}

class SnakeSpec extends FunSpec with Matchers {

  val gridSize: GridSize = GridSize(10, 10, 16)

  def collisionF: GridPoint => CollisionCheckOutcome = pt => NoCollision(pt)

  def tick(snake: Snake, count: Int): Snake =
    if (count == 0) snake
    else tick(snake.update(gridSize, collisionF)._1, count - 1)

  implicit class Tickable(snake: Snake) {
    def doTick(): Snake = tick(snake, 1)
  }

  describe("Moving and turning") {

    it("should advance forward on each tick") {

      val s = tick(Snake(GridPoint.identity), 1)

      s.length shouldEqual 1
      s.start shouldEqual GridPoint(0, 1)

    }

    it("should be able to turn left") {

      val s = Snake(GridPoint(1, 1)).turnLeft

      s.direction shouldEqual SnakeDirection.Left

      val s2 = tick(s, 1)

      s2.start shouldEqual GridPoint(0, 1)

    }

    it("should be able to turn right") {

      val s = Snake(GridPoint.identity).turnRight

      s.direction shouldEqual SnakeDirection.Right

      val s2 = tick(s, 1)

      s2.start shouldEqual GridPoint(1, 0)

    }

    it("should wrap the world") {
      withClue("up and over") {
        tick(Snake(GridPoint(0, 5)), 5).start shouldEqual GridPoint(0, 0)
      }

      withClue("down and out") {
        tick(Snake(GridPoint(5, 0)).turnLeft.turnLeft, 1).start shouldEqual GridPoint(5, 10)
      }
    }

    it("should be able to move") {

      val path: List[(Int, Int)] =
        Snake(GridPoint.identity).grow
          .doTick()
          .grow
          .turnRight
          .doTick()
          .grow
          .doTick()
          .grow
          .turnLeft
          .doTick()
          .grow
          .doTick()
          .grow
          .doTick()
          .grow
          .turnLeft
          .doTick()
          .grow
          .turnLeft
          .doTick()
          .givePathList

      val expected: List[(Int, Int)] = List(
        (0, 0), //start
        (0, 1), //up
        (1, 1), //right
        (2, 1), //right
        (2, 2), //up
        (2, 3), //up
        (2, 4), //up
        (1, 4), //left
        (1, 3) //down
      ).reverse

      path shouldEqual expected

    }

  }

  describe("Growing") {

    it("should be able to grow") {
      val s = Snake(GridPoint.identity).grow
      s.length shouldEqual 2
      s.body.length shouldEqual 1
      s.start shouldEqual s.body.headOption.get

      val s2 = Snake(GridPoint(0, 3), List(GridPoint(0, 2), GridPoint(0, 1)), SnakeDirection.Up, SnakeStatus.Alive).grow
      s2.length shouldEqual 4
      s2.body.length shouldEqual 3
      s2.start shouldEqual GridPoint(0, 3)
      s2.end shouldEqual GridPoint(0, 1)
      s2.body shouldEqual List(GridPoint(0, 2), GridPoint(0, 1), GridPoint(0, 1))
    }

  }

  describe("Colliding") {

    it("should die when it crashes into something") {
      val f: GridPoint => CollisionCheckOutcome = pt => Crashed(pt)

      val s = Snake(GridPoint.identity)
      s.status shouldEqual SnakeStatus.Alive

      val s2 = s.update(gridSize, f)._1
      s2.status shouldEqual SnakeStatus.Dead
    }

  }

  describe("Collecting") {

    it("should grow on item pick up") {
      val f: GridPoint => CollisionCheckOutcome = pt => PickUp(pt)

      val s = Snake(GridPoint.identity)
      s.length shouldEqual 1

      val s2 = s.update(gridSize, f)._1
      s2.length shouldEqual 2
    }

  }

}
