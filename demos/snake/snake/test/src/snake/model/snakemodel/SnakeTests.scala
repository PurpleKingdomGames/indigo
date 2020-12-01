package snake.model.snakemodel

import snake.model.grid.{GridPoint, GridSize}

class SnakeTests extends munit.FunSuite {

  val gridSize: GridSize = GridSize(10, 10, 16)

  def collisionF: GridPoint => CollisionCheckOutcome = pt => CollisionCheckOutcome.NoCollision(pt)

  def tick(snake: Snake, count: Int): Snake =
    if (count == 0) snake
    else tick(snake.update(gridSize, collisionF)._1, count - 1)

  implicit class Tickable(snake: Snake) {
    def doTick(): Snake = tick(snake, 1)
  }

  test("Moving and turning.should advance forward on each tick") {

    val s = tick(Snake(GridPoint.identity), 1)

    assertEquals(s.length, 1)
    assertEquals(s.start, GridPoint(0, 1))

  }

  test("Moving and turning.Turning.should be able to turn left") {

    val s = Snake(GridPoint(1, 1)).turnLeft

    assertEquals(s.direction, SnakeDirection.Left)

    val s2 = tick(s, 1)

    assertEquals(s2.start, GridPoint(0, 1))

  }

  test("Moving and turning.Turning.should be able to turn right") {

    val s = Snake(GridPoint.identity).turnRight

    assertEquals(s.direction, SnakeDirection.Right)

    val s2 = tick(s, 1)

    assertEquals(s2.start, GridPoint(1, 0))

  }

  test("Moving and turning.Going (instead of turning).should be able to go up") {

    //Turning proved in another test, turning to allow a legal move
    val s = Snake(GridPoint(1, 1)).turnLeft.goUp

    assertEquals(s.direction, SnakeDirection.Up)

    val s2 = tick(s, 1)

    assertEquals(s2.start, GridPoint(1, 2))

  }

  test("Moving and turning.Going (instead of turning).should be able to go down") {

    //Turning proved in another test, turning to allow a legal move
    val s = Snake(GridPoint(1, 1)).turnLeft.goDown

    assertEquals(s.direction, SnakeDirection.Down)

    val s2 = tick(s, 1)

    assertEquals(s2.start, GridPoint(1, 0))

  }

  test("Moving and turning.Going (instead of turning).should be able to go left") {

    val s = Snake(GridPoint(1, 1)).goLeft

    assertEquals(s.direction, SnakeDirection.Left)

    val s2 = tick(s, 1)

    assertEquals(s2.start, GridPoint(0, 1))

  }

  test("Moving and turning.Going (instead of turning).should be able to go right") {

    val s = Snake(GridPoint(1, 1)).goRight

    assertEquals(s.direction, SnakeDirection.Right)

    val s2 = tick(s, 1)

    assertEquals(s2.start, GridPoint(2, 1))

  }

  test("Moving and turning.should wrap the world.up and over") {
    assertEquals(tick(Snake(GridPoint(0, 5)), 5).start, GridPoint(0, 0))
  }

  test("Moving and turning.should wrap the world.down and out") {
    assertEquals(tick(Snake(GridPoint(5, 0)).turnLeft.turnLeft, 1).start, GridPoint(5, 10))
  }

  test("Moving and turning.should be able to move") {

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

    assertEquals(path, expected)

  }

  test("Growing.should be able to grow") {
    val s = Snake(GridPoint.identity).grow
    assertEquals(s.length, 2)
    assertEquals(s.body.length, 1)
    assertEquals(s.start, s.body.headOption.get)

    val s2 = Snake(GridPoint(0, 3), List(GridPoint(0, 2), GridPoint(0, 1)), SnakeDirection.Up, SnakeStatus.Alive).grow
    assertEquals(s2.length, 4)
    assertEquals(s2.body.length, 3)
    assertEquals(s2.start, GridPoint(0, 3))
    assertEquals(s2.end, GridPoint(0, 1))
    assertEquals(s2.body, List(GridPoint(0, 2), GridPoint(0, 1), GridPoint(0, 1)))
  }

  test("Shrinking.should be able to shrink") {
    val s =
      Snake(GridPoint(0, 3), List(GridPoint(0, 2), GridPoint(0, 1), GridPoint(0, 0)), SnakeDirection.Up, SnakeStatus.Alive)

    assertEquals(s.length, 4)

    assertEquals(s.shrink.length, 3)
    assertEquals(s.shrink.start, GridPoint(0, 3))
    assertEquals(s.shrink.end, GridPoint(0, 1))
    assertEquals(s.shrink.body, List(GridPoint(0, 2), GridPoint(0, 1)))

    assertEquals(s.shrink.shrink.shrink.start, GridPoint(0, 3))
    assertEquals(s.shrink.shrink.shrink.end, GridPoint(0, 3))
    assertEquals(s.shrink.shrink.shrink.shrink.shrink.shrink.shrink.length, 1)
  }

  test("Colliding.should die when it crashes into something") {
    val f: GridPoint => CollisionCheckOutcome = pt => CollisionCheckOutcome.Crashed(pt)

    val s = Snake(GridPoint.identity)
    assertEquals(s.status, SnakeStatus.Alive)

    val s2 = s.update(gridSize, f)._1
    assertEquals(s2.status, SnakeStatus.Dead)
  }

  test("Collecting.should grow on item pick up") {
    val f: GridPoint => CollisionCheckOutcome = pt => CollisionCheckOutcome.PickUp(pt)

    val s = Snake(GridPoint.identity)
    assertEquals(s.length, 1)

    val s2 = s.update(gridSize, f)._1
    assertEquals(s2.length, 2)
  }

}
