package indigoextras.ui.datatypes

import indigo.*

/** Represents a position on the ui grid, rather than a position on the screen.
  */
opaque type Coords = Point

object Coords:

  inline def apply(value: Int): Coords     = Point(value)
  inline def apply(x: Int, y: Int): Coords = Point(x, y)
  inline def apply(point: Point): Coords   = point

  def fromScreenSpace(pt: Point, charSize: Size): Coords =
    Coords(pt / charSize.toPoint)

  val zero: Coords = Coords(0, 0)
  val one: Coords  = Coords(1, 1)

  extension (c: Coords)
    private[datatypes] inline def toPoint: Point    = c
    inline def unsafeToPoint: Point                 = c
    inline def toDimensions: Dimensions             = Dimensions(c.toSize)
    inline def toScreenSpace(charSize: Size): Point = c * charSize.toPoint

    inline def x: Int = c.x
    inline def y: Int = c.y

    inline def +(other: Coords): Coords = c + other
    inline def +(i: Int): Coords        = c + i
    inline def -(other: Coords): Coords = c - other
    inline def -(i: Int): Coords        = c - i
    inline def *(other: Coords): Coords = c * other
    inline def *(i: Int): Coords        = c * i
    inline def /(other: Coords): Coords = c / other
    inline def /(i: Int): Coords        = c / i

    inline def abs: Coords = c.abs
