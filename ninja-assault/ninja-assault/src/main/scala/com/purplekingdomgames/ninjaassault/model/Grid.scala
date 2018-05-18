package com.purplekingdomgames.ninjaassault.model

import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.Point

case class Grid(gridSize: Int, width: Int, height: Int, rows: List[GridRow]) {
  val levelWidthInPixels: Int  = width * gridSize
  val levelHeightInPixels: Int = height * gridSize
  val levelSizeInPixels: Point = Point(width, gridSize)
}

case class GridRow(columns: List[GridSquare])

case class GridSquare(row: Int, column: Int, tile: Tile)

sealed trait Tile
case object Floor  extends Tile
case object Column extends Tile
case object Wall   extends Tile
