package snake.model.snakemodel

import indigoexts.grid.{GridPoint, GridSize}

import utest._

object SnakeTests extends TestSuite {

  val gridSize: GridSize = GridSize(10, 10, 16)

  def collisionF: GridPoint => CollisionCheckOutcome = pt => CollisionCheckOutcome.NoCollision(pt)

  def tick(snake: Snake, count: Int): Snake =
    if (count == 0) snake
    else tick(snake.update(gridSize, collisionF)._1, count - 1)

  implicit class Tickable(snake: Snake) {
    def doTick(): Snake = tick(snake, 1)
  }

  val tests: Tests =
    Tests {
      "Moving and turning" - {

        "should advance forward on each tick" - {

          val s = tick(Snake(GridPoint.identity), 1)

          s.length ==> 1
          s.start ==> GridPoint(0, 1)

        }

        "Turning" - {
          "should be able to turn left" - {

            val s = Snake(GridPoint(1, 1)).turnLeft

            s.direction ==> SnakeDirection.Left

            val s2 = tick(s, 1)

            s2.start ==> GridPoint(0, 1)

          }

          "should be able to turn right" - {

            val s = Snake(GridPoint.identity).turnRight

            s.direction ==> SnakeDirection.Right

            val s2 = tick(s, 1)

            s2.start ==> GridPoint(1, 0)

          }
        }

        "Going (instead of turning)" - {

          "should be able to go up" - {

            //Turning proved in another test, turning to allow a legal move
            val s = Snake(GridPoint(1, 1)).turnLeft.goUp

            s.direction ==> SnakeDirection.Up

            val s2 = tick(s, 1)

            s2.start ==> GridPoint(1, 2)

          }

          "should be able to go down" - {

            //Turning proved in another test, turning to allow a legal move
            val s = Snake(GridPoint(1, 1)).turnLeft.goDown

            s.direction ==> SnakeDirection.Down

            val s2 = tick(s, 1)

            s2.start ==> GridPoint(1, 0)

          }

          "should be able to go left" - {

            val s = Snake(GridPoint(1, 1)).goLeft

            s.direction ==> SnakeDirection.Left

            val s2 = tick(s, 1)

            s2.start ==> GridPoint(0, 1)

          }

          "should be able to go right" - {

            val s = Snake(GridPoint(1, 1)).goRight

            s.direction ==> SnakeDirection.Right

            val s2 = tick(s, 1)

            s2.start ==> GridPoint(2, 1)

          }

        }

        "should wrap the world" - {
          "up and over" - {
            tick(Snake(GridPoint(0, 5)), 5).start ==> GridPoint(0, 0)
          }

          "down and out" - {
            tick(Snake(GridPoint(5, 0)).turnLeft.turnLeft, 1).start ==> GridPoint(5, 10)
          }
        }

        "should be able to move" - {

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
            (1, 3)  //down
          ).reverse

          path ==> expected

        }

      }

      "Growing" - {

        "should be able to grow" - {
          val s = Snake(GridPoint.identity).grow
          s.length ==> 2
          s.body.length ==> 1
          s.start ==> s.body.headOption.get

          val s2 = Snake(GridPoint(0, 3), List(GridPoint(0, 2), GridPoint(0, 1)), SnakeDirection.Up, SnakeStatus.Alive).grow
          s2.length ==> 4
          s2.body.length ==> 3
          s2.start ==> GridPoint(0, 3)
          s2.end ==> GridPoint(0, 1)
          s2.body ==> List(GridPoint(0, 2), GridPoint(0, 1), GridPoint(0, 1))
        }

      }

      "Shrinking" - {

        "should be able to shrink" - {
          val s =
            Snake(GridPoint(0, 3), List(GridPoint(0, 2), GridPoint(0, 1), GridPoint(0, 0)), SnakeDirection.Up, SnakeStatus.Alive)

          s.length ==> 4

          s.shrink.length ==> 3
          s.shrink.start ==> GridPoint(0, 3)
          s.shrink.end ==> GridPoint(0, 1)
          s.shrink.body ==> List(GridPoint(0, 2), GridPoint(0, 1))

          s.shrink.shrink.shrink.start ==> GridPoint(0, 3)
          s.shrink.shrink.shrink.end ==> GridPoint(0, 3)
          s.shrink.shrink.shrink.shrink.shrink.shrink.shrink.length ==> 1
        }

      }

      "Colliding" - {

        "should die when it crashes into something" - {
          val f: GridPoint => CollisionCheckOutcome = pt => CollisionCheckOutcome.Crashed(pt)

          val s = Snake(GridPoint.identity)
          s.status ==> SnakeStatus.Alive

          val s2 = s.update(gridSize, f)._1
          s2.status ==> SnakeStatus.Dead
        }

      }

      "Collecting" - {

        "should grow on item pick up" - {
          val f: GridPoint => CollisionCheckOutcome = pt => CollisionCheckOutcome.PickUp(pt)

          val s = Snake(GridPoint.identity)
          s.length ==> 1

          val s2 = s.update(gridSize, f)._1
          s2.length ==> 2
        }

      }
    }

}
