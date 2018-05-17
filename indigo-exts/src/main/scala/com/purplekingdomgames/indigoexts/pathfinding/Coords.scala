package com.purplekingdomgames.indigoexts.pathfinding

case class Coords(x: Int, y: Int) {

  def toGridPosition(gridWidth: Int): Int =
    Coords.toGridPosition(this, gridWidth)

  def ===(other: Coords): Boolean =
    Coords.equality(this, other)

  def +(other: Coords): Coords =
    Coords.add(this, other)

}

object Coords {

  val relativeUpLeft: Coords    = Coords(-1, -1)
  val relativeUp: Coords        = Coords(0, -1)
  val relativeUpRight: Coords   = Coords(1, -1)
  val relativeLeft: Coords      = Coords(-1, 0)
  val relativeRight: Coords     = Coords(1, 0)
  val relativeDownLeft: Coords  = Coords(-1, 1)
  val relativeDown: Coords      = Coords(0, 1)
  val relativeDownRight: Coords = Coords(1, 1)

  def toGridPosition(coords: Coords, gridWidth: Int): Int =
    coords.x + (coords.y * gridWidth)

  def fromIndex(index: Int, gridWidth: Int): Coords =
    Coords(
      x = index % gridWidth,
      y = index / gridWidth
    )

  def equality(a: Coords, b: Coords): Boolean =
    a.x == b.x && a.y == b.y

  def add(a: Coords, b: Coords): Coords =
    Coords(a.x + b.x, a.y + b.y)

}
