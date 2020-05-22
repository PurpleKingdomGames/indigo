package indigoextras.pathfinding

import indigo.shared.EqualTo
import indigo.shared.AsString

final case class Coords(x: Int, y: Int) {

  def toGridPosition(gridWidth: Int): Int =
    Coords.toGridPosition(this, gridWidth)

  def +(other: Coords): Coords =
    Coords.add(this, other)

}

object Coords {

  implicit val show: AsString[Coords] = {
    val showI = implicitly[AsString[Int]]
    AsString.create(p => s"""Coords(${showI.show(p.x)}, ${showI.show(p.y)})""")
  }

  implicit val eq: EqualTo[Coords] = {
    val eqI = implicitly[EqualTo[Int]]
    EqualTo.create { (a, b) =>
      eqI.equal(a.x, b.x) && eqI.equal(a.y, b.y)
    }
  }

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

  def add(a: Coords, b: Coords): Coords =
    Coords(a.x + b.x, a.y + b.y)

}
