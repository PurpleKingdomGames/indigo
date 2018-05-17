package com.purplekingdomgames.ninjaassault.model

import com.purplekingdomgames.indigo.gameengine.events.GameEvent
import com.purplekingdomgames.ninjaassault.JumpToMenu

sealed trait Scene {
  val active: Boolean
}
case class Logo(active: Boolean) extends Scene
case class Menu(active: Boolean) extends Scene

case class NinjaAssaultGameModel(scenes: List[Scene]) {
  val activeScene: Scene = scenes.find(_.active == true).getOrElse(Menu(true))

  def makeMenuSceneActive: NinjaAssaultGameModel =
    this.copy(
      scenes = scenes.map {
        case Menu(_) => Menu(true)
        case Logo(_) => Logo(false)
      }
    )

}

object NinjaAssaultGameModel {
  val initialModel: NinjaAssaultGameModel = NinjaAssaultGameModel(
    scenes = Logo(true) :: Menu(false) :: Nil
  )

  def update(state: NinjaAssaultGameModel): GameEvent => NinjaAssaultGameModel = {
    case JumpToMenu =>
      state.makeMenuSceneActive

    case _ =>
      state
  }

}

//object NinjaAssaultGameModel {
//
//  private val initialLevel = Level(
//    grid = Grid(
//      gridSize = 32,
//      width = 7,
//      height = 9,
//      rows = List(
//        GridRow(
//          columns = List(
//            GridSquare(0, 0, Floor),
//            GridSquare(0, 1, Floor),
//            GridSquare(0, 2, Floor),
//            GridSquare(0, 3, Floor),
//            GridSquare(0, 4, Floor),
//            GridSquare(0, 5, Floor),
//            GridSquare(0, 6, Floor)
//          )
//        ),
//        GridRow(
//          columns = List(
//            GridSquare(1, 0, Floor),
//            GridSquare(1, 1, Column),
//            GridSquare(1, 2, Floor),
//            GridSquare(1, 3, Floor),
//            GridSquare(1, 4, Floor),
//            GridSquare(1, 5, Column),
//            GridSquare(1, 6, Floor)
//          )
//        ),
//        GridRow(
//          columns = List(
//            GridSquare(2, 0, Floor),
//            GridSquare(2, 1, Floor),
//            GridSquare(2, 2, Floor),
//            GridSquare(2, 3, Floor),
//            GridSquare(2, 4, Floor),
//            GridSquare(2, 5, Floor),
//            GridSquare(2, 6, Floor)
//          )
//        ),
//        GridRow(
//          columns = List(
//            GridSquare(3, 0, Floor),
//            GridSquare(3, 1, Floor),
//            GridSquare(3, 2, Floor),
//            GridSquare(3, 3, Column),
//            GridSquare(3, 4, Floor),
//            GridSquare(3, 5, Floor),
//            GridSquare(3, 6, Floor)
//          )
//        ),
//        GridRow(
//          columns = List(
//            GridSquare(4, 0, Floor),
//            GridSquare(4, 1, Floor),
//            GridSquare(4, 2, Floor),
//            GridSquare(4, 3, Floor),
//            GridSquare(4, 4, Floor),
//            GridSquare(4, 5, Floor),
//            GridSquare(4, 6, Floor)
//          )
//        ),
//        GridRow(
//          columns = List(
//            GridSquare(5, 0, Floor),
//            GridSquare(5, 1, Column),
//            GridSquare(5, 2, Floor),
//            GridSquare(5, 3, Floor),
//            GridSquare(5, 4, Floor),
//            GridSquare(5, 5, Column),
//            GridSquare(5, 6, Floor)
//          )
//        ),
//        GridRow(
//          columns = List(
//            GridSquare(6, 0, Floor),
//            GridSquare(6, 1, Floor),
//            GridSquare(6, 2, Floor),
//            GridSquare(6, 3, Floor),
//            GridSquare(6, 4, Floor),
//            GridSquare(6, 5, Floor),
//            GridSquare(6, 6, Floor)
//          )
//        ),
//        GridRow(
//          columns = List(
//            GridSquare(7, 0, Floor),
//            GridSquare(7, 1, Floor),
//            GridSquare(7, 2, Wall),
//            GridSquare(7, 3, Wall),
//            GridSquare(7, 4, Wall),
//            GridSquare(7, 5, Floor),
//            GridSquare(7, 6, Floor)
//          )
//        ),
//        GridRow(
//          columns = List(
//            GridSquare(8, 0, Floor),
//            GridSquare(8, 1, Floor),
//            GridSquare(8, 2, Floor),
//            GridSquare(8, 3, Floor),
//            GridSquare(8, 4, Floor),
//            GridSquare(8, 5, Floor),
//            GridSquare(8, 6, Floor)
//          )
//        )
//      )
//    ),
//    gameActors = List(
////      Guard(0, 0),
//      Ninja(8, 3)
//    )
//  )
//
//  val initialModel: NinjaAssaultGameModel = NinjaAssaultGameModel(
//    level = initialLevel,
//    whosTurn = Human
//  )
//
//  def update(gameTime: GameTime, state: NinjaAssaultGameModel): GameEvent => NinjaAssaultGameModel = { e =>
//    initialModel
//  }
//
//}
//
//case class NinjaAssaultGameModel(level: Level, whosTurn: Player)
//
//case class Level(grid: Grid, gameActors: List[GameActor])
//
//case class Grid(gridSize: Int, width: Int, height: Int, rows: List[GridRow])
//
//case class GridRow(columns: List[GridSquare])
//
//case class GridSquare(row: Int, column: Int, tile: Tile)
//
//sealed trait Tile
//case object Floor extends Tile
//case object Column extends Tile
//case object Wall extends Tile
//
//sealed trait GameActor {
//  val row: Int
//  val column: Int
//}
//case class Ninja(row: Int, column: Int) extends GameActor
//case class Guard(row: Int, column: Int) extends GameActor
//
//sealed trait Player
//case object CPU extends Player
//case object Human extends Player

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
/*
//lens example:


  import scalaz._
  import Scalaz._

  case class Point(x: Double, y: Double)

  case class Color(r: Byte, g: Byte, b: Byte)

  case class Turtle(
         position: Point,
         heading: Double,
         color: Color)

  Turtle(Point(2.0, 3.0), 0.0, Color(255.toByte, 255.toByte, 255.toByte))

  val turtlePosition = Lens.lensu[Turtle, Point] (
         (a, value) => a.copy(position = value),
         _.position
       )

  val pointX = Lens.lensu[Point, Double] (
         (a, value) => a.copy(x = value),
         _.x
       )

  // So this updates the turtles x position.
  // Note that the power here is the ability to compose
  // these sub levels of lens modifier into bigger modifiers.
  val turtleX = turtlePosition >=> pointX

  val t0 = Turtle(Point(2.0, 3.0), 0.0,
                  Color(255.toByte, 255.toByte, 255.toByte))

  turtleX.get(t0)

  turtleX.set(t0, 5.0)

  turtleX.mod(_ + 1.0, t0)

  val incX = turtleX =>= {_ + 1.0}

  incX(t0)

 */
