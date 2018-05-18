package com.purplekingdomgames.ninjaassault.model

case class Level(grid: Grid, gameActors: List[GameActor])

object Level {

  /*

  7x9

  .......
  .x...x.
  .......
  ...x...
  .......
  .x...x.
  .......
  ..XXX..
  .......

   */

  val testLevel: Level = Level(
    grid = Grid(
      gridSize = 32,
      width = 7,
      height = 9,
      rows = List(
        GridRow(
          columns = List(
            GridSquare(0, 0, Floor),
            GridSquare(0, 1, Floor),
            GridSquare(0, 2, Floor),
            GridSquare(0, 3, Floor),
            GridSquare(0, 4, Floor),
            GridSquare(0, 5, Floor),
            GridSquare(0, 6, Floor)
          )
        ),
        GridRow(
          columns = List(
            GridSquare(1, 0, Floor),
            GridSquare(1, 1, Column),
            GridSquare(1, 2, Floor),
            GridSquare(1, 3, Floor),
            GridSquare(1, 4, Floor),
            GridSquare(1, 5, Column),
            GridSquare(1, 6, Floor)
          )
        ),
        GridRow(
          columns = List(
            GridSquare(2, 0, Floor),
            GridSquare(2, 1, Floor),
            GridSquare(2, 2, Floor),
            GridSquare(2, 3, Floor),
            GridSquare(2, 4, Floor),
            GridSquare(2, 5, Floor),
            GridSquare(2, 6, Floor)
          )
        ),
        GridRow(
          columns = List(
            GridSquare(3, 0, Floor),
            GridSquare(3, 1, Floor),
            GridSquare(3, 2, Floor),
            GridSquare(3, 3, Column),
            GridSquare(3, 4, Floor),
            GridSquare(3, 5, Floor),
            GridSquare(3, 6, Floor)
          )
        ),
        GridRow(
          columns = List(
            GridSquare(4, 0, Floor),
            GridSquare(4, 1, Floor),
            GridSquare(4, 2, Floor),
            GridSquare(4, 3, Floor),
            GridSquare(4, 4, Floor),
            GridSquare(4, 5, Floor),
            GridSquare(4, 6, Floor)
          )
        ),
        GridRow(
          columns = List(
            GridSquare(5, 0, Floor),
            GridSquare(5, 1, Column),
            GridSquare(5, 2, Floor),
            GridSquare(5, 3, Floor),
            GridSquare(5, 4, Floor),
            GridSquare(5, 5, Column),
            GridSquare(5, 6, Floor)
          )
        ),
        GridRow(
          columns = List(
            GridSquare(6, 0, Floor),
            GridSquare(6, 1, Floor),
            GridSquare(6, 2, Floor),
            GridSquare(6, 3, Floor),
            GridSquare(6, 4, Floor),
            GridSquare(6, 5, Floor),
            GridSquare(6, 6, Floor)
          )
        ),
        GridRow(
          columns = List(
            GridSquare(7, 0, Floor),
            GridSquare(7, 1, Floor),
            GridSquare(7, 2, Wall),
            GridSquare(7, 3, Wall),
            GridSquare(7, 4, Wall),
            GridSquare(7, 5, Floor),
            GridSquare(7, 6, Floor)
          )
        ),
        GridRow(
          columns = List(
            GridSquare(8, 0, Floor),
            GridSquare(8, 1, Floor),
            GridSquare(8, 2, Floor),
            GridSquare(8, 3, Floor),
            GridSquare(8, 4, Floor),
            GridSquare(8, 5, Floor),
            GridSquare(8, 6, Floor)
          )
        )
      )
    ),
    gameActors = List(
      //      Guard(0, 0),
      Ninja(8, 3)
    )
  )

}
