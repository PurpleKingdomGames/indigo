package indigoextras.ui.datatypes

import indigo.*

/** Represents a size on the ui grid, rather than a position on the screen.
  */
opaque type Dimensions = Size

object Dimensions:

  inline def apply(value: Int): Dimensions              = Size(value)
  inline def apply(width: Int, height: Int): Dimensions = Size(width, height)
  inline def apply(size: Size): Dimensions              = size

  val zero: Dimensions = Dimensions(0, 0)
  val one: Dimensions  = Dimensions(1, 1)

  extension (d: Dimensions)
    private[datatypes] inline def toSize: Size     = d
    inline def unsafeToSize: Size                  = d
    inline def toCoords: Coords                    = Coords(d.toPoint)
    inline def toScreenSpace(charSize: Size): Size = d * charSize

    inline def width: Int  = d.width
    inline def height: Int = d.height

    inline def +(other: Dimensions): Dimensions = d + other
    inline def +(i: Int): Dimensions            = d + i
    inline def -(other: Dimensions): Dimensions = d - other
    inline def -(i: Int): Dimensions            = d - i
    inline def *(other: Dimensions): Dimensions = d * other
    inline def *(i: Int): Dimensions            = d * i
    inline def /(other: Dimensions): Dimensions = d / other
    inline def /(i: Int): Dimensions            = d / i

    inline def min(other: Dimensions): Dimensions = d.min(other)
    inline def min(value: Int): Dimensions        = d.min(value)

    inline def max(other: Dimensions): Dimensions = d.max(other)
    inline def max(value: Int): Dimensions        = d.max(value)

    inline def abs: Dimensions = d.abs

    def withWidth(value: Int): Dimensions  = Dimensions(value, d.height)
    def withHeight(value: Int): Dimensions = Dimensions(d.width, value)
